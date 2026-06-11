package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.config.AgentProperties;
import com.hopeandsparks.agent.dto.AgentCheckpointSnapshot;
import com.hopeandsparks.agent.dto.AgentRunDebugVO;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentStageEvent;
import com.hopeandsparks.agent.dto.FinalAnswerEnvelope;
import com.hopeandsparks.agent.dto.ResourceBundle;
import com.hopeandsparks.agent.enums.AgentCheckpointPolicy;
import com.hopeandsparks.agent.enums.AgentOutputFormat;
import com.hopeandsparks.agent.enums.AgentRetrievalMode;
import com.hopeandsparks.agent.enums.AgentRunStatus;
import com.hopeandsparks.agent.orchestration.AgentGraphState;
import com.hopeandsparks.agent.orchestration.Aggregator;
import com.hopeandsparks.agent.runtime.GraphRuntime;
import com.hopeandsparks.agent.runtime.LinearRuntime;
import com.hopeandsparks.agent.service.AgentCheckpointStore;
import com.hopeandsparks.agent.service.AgentMemoryService;
import com.hopeandsparks.agent.service.AgentOrchestrationService;
import com.hopeandsparks.agent.service.AgentRunEventStore;
import com.hopeandsparks.agent.vo.AgentRunResultVO;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AgentOrchestrationServiceImpl implements AgentOrchestrationService {

    private final LinearRuntime linearRuntime;
    private final GraphRuntime graphRuntime;
    private final AgentProperties agentProperties;
    private final AgentMemoryService agentMemoryService;
    private final AgentCheckpointStore checkpointStore;
    private final AgentRunEventStore eventStore;
    private final Aggregator aggregator;
    private final ToolRegistry toolRegistry;

    public AgentOrchestrationServiceImpl(
            LinearRuntime linearRuntime,
            GraphRuntime graphRuntime,
            AgentProperties agentProperties,
            AgentMemoryService agentMemoryService,
            AgentCheckpointStore checkpointStore,
            AgentRunEventStore eventStore,
            Aggregator aggregator,
            ToolRegistry toolRegistry
    ) {
        this.linearRuntime = linearRuntime;
        this.graphRuntime = graphRuntime;
        this.agentProperties = agentProperties;
        this.agentMemoryService = agentMemoryService;
        this.checkpointStore = checkpointStore;
        this.eventStore = eventStore;
        this.aggregator = aggregator;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public AgentRunResultVO run(AgentRunRequest request) {
        return executeRequest(request);
    }

    @Override
    public AgentRunResultVO resume(String runId, String checkpointId) {
        AgentGraphState checkpointState = resolveResumeState(runId, checkpointId)
                .orElseThrow(() -> new IllegalArgumentException("run not found for resume: " + runId));
        AgentRunRequest original = checkpointState.request();
        AgentRunRequest resumeRequest = new AgentRunRequest(
                original.requestId(),
                original.userId(),
                original.sessionId(),
                original.messageId(),
                original.userQuery(),
                original.agentMode(),
                original.outputPreference(),
                original.projectId(),
                original.courseId(),
                original.courseName(),
                original.knowledgePoint(),
                original.knowledgePointIds(),
                original.allowWebSearch(),
                original.strictnessLevel(),
                original.renderMermaid(),
                original.pageContext(),
                "resume",
                original.outputFormat() == null ? AgentOutputFormat.STRICT_JSON_WITH_TEXT : original.outputFormat(),
                original.retrievalMode() == null ? AgentRetrievalMode.KB_FIRST_CONTROLLED_WEB : original.retrievalMode(),
                original.checkpointPolicy() == null ? AgentCheckpointPolicy.AUTO : original.checkpointPolicy(),
                runId,
                checkpointId == null ? "" : checkpointId,
                original.requireCitations(),
                original.responseStyle(),
                original.maxContextChunks(),
                original.debugOptions()
        );
        AgentGraphState resumedState = new AgentGraphState(
                resumeRequest,
                checkpointState.intent(),
                checkpointState.subIntents(),
                checkpointState.plan(),
                checkpointState.tasks(),
                checkpointState.specialistResults(),
                checkpointState.review(),
                checkpointState.memory(),
                checkpointState.retrieval(),
                checkpointState.resourceBundle(),
                checkpointState.resourceDecision(),
                checkpointState.draft(),
                checkpointState.revision(),
                checkpointState.toolContext(),
                checkpointState.artifacts(),
                checkpointState.resourceContext(),
                checkpointState.resourceTelemetry(),
                checkpointState.telemetry(),
                checkpointState.maxRevisionCount(),
                checkpointState.currentRevisionCount(),
                checkpointState.stateVersion(),
                checkpointState.payload()
        );
        AgentGraphState finalState = useGraphRuntime()
                ? graphRuntime.resume(resumedState)
                : linearRuntime.resume(resumedState);
        return toResult(resumeRequest, finalState);
    }

    private AgentRunResultVO executeRequest(AgentRunRequest request) {
        AgentGraphState initialState = new AgentGraphState(
                request,
                null,
                List.of(),
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                "",
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Math.max(1, agentProperties.getGraph().getMaxRevisions()),
                0,
                0L,
                Map.of()
        );
        checkpointStore.saveRun(initialState, useGraphRuntime() ? "graph" : "linear", "START", AgentRunStatus.PENDING.name(), "", "");
        AgentGraphState finalState = useGraphRuntime()
                ? graphRuntime.run(initialState)
                : linearRuntime.run(initialState);
        return toResult(request, finalState);
    }

    private AgentRunResultVO toResult(AgentRunRequest request, AgentGraphState finalState) {
        List<String> memoryUpdates = agentMemoryService.persist(request, finalState.plan(), finalState.specialistResults(), finalState.review());
        List<AgentStageEvent> events = buildEvents(finalState, memoryUpdates);
        eventStore.save(request.requestId(), events);
        FinalAnswerEnvelope envelope = aggregator.envelope(finalState.plan(), finalState.specialistResults(), finalState.retrieval(), finalState.artifacts());
        List<AgentCheckpointSnapshot> checkpoints = checkpointStore.listCheckpoints(request.requestId());
        boolean mock = toolRegistry.recentCalls().stream()
                .anyMatch(call -> call.outputSummary() != null && call.outputSummary().contains("mock=true"));
        return new AgentRunResultVO(
                request.requestId(),
                finalState.stateVersion(),
                mapRunStatus(finalState),
                finalState.intent() == null ? "unknown" : finalState.intent().name().toLowerCase(),
                finalState.review().finalDecision().name().toLowerCase(),
                finalState.draft(),
                envelope.answerSummary(),
                listArtifact(finalState.artifacts(), "stepList"),
                String.valueOf(finalState.artifacts().getOrDefault("diagramScript", "")),
                String.valueOf(finalState.artifacts().getOrDefault("diagramImagePath", "")),
                finalState.retrieval() == null ? List.of() : finalState.retrieval().citations(),
                listArtifact(finalState.artifacts(), "learningPlan"),
                memoryUpdates,
                finalState.retrieval() == null ? List.of() : finalState.retrieval().candidateIds(),
                finalState.specialistResults(),
                finalState.review(),
                toolRegistry.recentCalls(),
                finalState.artifacts(),
                finalState.resourceBundle(),
                finalState.resourceDecision(),
                listArtifact(finalState.artifacts(), "resourceTrace"),
                mergeQualityFlags(finalState),
                finalState.retrieval(),
                checkpoints,
                envelope,
                new AgentRunDebugVO(
                        String.valueOf(finalState.payload().getOrDefault("executor", useGraphRuntime() ? "StateGraphRuntime" : "LinearRuntime")),
                        finalState.currentRevisionCount(),
                        finalState.retrieval() == null ? Map.of() : finalState.retrieval().retrievalDebug(),
                        finalState.toolContext()
                ),
                Map.of(
                        "publishable", "publish".equalsIgnoreCase(finalState.review().finalDecision().name()),
                        "runtime", finalState.payload().getOrDefault("executor", "LinearRuntime"),
                        "currentRevisionCount", finalState.currentRevisionCount()
                ),
                mock
        );
    }

    public List<AgentStreamEventVO> streamEvents(String runId) {
        return eventStore.list(runId).stream()
                .map(event -> new AgentStreamEventVO(
                        event.stage(),
                        runId,
                        event.summary(),
                        Map.of(
                                "runId", event.runId(),
                                "stateVersion", event.stateVersion(),
                                "stage", event.stage(),
                                "payload", event.payload()
                        ),
                        event.mock()
                ))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<String> listArtifact(Map<String, Object> artifacts, String key) {
        Object value = artifacts.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private boolean useGraphRuntime() {
        return "graph".equalsIgnoreCase(agentProperties.getRuntime());
    }

    private List<String> mergeQualityFlags(AgentGraphState state) {
        List<String> flags = new ArrayList<>();
        if (state.review() != null && state.review().qualityFlags() != null) {
            flags.addAll(state.review().qualityFlags());
        }
        if (state.retrieval() != null && state.retrieval().retrievalQualityFlags() != null) {
            flags.addAll(state.retrieval().retrievalQualityFlags());
        }
        if (state.resourceBundle() != null && state.resourceBundle().qualityFlags() != null) {
            flags.addAll(state.resourceBundle().qualityFlags());
        }
        return flags.stream().distinct().toList();
    }

    private List<AgentStageEvent> buildEvents(AgentGraphState finalState, List<String> memoryUpdates) {
        String runId = finalState.request().requestId();
        long stateVersion = finalState.stateVersion();
        List<AgentStageEvent> events = new ArrayList<>();
        events.add(new AgentStageEvent(runId, stateVersion, "plan", "execution plan ready", Map.of(
                "intent", finalState.intent() == null ? "unknown" : finalState.intent().name(),
                "tasks", finalState.tasks().stream().map(task -> task.targetAgent().name()).toList()
        ), false));
        events.add(new AgentStageEvent(runId, stateVersion, "memory", "memory loaded and updated", Map.of(
                "sessionSummary", finalState.memory() == null ? "" : finalState.memory().sessionSummary(),
                "projectSummary", finalState.memory() == null ? "" : finalState.memory().projectSummary(),
                "updates", memoryUpdates
        ), false));
        events.add(new AgentStageEvent(runId, stateVersion, "retrieval", "retrieval bundle prepared", Map.of(
                "citations", finalState.retrieval() == null ? List.of() : finalState.retrieval().citations(),
                "candidateIds", finalState.retrieval() == null ? List.of() : finalState.retrieval().candidateIds(),
                "webSearchUsed", finalState.retrieval() != null && finalState.retrieval().webSearchUsed(),
                "nodeName", "ContextNormalizer"
        ), false));
        if (finalState.plan() != null && finalState.plan().requiresResources()) {
            events.add(new AgentStageEvent(runId, stateVersion, "resource_plan", "resource plan prepared", Map.of(
                    "resourceGoals", finalState.plan().resourceGoals(),
                    "preferredResourceTypes", finalState.plan().preferredResourceTypes(),
                    "nodeName", "TaskPlanner"
            ), false));
        }
        events.add(new AgentStageEvent(runId, stateVersion, "specialist", "specialist tasks completed", Map.of(
                "agents", finalState.specialistResults().stream().map(result -> result.sourceAgent().name()).toList(),
                "issues", finalState.specialistResults().stream().flatMap(result -> result.issues().stream()).distinct().toList()
        ), false));
        if (finalState.resourceBundle() != null) {
            events.add(new AgentStageEvent(runId, stateVersion, "resource_generate", "resource bundle prepared", Map.of(
                    "videoCount", finalState.resourceBundle().videoResources().size(),
                    "referenceCount", finalState.resourceBundle().referenceResources().size(),
                    "practiceCount", finalState.resourceBundle().practiceResources().size(),
                    "resourceDecision", finalState.resourceDecision() == null ? "" : finalState.resourceDecision().selectionReason(),
                    "nodeName", "ResourceExecutor"
            ), false));
        }
        if (!String.valueOf(finalState.artifacts().getOrDefault("diagramScript", "")).isBlank()) {
            events.add(new AgentStageEvent(runId, stateVersion, "diagram", "diagram artifact prepared", Map.of(
                    "diagramScript", finalState.artifacts().getOrDefault("diagramScript", ""),
                    "diagramImagePath", finalState.artifacts().getOrDefault("diagramImagePath", "")
            ), false));
        }
        events.add(new AgentStageEvent(runId, stateVersion, "review", "review completed", Map.of(
                "decision", finalState.review().finalDecision().name(),
                "qualityFlags", finalState.review().qualityFlags(),
                "revisionTarget", finalState.review().revisionTarget().name()
        ), false));
        if (finalState.review() != null && finalState.review().resourceIssues() != null && !finalState.review().resourceIssues().isEmpty()) {
            events.add(new AgentStageEvent(runId, stateVersion, "resource_review", "resource review completed", Map.of(
                    "resourceIssues", finalState.review().resourceIssues(),
                    "resourceRevisionTarget", finalState.review().resourceRevisionTarget().name(),
                    "nodeName", "Horizon"
            ), false));
        }
        events.add(new AgentStageEvent(runId, stateVersion, "done", "agent run completed", Map.of(
                "finalAnswer", finalState.draft(),
                "artifacts", finalState.artifacts(),
                "nodeName", "Horizon"
        ), false));
        return events;
    }

    private String mapRunStatus(AgentGraphState state) {
        return switch (state.review().finalDecision()) {
            case PUBLISH -> AgentRunStatus.SUCCESS.name();
            case BLOCK -> AgentRunStatus.BLOCKED.name();
            case REVISE -> AgentRunStatus.REVISING.name();
        };
    }

    private Optional<AgentGraphState> resolveResumeState(String runId, String checkpointId) {
        if (checkpointId != null && !checkpointId.isBlank()) {
            return checkpointStore.loadCheckpointState(checkpointId);
        }
        return checkpointStore.loadLatestState(runId);
    }
}
