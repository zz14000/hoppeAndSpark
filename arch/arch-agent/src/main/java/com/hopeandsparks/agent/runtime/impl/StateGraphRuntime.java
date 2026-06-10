package com.hopeandsparks.agent.runtime.impl;

import com.hopeandsparks.agent.orchestration.AgentGraphState;
import com.hopeandsparks.agent.runtime.GraphRuntime;
import com.hopeandsparks.agent.runtime.state.GraphAgentState;
import com.hopeandsparks.agent.enums.ReviewStatus;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class StateGraphRuntime implements GraphRuntime {

    private static final String STATE_KEY = GraphAgentState.BUSINESS_STATE;
    private final LinearRuntimeImpl linearRuntime;
    private final CompiledGraph<GraphAgentState> graph;

    public StateGraphRuntime(LinearRuntimeImpl linearRuntime) {
        this.linearRuntime = linearRuntime;
        this.graph = compileGraph();
    }

    @Override
    public AgentGraphState run(AgentGraphState initialState) {
        try {
            Optional<GraphAgentState> result = graph.invoke(Map.of(STATE_KEY, initialState));
            if (result.isPresent() && result.get().businessState() != null) {
                return result.get().businessState();
            }
            return initialState;
        } catch (Exception exception) {
            return linearRuntime.execute(initialState, false);
        }
    }

    private CompiledGraph<GraphAgentState> compileGraph() {
        try {
            StateGraph<GraphAgentState> stateGraph = new StateGraph<>(GraphAgentState::new);
            stateGraph
                    .addNode("SparkEntry", (AsyncNodeAction<GraphAgentState>) graphState -> completed(linearRuntime.execute(graphState.businessState(), false)))
                    .addNode("MemoryLoader", (AsyncNodeAction<GraphAgentState>) graphState -> completed(graphState.businessState()))
                    .addNode("ContextNormalizer", (AsyncNodeAction<GraphAgentState>) graphState -> completed(graphState.businessState()))
                    .addNode("TaskPlanner", (AsyncNodeAction<GraphAgentState>) graphState -> completed(graphState.businessState()))
                    .addNode("SpecialistExecutor", (AsyncNodeAction<GraphAgentState>) graphState -> completed(graphState.businessState()))
                    .addNode("Aggregator", (AsyncNodeAction<GraphAgentState>) graphState -> completed(graphState.businessState()))
                    .addNode("Horizon", (AsyncNodeAction<GraphAgentState>) graphState -> completed(graphState.businessState()))
                    .addNode("RevisionRouter", (AsyncNodeAction<GraphAgentState>) graphState -> completed(routeRevision(graphState.businessState())))
                    .addEdge(GraphDefinition.START, "SparkEntry")
                    .addEdge("SparkEntry", "MemoryLoader")
                    .addEdge("MemoryLoader", "ContextNormalizer")
                    .addEdge("ContextNormalizer", "TaskPlanner")
                    .addEdge("TaskPlanner", "SpecialistExecutor")
                    .addEdge("SpecialistExecutor", "Aggregator")
                    .addEdge("Aggregator", "Horizon")
                    .addConditionalEdges("Horizon", nextEdge(), Map.of(
                            "publish", GraphDefinition.END,
                            "block", GraphDefinition.END,
                            "revise", "RevisionRouter"
                    ))
                    .addConditionalEdges("RevisionRouter", revisionEdge(), Map.of(
                            "publish", GraphDefinition.END,
                            "retry", "SpecialistExecutor",
                            "aggregate", "Aggregator",
                            "block", GraphDefinition.END
                    ));
            return stateGraph.compile();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to compile agent StateGraph", exception);
        }
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
        return linearRuntime.execute(state, true);
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
