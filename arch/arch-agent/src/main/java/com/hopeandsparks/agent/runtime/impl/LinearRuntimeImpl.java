package com.hopeandsparks.agent.runtime.impl;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.orchestration.MemoryLoader;
import com.hopeandsparks.agent.orchestration.AgentGraphState;
import com.hopeandsparks.agent.orchestration.Aggregator;
import com.hopeandsparks.agent.orchestration.ContextNormalizer;
import com.hopeandsparks.agent.orchestration.Horizon;
import com.hopeandsparks.agent.orchestration.SparkEntry;
import com.hopeandsparks.agent.orchestration.TaskPlanner;
import com.hopeandsparks.agent.orchestration.TaskScheduler;
import com.hopeandsparks.agent.runtime.LinearRuntime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LinearRuntimeImpl implements LinearRuntime {

    private final SparkEntry sparkEntry;
    private final MemoryLoader memoryLoader;
    private final ContextNormalizer contextNormalizer;
    private final TaskPlanner taskPlanner;
    private final TaskScheduler taskScheduler;
    private final Aggregator aggregator;
    private final Horizon horizon;

    public LinearRuntimeImpl(
            SparkEntry sparkEntry,
            MemoryLoader memoryLoader,
            ContextNormalizer contextNormalizer,
            TaskPlanner taskPlanner,
            TaskScheduler taskScheduler,
            Aggregator aggregator,
            Horizon horizon
    ) {
        this.sparkEntry = sparkEntry;
        this.memoryLoader = memoryLoader;
        this.contextNormalizer = contextNormalizer;
        this.taskPlanner = taskPlanner;
        this.taskScheduler = taskScheduler;
        this.aggregator = aggregator;
        this.horizon = horizon;
    }

    @Override
    public AgentGraphState run(AgentGraphState initialState) {
        return execute(initialState, initialState.review() != null && initialState.review().repairable());
    }

    public AgentGraphState execute(AgentGraphState initialState, boolean revisionRun) {
        var request = initialState.request();
        var intent = initialState.intent() == null ? sparkEntry.route(request) : initialState.intent();
        MemoryContext memory = initialState.memory() == null ? memoryLoader.load(request) : initialState.memory();
        RetrievalBundle retrieval = initialState.retrieval() == null ? contextNormalizer.ragContext(request) : initialState.retrieval();
        AgentExecutionPlan plan = initialState.plan() == null ? taskPlanner.plan(request, intent) : initialState.plan();
        List<AgentTask> tasks = initialState.tasks() == null || initialState.tasks().isEmpty() ? plan.taskList() : initialState.tasks();
        List<AgentTaskResult> specialistResults = taskScheduler.execute(
                request,
                tasks,
                memory,
                retrieval,
                initialState.specialistResults(),
                initialState.review() == null ? null : initialState.review().targetRevisionAgent()
        );
        String draft = aggregator.answer(plan, specialistResults, retrieval);
        Map<String, Object> artifacts = aggregator.artifacts(plan, specialistResults);
        ReviewDecision review = horizon.review(plan, draft, specialistResults, retrieval, artifacts);
        Map<String, Object> telemetry = Map.of(
                "runtime", "linear",
                "completed", true,
                "revisionRun", revisionRun
        );
        return new AgentGraphState(
                request,
                intent,
                List.of(intent.name().toLowerCase(), plan.outputMode().name().toLowerCase()),
                plan,
                tasks,
                specialistResults,
                review,
                memory,
                retrieval,
                draft,
                Map.of(
                        "target", review.revisionTarget().name(),
                        "agent", review.targetRevisionAgent() == null ? "" : review.targetRevisionAgent().name(),
                        "repairable", review.repairable()
                ),
                Map.of(
                        "retrievalWebUsed", retrieval.webSearchUsed(),
                        "candidateIds", retrieval.candidateIds()
                ),
                mergeArtifacts(artifacts, request.allowWebSearch()),
                telemetry,
                initialState.maxRevisionCount(),
                revisionRun ? initialState.currentRevisionCount() + 1 : initialState.currentRevisionCount(),
                initialState.stateVersion() + 1,
                Map.of("executor", "LinearRuntime")
        );
    }

    private Map<String, Object> mergeArtifacts(Map<String, Object> artifacts, boolean allowWebSearch) {
        java.util.Map<String, Object> merged = new java.util.LinkedHashMap<>(artifacts);
        merged.put("allowWebSearch", allowWebSearch);
        return Map.copyOf(merged);
    }
}
