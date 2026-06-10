package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentStageEvent;
import com.hopeandsparks.agent.config.AgentProperties;
import com.hopeandsparks.agent.orchestration.AgentGraphState;
import com.hopeandsparks.agent.runtime.GraphRuntime;
import com.hopeandsparks.agent.runtime.LinearRuntime;
import com.hopeandsparks.agent.service.AgentMemoryService;
import com.hopeandsparks.agent.service.AgentOrchestrationService;
import com.hopeandsparks.agent.service.AgentRunEventStore;
import com.hopeandsparks.agent.service.KnowledgeCacheService;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;
import com.hopeandsparks.agent.vo.AgentRunResultVO;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
public class AgentOrchestrationServiceImpl implements AgentOrchestrationService {

    private final LinearRuntime linearRuntime;
    private final GraphRuntime graphRuntime;
    private final AgentProperties agentProperties;
    private final AgentMemoryService agentMemoryService;
    private final AgentRunEventStore eventStore;
    private final KnowledgeCacheService knowledgeCacheService;
    private final ToolRegistry toolRegistry;

    public AgentOrchestrationServiceImpl(
            LinearRuntime linearRuntime,
            GraphRuntime graphRuntime,
            AgentProperties agentProperties,
            AgentMemoryService agentMemoryService,
            AgentRunEventStore eventStore,
            KnowledgeCacheService knowledgeCacheService,
            ToolRegistry toolRegistry
    ) {
        this.linearRuntime = linearRuntime;
        this.graphRuntime = graphRuntime;
        this.agentProperties = agentProperties;
        this.agentMemoryService = agentMemoryService;
        this.eventStore = eventStore;
        this.knowledgeCacheService = knowledgeCacheService;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public AgentRunResultVO run(AgentRunRequest request) {
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
                "",
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Math.max(1, agentProperties.getGraph().getMaxRevisions()),
                0,
                0L,
                Map.of()
        );
        AgentGraphState finalState = useGraphRuntime()
                ? graphRuntime.run(initialState)
                : linearRuntime.run(initialState);
        List<String> memoryUpdates = agentMemoryService.persist(request, finalState.plan(), finalState.specialistResults(), finalState.review());
        List<AgentStageEvent> events = buildEvents(finalState, memoryUpdates);
        eventStore.save(request.requestId(), events);
        boolean mock = toolRegistry.recentCalls().stream()
                .anyMatch(call -> call.outputSummary() != null && call.outputSummary().contains("mock=true"));
        return new AgentRunResultVO(
                request.requestId(),
                finalState.stateVersion(),
                finalState.intent() == null ? "unknown" : finalState.intent().name().toLowerCase(),
                finalState.review().finalDecision().name().toLowerCase(),
                finalState.draft(),
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
                mergeQualityFlags(finalState),
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
                "webSearchUsed", finalState.retrieval() != null && finalState.retrieval().webSearchUsed()
        ), false));
        events.add(new AgentStageEvent(runId, stateVersion, "specialist", "specialist tasks completed", Map.of(
                "agents", finalState.specialistResults().stream().map(result -> result.sourceAgent().name()).toList(),
                "issues", finalState.specialistResults().stream().flatMap(result -> result.issues().stream()).distinct().toList()
        ), false));
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
        events.add(new AgentStageEvent(runId, stateVersion, "done", "agent run completed", Map.of(
                "finalAnswer", finalState.draft(),
                "artifacts", finalState.artifacts()
        ), false));
        return events;
    }
}
