package com.hopeandsparks.agent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeandsparks.agent.config.AgentProperties;
import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.enums.ReviewStatus;
import com.hopeandsparks.agent.repository.AgentMemorySnapshotRecord;
import com.hopeandsparks.agent.repository.AgentMemorySnapshotRepository;
import com.hopeandsparks.agent.repository.AgentProjectMemoryRecord;
import com.hopeandsparks.agent.repository.AgentProjectMemoryRepository;
import com.hopeandsparks.agent.repository.AgentSessionMemoryRecord;
import com.hopeandsparks.agent.repository.AgentSessionMemoryRepository;
import com.hopeandsparks.agent.service.AgentMemoryService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentMemoryServiceImpl implements AgentMemoryService {

    private static final String SESSION_KEY_PREFIX = "agent:l1:session:";

    private final StringRedisTemplate redisTemplate;
    private final AgentProperties agentProperties;
    private final AgentSessionMemoryRepository sessionMemoryRepository;
    private final AgentProjectMemoryRepository projectMemoryRepository;
    private final AgentMemorySnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, String> fallbackStore = new ConcurrentHashMap<>();

    public AgentMemoryServiceImpl(
            StringRedisTemplate redisTemplate,
            AgentProperties agentProperties,
            AgentSessionMemoryRepository sessionMemoryRepository,
            AgentProjectMemoryRepository projectMemoryRepository,
            AgentMemorySnapshotRepository snapshotRepository,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.agentProperties = agentProperties;
        this.sessionMemoryRepository = sessionMemoryRepository;
        this.projectMemoryRepository = projectMemoryRepository;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public MemoryContext load(AgentRunRequest request) {
        Optional<AgentSessionMemoryRecord> sessionRecord = sessionMemoryRepository.findBySessionId(safe(request.sessionId()));
        String recentSummary = loadL1(request.sessionId()).orElseGet(() -> sessionRecord.map(AgentSessionMemoryRecord::recentSummary).orElse(""));
        Map<String, Object> sessionState = new LinkedHashMap<>();
        sessionRecord.ifPresent(record -> {
            sessionState.put("lastPlanJson", safe(record.lastPlanJson()));
            sessionState.put("unfinishedTaskJson", safe(record.unfinishedTaskJson()));
        });
        List<AgentProjectMemoryRecord> projectRecords = projectMemoryRepository.listByProjectAndPoints(
                safe(request.projectId()),
                safe(request.userId()),
                request.knowledgePointIds() == null ? List.of() : request.knowledgePointIds()
        );
        String projectSummary = projectRecords.stream()
                .map(item -> item.knowledgePoint() + ": mastery=" + item.masteryLevel())
                .reduce((left, right) -> left + "; " + right)
                .orElse("");
        Map<String, Object> projectState = new LinkedHashMap<>();
        projectState.put("courseId", safe(request.courseId()));
        projectState.put("courseName", safe(request.courseName()));
        projectState.put("knowledgePoints", projectRecords.stream().map(AgentProjectMemoryRecord::knowledgePoint).toList());
        projectState.put("weaknessTags", projectRecords.stream().map(AgentProjectMemoryRecord::weaknessTagsJson).filter(value -> value != null && !value.isBlank()).toList());
        return new MemoryContext(recentSummary, sessionState, projectSummary, projectState, List.of());
    }

    @Override
    public List<String> persist(AgentRunRequest request, AgentExecutionPlan plan, List<AgentTaskResult> results, ReviewDecision reviewDecision) {
        List<String> updates = new ArrayList<>();
        String l1Summary = summarizeForL1(request, results, reviewDecision);
        writeL1(request.sessionId(), l1Summary);
        String lastPlanJson = toJson(plan == null ? Map.of() : Map.of(
                "goal", safe(plan.primaryGoal()),
                "mustProduce", plan.mustProduce(),
                "outputMode", plan.outputMode().name()
        ));
        String unfinishedTaskJson = toJson(results.stream()
                .filter(result -> result.issues() != null && !result.issues().isEmpty())
                .map(result -> Map.of("taskId", result.taskId(), "issues", result.issues()))
                .toList());
        sessionMemoryRepository.save(new AgentSessionMemoryRecord(
                safe(request.sessionId()),
                safe(request.userId()),
                safe(request.projectId()),
                l1Summary,
                lastPlanJson,
                unfinishedTaskJson,
                LocalDateTime.now()
        ));
        snapshotRepository.save(new AgentMemorySnapshotRecord(
                safe(request.requestId()),
                safe(request.sessionId()),
                safe(request.userId()),
                safe(request.projectId()),
                safe(request.courseId()),
                safe(request.knowledgePoint()),
                "L1",
                toJson(Map.of(
                        "recentSummary", l1Summary,
                        "lastPlanJson", lastPlanJson,
                        "unfinishedTaskJson", unfinishedTaskJson
                )),
                LocalDateTime.now()
        ));
        updates.add("L1: recent session summary updated");
        if (reviewDecision != null && reviewDecision.finalDecision() == ReviewStatus.PUBLISH && agentProperties.getMemory().getL2().isEnabled()) {
            List<String> points = request.knowledgePointIds() == null || request.knowledgePointIds().isEmpty()
                    ? List.of(safe(request.knowledgePoint()))
                    : request.knowledgePointIds();
            String learningPlanJson = toJson(extractLearningPlan(results));
            String weaknessTagsJson = toJson(extractWeaknessTags(results));
            for (String point : points.stream().filter(value -> value != null && !value.isBlank()).toList()) {
                projectMemoryRepository.save(new AgentProjectMemoryRecord(
                        safe(request.projectId()),
                        safe(request.userId()),
                        safe(request.courseId()),
                        safe(request.courseName()),
                        point,
                        inferMasteryLevel(results),
                        weaknessTagsJson,
                        learningPlanJson,
                        LocalDateTime.now()
                ));
                snapshotRepository.save(new AgentMemorySnapshotRecord(
                        safe(request.requestId()),
                        safe(request.sessionId()),
                        safe(request.userId()),
                        safe(request.projectId()),
                        safe(request.courseId()),
                        point,
                        "L2",
                        toJson(Map.of(
                                "knowledgePoint", point,
                                "masteryLevel", inferMasteryLevel(results),
                                "weaknessTags", extractWeaknessTags(results),
                                "learningPlan", extractLearningPlan(results)
                        )),
                        LocalDateTime.now()
                ));
            }
            updates.add("L2: knowledge-point memory updated");
        }
        return updates;
    }

    private Optional<String> loadL1(String sessionId) {
        String key = SESSION_KEY_PREFIX + safe(sessionId);
        if (fallbackStore.containsKey(key)) {
            return Optional.ofNullable(fallbackStore.get(key));
        }
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private void writeL1(String sessionId, String summary) {
        String key = SESSION_KEY_PREFIX + safe(sessionId);
        fallbackStore.put(key, summary);
        try {
            redisTemplate.opsForValue().set(key, summary, Duration.ofMinutes(Math.max(5L, agentProperties.getMemory().getL1().getTtlMinutes())));
        } catch (RuntimeException exception) {
            // Redis unavailable in local env; keep fallback memory store active.
        }
    }

    private String summarizeForL1(AgentRunRequest request, List<AgentTaskResult> results, ReviewDecision reviewDecision) {
        String answerSummary = results.stream()
                .map(AgentTaskResult::answerText)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .map(text -> text.length() > 160 ? text.substring(0, 160) + "..." : text)
                .orElse(safe(request.userQuery()));
        return "mode=" + safe(request.agentMode()) +
                ", review=" + (reviewDecision == null ? "unknown" : reviewDecision.finalDecision().name()) +
                ", summary=" + answerSummary;
    }

    private List<String> extractLearningPlan(List<AgentTaskResult> results) {
        return results.stream()
                .map(AgentTaskResult::artifacts)
                .map(artifacts -> artifacts.get("learningPlan"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .toList();
    }

    private List<String> extractWeaknessTags(List<AgentTaskResult> results) {
        return results.stream()
                .map(AgentTaskResult::structuredPayload)
                .map(payload -> payload.get("commonMistakes"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .distinct()
                .toList();
    }

    private int inferMasteryLevel(List<AgentTaskResult> results) {
        long issueCount = results.stream().map(AgentTaskResult::issues).filter(List.class::isInstance).mapToLong(List::size).sum();
        return (int) Math.max(20, 85 - issueCount * 10);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
