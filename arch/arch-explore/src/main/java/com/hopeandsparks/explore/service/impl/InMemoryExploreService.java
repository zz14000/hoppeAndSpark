package com.hopeandsparks.explore.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.explore.dto.ExploreRequest;
import com.hopeandsparks.explore.dto.MindMapRequest;
import com.hopeandsparks.explore.entity.ExploreDraftResource;
import com.hopeandsparks.explore.entity.ExploreRecord;
import com.hopeandsparks.explore.service.ExploreService;
import com.hopeandsparks.explore.vo.ExploreResourceVO;
import com.hopeandsparks.explore.vo.ExploreVO;
import com.hopeandsparks.explore.vo.MindMapEdgeVO;
import com.hopeandsparks.explore.vo.MindMapNodeVO;
import com.hopeandsparks.explore.vo.MindMapVO;
import com.hopeandsparks.infra.coze.CozeAgentClient;
import com.hopeandsparks.infra.coze.CozeAgentRequest;
import com.hopeandsparks.infra.coze.CozeAgentResponse;
import com.hopeandsparks.infra.redis.RedisStreamClient;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.service.AsyncTaskService;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nebula 探索 W3 内存实现。只触发任务和返回草案，不直接写 learning_resource。
 */
@Service
public class InMemoryExploreService implements ExploreService {

    private static final String RESOURCE_GENERATE_STREAM = "resource.generate";

    private final AsyncTaskService asyncTaskService;
    private final RedisStreamClient redisStreamClient;
    private final CozeAgentClient cozeAgentClient;
    private final Map<String, ExploreRecord> records = new ConcurrentHashMap<>();
    private final AtomicLong exploreSequence = new AtomicLong(1001);
    private final AtomicLong mindMapSequence = new AtomicLong(1001);

    public InMemoryExploreService(
            AsyncTaskService asyncTaskService,
            RedisStreamClient redisStreamClient,
            CozeAgentClient cozeAgentClient
    ) {
        this.asyncTaskService = asyncTaskService;
        this.redisStreamClient = redisStreamClient;
        this.cozeAgentClient = cozeAgentClient;
    }

    @Override
    public ExploreVO explore(AuthenticatedPrincipal principal, ExploreRequest request) {
        Long userId = requireUserId(principal);
        String query = extractQuery(request);
        String exploreId = "exp_" + exploreSequence.getAndIncrement();
        int depth = safeDepth(request == null ? null : request.depth());
        List<String> preferredTypes = preferredTypes(request);
        LocalDateTime now = LocalDateTime.now();

        AsyncTaskVO task = asyncTaskService.create(new CreateAsyncTaskCommand(
                "resource_generate",
                "explore",
                exploreId,
                null,
                3
        ));
        task = asyncTaskService.start(task.taskId());

        CozeAgentResponse response = runNebulaMockWorkflow(task.taskId(), query, request, preferredTypes);
        task = asyncTaskService.recordExternalRunId(task.taskId(), response.externalMessageId());
        String streamMessageId = publishExploreTask(userId, exploreId, task.taskId(), query, request, preferredTypes);
        task = asyncTaskService.updateProgress(task.taskId(), 35, "Nebula mock 任务已提交到 Redis Stream: " + streamMessageId);

        ExploreRecord record = new ExploreRecord();
        record.setExploreId(exploreId);
        record.setUserId(userId);
        record.setQuery(query);
        record.setDomain(request == null ? null : request.domain());
        record.setMode(firstText(request == null ? null : request.mode(), "deep"));
        record.setDepth(depth);
        record.setGoals(request == null || request.goals() == null ? List.of() : request.goals());
        record.setPreferredResourceTypes(preferredTypes);
        record.setSummary(buildSummary(query));
        record.setRelatedNodes(buildRelatedNodes(query, record.getDomain()));
        record.setResources(buildDraftResources(exploreId, query, preferredTypes));
        record.setTaskId(task.taskId());
        record.setStatus(task.status().name().toLowerCase(Locale.ROOT));
        record.setMock(response.mock());
        record.setCreatedAt(now);
        record.setUpdatedAt(LocalDateTime.now());

        records.put(record.getExploreId(), record);
        return toVO(record, task);
    }

    @Override
    public ExploreVO detail(AuthenticatedPrincipal principal, String exploreId) {
        ExploreRecord record = requireRecord(principal, exploreId);
        AsyncTaskVO task = asyncTaskService.getByTaskId(record.getTaskId());
        record.setStatus(task.status().name().toLowerCase(Locale.ROOT));
        record.setUpdatedAt(LocalDateTime.now());
        return toVO(record, task);
    }

    @Override
    public MindMapVO createMindMap(AuthenticatedPrincipal principal, String exploreId, MindMapRequest request) {
        ExploreRecord record = requireRecord(principal, exploreId);
        String mindmapId = "mm_" + mindMapSequence.getAndIncrement();

        AsyncTaskVO task = asyncTaskService.create(new CreateAsyncTaskCommand(
                "resource_generate",
                "explore_mindmap",
                mindmapId,
                null,
                2
        ));
        task = asyncTaskService.start(task.taskId());
        String streamMessageId = publishMindMapTask(record, mindmapId, task.taskId(), request);
        task = asyncTaskService.updateProgress(task.taskId(), 70, "思维导图 mock 任务已提交到 Redis Stream: " + streamMessageId);

        MindMapVO mindMap = buildMindMap(record, mindmapId, request, task);
        task = asyncTaskService.markSuccess(task.taskId(), "思维导图 mock 已生成");
        return new MindMapVO(mindMap.mindmapId(), mindMap.exploreId(), mindMap.style(), mindMap.nodes(), mindMap.edges(), task);
    }

    private CozeAgentResponse runNebulaMockWorkflow(String taskId, String query, ExploreRequest request, List<String> preferredTypes) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("task_id", taskId);
        context.put("task_type", "resource_generate");
        context.put("target_agent", "nebula");
        context.put("source_agent", "explore");
        context.put("resource_types", String.join(",", preferredTypes));
        putIfPresent(context, "domain", request == null ? null : request.domain());
        putIfPresent(context, "mode", request == null ? null : request.mode());
        return cozeAgentClient.runWorkflow(new CozeAgentRequest("nebula", query, null, context));
    }

    private String publishExploreTask(
            Long userId,
            String exploreId,
            String taskId,
            String query,
            ExploreRequest request,
            List<String> preferredTypes
    ) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("taskId", taskId);
        body.put("taskType", "resource_generate");
        body.put("targetAgent", "nebula");
        body.put("exploreId", exploreId);
        body.put("userId", String.valueOf(userId));
        body.put("query", query);
        body.put("preferredResourceTypes", String.join(",", preferredTypes));
        putIfPresent(body, "domain", request == null ? null : request.domain());
        putIfPresent(body, "mode", request == null ? null : request.mode());
        return redisStreamClient.publish(RESOURCE_GENERATE_STREAM, body);
    }

    private String publishMindMapTask(ExploreRecord record, String mindmapId, String taskId, MindMapRequest request) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("taskId", taskId);
        body.put("taskType", "resource_generate");
        body.put("outputType", "mindmap");
        body.put("targetAgent", "nebula");
        body.put("exploreId", record.getExploreId());
        body.put("mindmapId", mindmapId);
        body.put("query", record.getQuery());
        body.put("includeResources", String.valueOf(request == null || Boolean.TRUE.equals(request.includeResources())));
        putIfPresent(body, "style", request == null ? null : request.style());
        return redisStreamClient.publish(RESOURCE_GENERATE_STREAM, body);
    }

    private MindMapVO buildMindMap(ExploreRecord record, String mindmapId, MindMapRequest request, AsyncTaskVO task) {
        String style = firstText(request == null ? null : request.style(), "default");
        boolean includeResources = request == null || Boolean.TRUE.equals(request.includeResources());

        List<MindMapNodeVO> nodes = new ArrayList<>();
        List<MindMapEdgeVO> edges = new ArrayList<>();
        String rootId = "mmn_" + record.getExploreId() + "_root";
        nodes.add(new MindMapNodeVO(rootId, record.getQuery(), null, "topic", null));

        int index = 1;
        for (String relatedNode : record.getRelatedNodes()) {
            String nodeId = "mmn_" + record.getExploreId() + "_n" + index;
            nodes.add(new MindMapNodeVO(nodeId, relatedNode, rootId, "knowledge_node", null));
            edges.add(new MindMapEdgeVO("mme_" + record.getExploreId() + "_" + index, rootId, nodeId, "related"));
            index++;
        }

        if (includeResources) {
            for (ExploreDraftResource resource : record.getResources()) {
                String nodeId = "mmn_" + resource.id();
                nodes.add(new MindMapNodeVO(nodeId, resource.title(), rootId, "resource_draft", resource.id()));
                edges.add(new MindMapEdgeVO("mme_" + resource.id(), rootId, nodeId, resource.type()));
            }
        }
        return new MindMapVO(mindmapId, record.getExploreId(), style, nodes, edges, task);
    }

    private List<ExploreDraftResource> buildDraftResources(String exploreId, String query, List<String> preferredTypes) {
        List<ExploreDraftResource> resources = new ArrayList<>();
        int index = 1;
        for (String type : preferredTypes) {
            resources.add(new ExploreDraftResource(
                    "draft_res_" + exploreId + "_" + index,
                    type,
                    resourceTitle(query, type),
                    "Nebula mock 资源草案，等待 resource 模块后续落库。",
                    "draft_node_" + exploreId + "_" + index,
                    "draft"
            ));
            index++;
        }
        return resources;
    }

    private List<String> buildRelatedNodes(String query, String domain) {
        List<String> nodes = new ArrayList<>();
        if (!isBlank(domain)) {
            nodes.add(domain + " 基础");
        }
        nodes.add(query + " 核心概念");
        nodes.add(query + " 前置知识");
        nodes.add(query + " 实践场景");
        return nodes;
    }

    private String buildSummary(String query) {
        return "Nebula mock 已围绕「" + query + "」整理探索草案，资源最终落库仍交给 arch-resource。";
    }

    private String resourceTitle(String query, String type) {
        return switch (type) {
            case "document", "doc" -> query + " 入门讲义草案";
            case "video" -> query + " 视频脚本草案";
            case "exercise_set", "quiz" -> query + " 练习题草案";
            case "mindmap" -> query + " 思维导图草案";
            case "ppt" -> query + " PPT 提纲草案";
            default -> query + " " + type + " 资源草案";
        };
    }

    private ExploreVO toVO(ExploreRecord record, AsyncTaskVO task) {
        return new ExploreVO(
                record.getExploreId(),
                record.getQuery(),
                record.getDomain(),
                record.getMode(),
                record.getSummary(),
                record.getResources().stream().map(this::toResourceVO).toList(),
                record.getRelatedNodes(),
                task,
                record.getStatus(),
                record.isMock(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private ExploreResourceVO toResourceVO(ExploreDraftResource resource) {
        return new ExploreResourceVO(
                resource.id(),
                resource.type(),
                resource.title(),
                resource.summary(),
                resource.nodeId(),
                resource.status()
        );
    }

    private ExploreRecord requireRecord(AuthenticatedPrincipal principal, String exploreId) {
        Long userId = requireUserId(principal);
        ExploreRecord record = records.get(exploreId);
        if (record == null) {
            throw new BusinessException(404, "探索记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(403, "不能访问其他用户的探索记录");
        }
        return record;
    }

    private String extractQuery(ExploreRequest request) {
        if (request == null) {
            throw new BusinessException(400, "请求体不能为空");
        }
        String query = firstText(request.keyword(), request.query());
        if (isBlank(query)) {
            throw new BusinessException(400, "keyword 或 query 不能为空");
        }
        return query.trim();
    }

    private List<String> preferredTypes(ExploreRequest request) {
        List<String> types = request == null ? null : request.preferredResourceTypes();
        if (types == null || types.isEmpty()) {
            types = request == null ? null : request.preferredTypes();
        }
        if (types == null || types.isEmpty()) {
            return List.of("document", "mindmap", "exercise_set");
        }
        return types.stream()
                .filter(type -> !isBlank(type))
                .map(String::trim)
                .distinct()
                .limit(6)
                .toList();
    }

    private int safeDepth(Integer depth) {
        if (depth == null) {
            return 2;
        }
        return Math.max(1, Math.min(depth, 5));
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (!isBlank(value)) {
            map.put(key, value);
        }
    }

    private String firstText(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
