package com.hopeandsparks.agent.runtime.impl;

import com.hopeandsparks.agent.enums.AgentRunStatus;
import com.hopeandsparks.agent.enums.ReviewStatus;
import com.hopeandsparks.agent.orchestration.AgentGraphState;
import com.hopeandsparks.agent.runtime.GraphRuntime;
import com.hopeandsparks.agent.runtime.state.GraphAgentState;
import com.hopeandsparks.agent.service.AgentCheckpointStore;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class StateGraphRuntime implements GraphRuntime {

    private static final String STATE_KEY = GraphAgentState.BUSINESS_STATE;
    private final LinearRuntimeImpl linearRuntime;
    private final AgentCheckpointStore checkpointStore;
    private final CompiledGraph<GraphAgentState> graph;

    public StateGraphRuntime(LinearRuntimeImpl linearRuntime, AgentCheckpointStore checkpointStore) {
        this.linearRuntime = linearRuntime;
        this.checkpointStore = checkpointStore;
        this.graph = compileGraph();
    }

    @Override
    public AgentGraphState run(AgentGraphState initialState) {
        try {
            checkpointStore.saveRun(initialState, "graph", "SparkEntry", AgentRunStatus.RUNNING.name(), "", "");
            Optional<GraphAgentState> result = graph.invoke(Map.of(STATE_KEY, initialState));
            if (result.isPresent() && result.get().businessState() != null) {
                return result.get().businessState();
            }
            return initialState;
        } catch (Exception exception) {
            return linearRuntime.execute(initialState, false);
        }
    }

    @Override
    public AgentGraphState resume(AgentGraphState resumedState) {
        AgentGraphState checkpointState;
        if (resumedState.request().resumeFromCheckpointId() != null && !resumedState.request().resumeFromCheckpointId().isBlank()) {
            checkpointState = checkpointStore.loadCheckpointState(resumedState.request().resumeFromCheckpointId()).orElse(resumedState);
        } else {
            checkpointState = checkpointStore.loadLatestState(resumedState.request().resumeFromRunId()).orElse(resumedState);
        }
        checkpointStore.saveRun(checkpointState, "graph", "RevisionRouter", AgentRunStatus.REVISING.name(), "", "");
        return run(checkpointState);
    }

    private CompiledGraph<GraphAgentState> compileGraph() {
        try {
            StateGraph<GraphAgentState> stateGraph = new StateGraph<>(GraphAgentState::new);
            stateGraph
                    .addNode("SparkEntry", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "SparkEntry"))
                    .addNode("MemoryLoader", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "MemoryLoader"))
                    .addNode("ContextNormalizer", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "ContextNormalizer"))
                    .addNode("TaskPlanner", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "TaskPlanner"))
                    .addNode("SpecialistExecutor", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "SpecialistExecutor"))
                    .addNode("ResourceExecutor", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "ResourceExecutor"))
                    .addNode("Aggregator", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "Aggregator"))
                    .addNode("Horizon", (AsyncNodeAction<GraphAgentState>) graphState -> checkpointed(graphState.businessState(), "Horizon"))
                    .addNode("RevisionRouter", (AsyncNodeAction<GraphAgentState>) graphState -> completed(routeRevision(graphState.businessState())))
                    .addEdge(GraphDefinition.START, "SparkEntry")
                    .addEdge("SparkEntry", "MemoryLoader")
                    .addEdge("MemoryLoader", "ContextNormalizer")
                    .addEdge("ContextNormalizer", "TaskPlanner")
                    .addEdge("TaskPlanner", "SpecialistExecutor")
                    .addEdge("SpecialistExecutor", "ResourceExecutor")
                    .addEdge("ResourceExecutor", "Aggregator")
                    .addEdge("Aggregator", "Horizon")
                    .addConditionalEdges("Horizon", nextEdge(), Map.of(
                            "publish", GraphDefinition.END,
                            "block", GraphDefinition.END,
                            "revise", "RevisionRouter"
                    ))
                    .addConditionalEdges("RevisionRouter", revisionEdge(), Map.of(
                            "publish", GraphDefinition.END,
                            "retry", "ResourceExecutor",
                            "aggregate", "Aggregator",
                            "block", GraphDefinition.END
                    ));
            return stateGraph.compile();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to compile agent StateGraph", exception);
        }
    }

    private CompletableFuture<Map<String, Object>> checkpointed(AgentGraphState state, String nodeName) {
        AgentGraphState progressed = advanceNodeState(state, nodeName);
        checkpointStore.saveCheckpoint(progressed, nodeName);
        return completed(progressed);
    }

    private CompletableFuture<Map<String, Object>> completed(AgentGraphState state) {
        return CompletableFuture.completedFuture(Map.of(STATE_KEY, state));
    }

    private AgentGraphState routeRevision(AgentGraphState state) {
        if (state.review() == null || state.review().finalDecision() != ReviewStatus.REVISE) {
            return state;
        }
        if (!state.review().repairable() || state.currentRevisionCount() >= state.maxRevisionCount()) {
            return state;
        }
        return linearRuntime.resume(state);
    }

    private AgentGraphState advanceNodeState(AgentGraphState state, String nodeName) {
        Map<String, Object> telemetry = new LinkedHashMap<>(state.telemetry());
        telemetry.put("currentNode", nodeName);
        Map<String, Object> payload = new LinkedHashMap<>(state.payload());
        payload.put("executor", "StateGraphRuntime");
        payload.put("nodeName", nodeName);
        List<String> subIntents = state.subIntents() == null ? List.of() : state.subIntents();
        return new AgentGraphState(
                state.request(),
                state.intent(),
                subIntents,
                state.plan(),
                state.tasks(),
                state.specialistResults(),
                state.review(),
                state.memory(),
                state.retrieval(),
                state.resourceBundle(),
                state.resourceDecision(),
                state.draft(),
                state.revision(),
                state.toolContext(),
                state.artifacts(),
                state.resourceContext(),
                state.resourceTelemetry(),
                Map.copyOf(telemetry),
                state.maxRevisionCount(),
                state.currentRevisionCount(),
                state.stateVersion() + 1,
                Map.copyOf(payload)
        );
    }

    private AsyncEdgeAction<GraphAgentState> nextEdge() {
        return graphState -> CompletableFuture.completedFuture(switch (graphState.businessState().review().finalDecision()) {
            case PUBLISH -> "publish";
            case BLOCK -> "block";
            case REVISE -> "revise";
        });
    }

    private AsyncEdgeAction<GraphAgentState> revisionEdge() {
        return graphState -> {
            AgentGraphState state = graphState.businessState();
            if (state.review() == null) {
                return CompletableFuture.completedFuture("block");
            }
            if (state.review().finalDecision() == ReviewStatus.PUBLISH) {
                return CompletableFuture.completedFuture("publish");
            }
            if (state.review().finalDecision() == ReviewStatus.BLOCK) {
                return CompletableFuture.completedFuture("block");
            }
            String next = switch (state.review().revisionTarget()) {
                case AGGREGATOR -> "aggregate";
                case SPECIALIST -> "retry";
                default -> "block";
            };
            return CompletableFuture.completedFuture(next);
        };
    }
}
