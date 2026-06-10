package com.hopeandsparks.study.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.knowledge.entity.Course;
import com.hopeandsparks.knowledge.entity.KnowledgeNode;
import com.hopeandsparks.knowledge.entity.KnowledgeRelation;
import com.hopeandsparks.knowledge.service.KnowledgeGraphService;
import com.hopeandsparks.resource.service.ResourceService;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.study.dto.PlanAdjustRequest;
import com.hopeandsparks.study.dto.PlanGenerateRequest;
import com.hopeandsparks.study.entity.StudyPlan;
import com.hopeandsparks.study.entity.StudyTask;
import com.hopeandsparks.study.entity.UserKnowledgeProgress;
import com.hopeandsparks.study.repository.StudyRepository;
import com.hopeandsparks.study.service.LearningPlanService;
import com.hopeandsparks.study.vo.ClickActionVO;
import com.hopeandsparks.study.vo.LearningPlanVO;
import com.hopeandsparks.study.vo.RelatedKnowledgeNodeVO;
import com.hopeandsparks.study.vo.ResourceNetworkCategoryVO;
import com.hopeandsparks.study.vo.ResourceNetworkEdgeVO;
import com.hopeandsparks.study.vo.ResourceNetworkKnowledgeVO;
import com.hopeandsparks.study.vo.ResourceNetworkResourceNodeVO;
import com.hopeandsparks.study.vo.ResourceNetworkStatsVO;
import com.hopeandsparks.study.vo.ResourceNetworkVO;
import com.hopeandsparks.study.vo.StudyTaskVO;
import com.hopeandsparks.study.vo.TopologyEdgeVO;
import com.hopeandsparks.study.vo.TopologyNodeVO;
import com.hopeandsparks.study.vo.TopologyVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LearningPlanServiceImpl implements LearningPlanService {

    private static final int MAX_GENERATED_TASKS = 20;

    private final StudyRepository studyRepository;
    private final KnowledgeGraphService knowledgeGraphService;
    private final ResourceService resourceService;

    public LearningPlanServiceImpl(
            StudyRepository studyRepository,
            KnowledgeGraphService knowledgeGraphService,
            ResourceService resourceService
    ) {
        this.studyRepository = studyRepository;
        this.knowledgeGraphService = knowledgeGraphService;
        this.resourceService = resourceService;
    }

    @Override
    public LearningPlanVO currentPlan(AuthenticatedPrincipal principal) {
        Long userId = requireUserId(principal);
        StudyPlan plan = studyRepository.findCurrentPlan(userId)
                .orElseThrow(() -> new BusinessException(404, "暂无学习计划，请先生成学习计划"));
        return toPlanVO(plan, studyRepository.listTasks(plan.id()));
    }

    @Override
    @Transactional
    public LearningPlanVO generatePlan(AuthenticatedPrincipal principal, PlanGenerateRequest request) {
        Long userId = requireUserId(principal);
        Course course = knowledgeGraphService.findCourse(
                        request == null ? null : request.courseId(),
                        firstText(request == null ? null : request.domain(), request == null ? null : request.goal())
                )
                .orElseThrow(() -> new BusinessException(404, "还没有可用课程，无法生成学习计划"));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = parseDate(request == null ? null : request.deadline())
                .filter(date -> !date.isBefore(startDate))
                .orElse(startDate.plusDays(14));
        String title = course.courseName() + "学习计划";
        if (request != null && request.goal() != null && !request.goal().isBlank()) {
            title = course.courseName() + " - " + request.goal().trim();
        }

        Long planId = studyRepository.insertPlan(userId, course.id(), title, startDate, endDate);
        List<KnowledgeNode> nodes = knowledgeGraphService.listActiveNodesByCourse(course.id());
        int dailyMinutes = normalizeDailyMinutes(request == null ? null : request.dailyMinutes());
        for (int i = 0; i < Math.min(nodes.size(), MAX_GENERATED_TASKS); i++) {
            KnowledgeNode node = nodes.get(i);
            Long resourceId = resourceService.firstResourceIdByNode(node.id()).orElse(null);
            LocalDateTime startTime = startDate.plusDays(i).atTime(19, 0);
            LocalDateTime endTime = startTime.plusMinutes(dailyMinutes);
            studyRepository.insertTask(
                    planId,
                    node.id(),
                    resourceId,
                    "学习：" + node.nodeName(),
                    "learn",
                    i + 1,
                    startTime,
                    endTime
            );
            studyRepository.upsertKnowledgeProgress(userId, node.id(), i == 0 ? "learning" : "locked", 0);
        }

        StudyPlan plan = studyRepository.findPlanForUser(userId, planId)
                .orElseThrow(() -> new BusinessException(500, "学习计划生成后读取失败"));
        return toPlanVO(plan, studyRepository.listTasks(plan.id()));
    }

    @Override
    @Transactional
    public LearningPlanVO adjustPlan(AuthenticatedPrincipal principal, String planId, PlanAdjustRequest request) {
        Long userId = requireUserId(principal);
        Long parsedPlanId = requireId(planId, "学习计划ID格式不正确");
        studyRepository.findPlanForUser(userId, parsedPlanId)
                .orElseThrow(() -> new BusinessException(404, "学习计划不存在"));
        String strategy = request == null ? null : request.strategy();
        studyRepository.adjustPlan(userId, parsedPlanId, strategy);
        studyRepository.adjustTasks(parsedPlanId, parseTaskIds(request == null ? null : request.taskIds()), strategy);
        StudyPlan updated = studyRepository.findPlanForUser(userId, parsedPlanId)
                .orElseThrow(() -> new BusinessException(404, "学习计划不存在"));
        return toPlanVO(updated, studyRepository.listTasks(parsedPlanId));
    }

    @Override
    public TopologyVO topology(AuthenticatedPrincipal principal, String planId) {
        Long userId = requireUserId(principal);
        StudyPlan plan = requirePlan(userId, planId);
        List<StudyTask> tasks = studyRepository.listTasks(plan.id());
        Set<Long> nodeIds = tasks.stream()
                .map(StudyTask::nodeId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (nodeIds.isEmpty()) {
            return new TopologyVO(String.valueOf(plan.id()), List.of(), List.of());
        }

        Map<Long, KnowledgeNode> nodeMap = knowledgeGraphService.listNodesByIds(nodeIds)
                .stream()
                .collect(Collectors.toMap(KnowledgeNode::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        List<KnowledgeRelation> relations = knowledgeGraphService.listRelationsBetween(nodeIds);
        Map<Long, UserKnowledgeProgress> progressMap = progressMap(userId, nodeIds);
        Map<Long, Integer> taskProgress = maxTaskProgress(tasks);

        List<TopologyNodeVO> nodes = nodeMap.values().stream()
                .map(node -> toTopologyNode(plan.id(), node, relations, progressMap.get(node.id()), taskProgress.get(node.id())))
                .toList();
        List<TopologyEdgeVO> edges = relations.stream()
                .map(relation -> new TopologyEdgeVO(
                        String.valueOf(relation.sourceNodeId()),
                        String.valueOf(relation.targetNodeId()),
                        relation.relationType()
                ))
                .toList();
        return new TopologyVO(String.valueOf(plan.id()), nodes, edges);
    }

    @Override
    public ResourceNetworkVO resourceNetwork(AuthenticatedPrincipal principal, String planId, String nodeId) {
        Long userId = requireUserId(principal);
        StudyPlan plan = requirePlan(userId, planId);
        KnowledgeNode node = requireNodeInPlan(plan.id(), nodeId);
        List<StudyTask> tasks = studyRepository.listTasks(plan.id());
        Map<Long, UserKnowledgeProgress> progressMap = progressMap(userId, List.of(node.id()));
        Integer taskProgress = maxTaskProgress(tasks).get(node.id());
        int progress = nodeProgress(progressMap.get(node.id()), taskProgress);
        String status = nodeStatus(progressMap.get(node.id()), progress);

        List<ResourceCardVO> resources = resourceService.listResourcesByNode(userId, node.id(), "all", 50);
        List<KnowledgeRelation> aroundRelations = knowledgeGraphService.listRelationsAround(List.of(node.id()));
        Map<Long, KnowledgeNode> relatedNodeMap = relatedNodes(node, aroundRelations);

        List<ResourceNetworkCategoryVO> categories = buildCategories(resources, relatedNodeMap.size());
        List<ResourceNetworkResourceNodeVO> resourceNodes = resources.stream()
                .map(this::toResourceNetworkNode)
                .toList();
        List<RelatedKnowledgeNodeVO> relatedNodes = aroundRelations.stream()
                .map(relation -> toRelatedNode(plan.id(), node.id(), relation, relatedNodeMap))
                .filter(item -> item != null)
                .toList();

        List<ResourceNetworkEdgeVO> edges = new ArrayList<>();
        categories.forEach(category -> edges.add(new ResourceNetworkEdgeVO(String.valueOf(node.id()), category.id(), "category")));
        resourceNodes.forEach(resource -> edges.add(new ResourceNetworkEdgeVO(resource.categoryId(), resource.id(), "resource")));
        relatedNodes.forEach(related -> edges.add(new ResourceNetworkEdgeVO(String.valueOf(node.id()), related.id(), "related_knowledge")));

        return new ResourceNetworkVO(
                String.valueOf(plan.id()),
                String.valueOf(node.id()),
                new ResourceNetworkKnowledgeVO(
                        String.valueOf(node.id()),
                        node.nodeName(),
                        node.nodeCode(),
                        null,
                        status,
                        progress,
                        node.nodeDesc()
                ),
                new ResourceNetworkStatsVO(
                        resourceService.countLearnedResourcesByNode(userId, node.id()),
                        resourceService.countResourcesByNode(node.id()),
                        resourceService.sumStudyDurationByNode(userId, node.id()),
                        0
                ),
                categories,
                resourceNodes,
                relatedNodes,
                edges,
                suggestions(resources)
        );
    }

    @Override
    public PageResponse<ResourceCardVO> nodeResources(
            AuthenticatedPrincipal principal,
            String planId,
            String nodeId,
            String type,
            long page,
            long pageSize
    ) {
        Long userId = requireUserId(principal);
        StudyPlan plan = requirePlan(userId, planId);
        KnowledgeNode node = requireNodeInPlan(plan.id(), nodeId);
        return resourceService.listNodeResources(userId, node.id(), type, page, pageSize);
    }

    private LearningPlanVO toPlanVO(StudyPlan plan, List<StudyTask> tasks) {
        return new LearningPlanVO(
                String.valueOf(plan.id()),
                plan.planTitle(),
                plan.majorDomain(),
                estimatedHours(tasks, plan.startDate(), plan.endDate()),
                plan.progress(),
                plan.generatedBy() == null ? "strict" : plan.generatedBy().toLowerCase(),
                planStatus(plan.planStatus()),
                plan.startDate(),
                plan.endDate(),
                tasks.stream().map(this::toTaskVO).toList()
        );
    }

    private StudyTaskVO toTaskVO(StudyTask task) {
        return new StudyTaskVO(
                String.valueOf(task.id()),
                task.nodeId() == null ? null : String.valueOf(task.nodeId()),
                task.resourceId() == null ? null : String.valueOf(task.resourceId()),
                task.taskTitle(),
                task.taskType(),
                taskStatus(task.taskStatus()),
                task.progressPercent(),
                task.planStartTime(),
                task.planEndTime()
        );
    }

    private TopologyNodeVO toTopologyNode(
            Long planId,
            KnowledgeNode node,
            List<KnowledgeRelation> relations,
            UserKnowledgeProgress progress,
            Integer taskProgress
    ) {
        List<String> prerequisites = relations.stream()
                .filter(relation -> relation.targetNodeId().equals(node.id()))
                .map(relation -> String.valueOf(relation.sourceNodeId()))
                .toList();
        String nodeId = String.valueOf(node.id());
        return new TopologyNodeVO(
                nodeId,
                node.nodeName(),
                node.nodeDesc(),
                node.nodeCode(),
                nodeStatus(progress, nodeProgress(progress, taskProgress)),
                nodeProgress(progress, taskProgress),
                prerequisites,
                "/app/resource-network?planId=" + planId + "&nodeId=" + nodeId,
                "/api/v1/learning-plans/" + planId + "/topology/nodes/" + nodeId + "/resource-network",
                new ClickActionVO(
                        "navigate",
                        "knowledge_resource_network",
                        Map.of("planId", String.valueOf(planId), "nodeId", nodeId)
                )
        );
    }

    private ResourceNetworkResourceNodeVO toResourceNetworkNode(ResourceCardVO resource) {
        return new ResourceNetworkResourceNodeVO(
                resource.id(),
                categoryId(resource.type()),
                resource.type(),
                resource.title(),
                resource.description(),
                resource.duration(),
                resource.status(),
                resourceTarget(resource)
        );
    }

    private RelatedKnowledgeNodeVO toRelatedNode(
            Long planId,
            Long centerNodeId,
            KnowledgeRelation relation,
            Map<Long, KnowledgeNode> relatedNodeMap
    ) {
        Long relatedId = relation.sourceNodeId().equals(centerNodeId) ? relation.targetNodeId() : relation.sourceNodeId();
        KnowledgeNode related = relatedNodeMap.get(relatedId);
        if (related == null) {
            return null;
        }
        String relationName = relation.sourceNodeId().equals(centerNodeId)
                ? relation.relationType()
                : "reverse_" + relation.relationType();
        Map<String, Object> target = new LinkedHashMap<>();
        target.put("type", "knowledge_resource_network");
        target.put("planId", String.valueOf(planId));
        target.put("nodeId", String.valueOf(related.id()));
        target.put("url", "/app/resource-network?planId=" + planId + "&nodeId=" + related.id());
        return new RelatedKnowledgeNodeVO(String.valueOf(related.id()), related.nodeName(), relationName, target);
    }

    private List<ResourceNetworkCategoryVO> buildCategories(List<ResourceCardVO> resources, int relatedCount) {
        Map<String, Long> counts = resources.stream()
                .collect(Collectors.groupingBy(ResourceCardVO::type, LinkedHashMap::new, Collectors.counting()));
        List<String> order = List.of("video", "document", "exercise_set", "reading", "code_case", "mindmap");
        List<ResourceNetworkCategoryVO> categories = new ArrayList<>();
        for (String type : order) {
            long count = counts.getOrDefault(type, 0L);
            if (count > 0) {
                categories.add(new ResourceNetworkCategoryVO(categoryId(type), type, categoryName(type), count));
            }
        }
        if (relatedCount > 0) {
            categories.add(new ResourceNetworkCategoryVO("cat_related", "knowledge", "相关知识点", (long) relatedCount));
        }
        return categories;
    }

    private Map<Long, KnowledgeNode> relatedNodes(KnowledgeNode center, List<KnowledgeRelation> relations) {
        Set<Long> ids = relations.stream()
                .map(relation -> relation.sourceNodeId().equals(center.id()) ? relation.targetNodeId() : relation.sourceNodeId())
                .filter(id -> !id.equals(center.id()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return knowledgeGraphService.listNodesByIds(ids).stream()
                .collect(Collectors.toMap(KnowledgeNode::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<String, Object> resourceTarget(ResourceCardVO resource) {
        Map<String, Object> target = new LinkedHashMap<>();
        target.put("type", targetType(resource.type()));
        if ("exercise_set".equals(resource.type())) {
            target.put("exerciseSetId", resource.id());
        } else {
            target.put("resourceId", resource.id());
        }
        target.put("url", resource.detailRoute());
        return target;
    }

    private String targetType(String type) {
        return switch (type) {
            case "video" -> "video_detail";
            case "document" -> "document_detail";
            case "exercise_set" -> "exercise_detail";
            case "reading" -> "reading_detail";
            case "code_case" -> "code_case_detail";
            case "mindmap" -> "mindmap_detail";
            default -> "resource_detail";
        };
    }

    private List<String> suggestions(List<ResourceCardVO> resources) {
        if (resources.isEmpty()) {
            return List.of("先补充当前知识点的学习资源，再继续推进学习计划。");
        }
        return resources.stream()
                .sorted(Comparator.comparing(ResourceCardVO::progress).reversed())
                .limit(3)
                .map(resource -> "建议先学习《" + resource.title() + "》。")
                .toList();
    }

    private Map<Long, UserKnowledgeProgress> progressMap(Long userId, Collection<Long> nodeIds) {
        return studyRepository.listProgressByUserAndNodes(userId, nodeIds).stream()
                .collect(Collectors.toMap(UserKnowledgeProgress::nodeId, Function.identity()));
    }

    private Map<Long, Integer> maxTaskProgress(List<StudyTask> tasks) {
        return tasks.stream()
                .filter(task -> task.nodeId() != null)
                .collect(Collectors.toMap(
                        StudyTask::nodeId,
                        task -> task.progressPercent() == null ? 0 : task.progressPercent(),
                        Math::max
                ));
    }

    private StudyPlan requirePlan(Long userId, String planId) {
        Long parsedPlanId = requireId(planId, "学习计划ID格式不正确");
        return studyRepository.findPlanForUser(userId, parsedPlanId)
                .orElseThrow(() -> new BusinessException(404, "学习计划不存在"));
    }

    private KnowledgeNode requireNodeInPlan(Long planId, String nodeId) {
        KnowledgeNode node = knowledgeGraphService.findNode(nodeId)
                .orElseThrow(() -> new BusinessException(404, "知识点不存在"));
        if (!studyRepository.planContainsNode(planId, node.id())) {
            throw new BusinessException(404, "该知识点不在当前学习计划中");
        }
        return node;
    }

    private int nodeProgress(UserKnowledgeProgress progress, Integer taskProgress) {
        if (progress != null) {
            return clamp(progress.progressPercent() == null ? 0 : progress.progressPercent());
        }
        return clamp(taskProgress == null ? 0 : taskProgress);
    }

    private String nodeStatus(UserKnowledgeProgress progress, int percent) {
        if (percent >= 100) {
            return "completed";
        }
        if (progress == null || progress.progressStatus() == null) {
            return percent > 0 ? "in_progress" : "not_started";
        }
        return switch (progress.progressStatus()) {
            case "mastered" -> "completed";
            case "learning" -> "in_progress";
            case "review" -> "review";
            default -> "not_started";
        };
    }

    private int estimatedHours(List<StudyTask> tasks, LocalDate startDate, LocalDate endDate) {
        long minutes = tasks.stream()
                .filter(task -> task.planStartTime() != null && task.planEndTime() != null)
                .mapToLong(task -> Math.max(Duration.between(task.planStartTime(), task.planEndTime()).toMinutes(), 0))
                .sum();
        if (minutes > 0) {
            return (int) Math.max(1, Math.round(minutes / 60.0));
        }
        if (startDate != null && endDate != null) {
            return (int) Math.max(1, startDate.datesUntil(endDate.plusDays(1)).count());
        }
        return 0;
    }

    private int normalizeDailyMinutes(Integer dailyMinutes) {
        if (dailyMinutes == null) {
            return 60;
        }
        return Math.max(15, Math.min(dailyMinutes, 240));
    }

    private String planStatus(Integer status) {
        if (status == null) {
            return "pending";
        }
        return switch (status) {
            case 1 -> "running";
            case 2 -> "completed";
            case 3 -> "paused";
            default -> "pending";
        };
    }

    private String taskStatus(Integer status) {
        if (status == null) {
            return "todo";
        }
        return switch (status) {
            case 1 -> "learning";
            case 2 -> "completed";
            case 3 -> "overdue";
            default -> "todo";
        };
    }

    private String categoryId(String type) {
        return "cat_" + type;
    }

    private String categoryName(String type) {
        return switch (type) {
            case "video" -> "视频动画";
            case "document" -> "课程文档";
            case "exercise_set" -> "练习题";
            case "reading" -> "拓展阅读";
            case "code_case" -> "代码案例";
            case "mindmap" -> "思维导图";
            default -> "学习资源";
        };
    }

    private List<Long> parseTaskIds(List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }
        return taskIds.stream()
                .map(this::parseId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Optional<Long> parseId(String value) {
        String safeValue = value == null ? null : value.trim();
        if (safeValue == null || safeValue.isBlank()) {
            return Optional.empty();
        }
        String digits = safeValue.matches("\\d+") ? safeValue : safeValue.replaceFirst("^.*_(\\d+)$", "$1");
        if (!digits.matches("\\d+")) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(digits));
    }

    private Long requireId(String value, String message) {
        return parseId(value).orElseThrow(() -> new BusinessException(400, message));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(value, 100));
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private String firstText(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }
}
