package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.enums.AgentIntent;
import com.hopeandsparks.agent.enums.ReviewStatus;
import com.hopeandsparks.agent.orchestration.Aggregator;
import com.hopeandsparks.agent.orchestration.ContextNormalizer;
import com.hopeandsparks.agent.orchestration.Horizon;
import com.hopeandsparks.agent.orchestration.SparkEntry;
import com.hopeandsparks.agent.orchestration.TaskPlanner;
import com.hopeandsparks.agent.orchestration.TaskScheduler;
import com.hopeandsparks.agent.service.AgentOrchestrationService;
import com.hopeandsparks.agent.service.KnowledgeCacheService;
import com.hopeandsparks.agent.vo.AgentRunResultVO;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgentOrchestrationServiceImpl implements AgentOrchestrationService {

    private final SparkEntry sparkEntry;
    private final TaskPlanner taskPlanner;
    private final ContextNormalizer contextNormalizer;
    private final TaskScheduler taskScheduler;
    private final Aggregator aggregator;
    private final Horizon horizon;
    private final KnowledgeCacheService knowledgeCacheService;
    private final ToolRegistry toolRegistry;

    public AgentOrchestrationServiceImpl(
            SparkEntry sparkEntry,
            TaskPlanner taskPlanner,
            ContextNormalizer contextNormalizer,
            TaskScheduler taskScheduler,
            Aggregator aggregator,
            Horizon horizon,
            KnowledgeCacheService knowledgeCacheService,
            ToolRegistry toolRegistry
    ) {
        this.sparkEntry = sparkEntry;
        this.taskPlanner = taskPlanner;
        this.contextNormalizer = contextNormalizer;
        this.taskScheduler = taskScheduler;
        this.aggregator = aggregator;
        this.horizon = horizon;
        this.knowledgeCacheService = knowledgeCacheService;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public AgentRunResultVO run(AgentRunRequest request) {
        AgentIntent intent = sparkEntry.route(request);
        Map<String, Object> context = Map.of(
                "memory", contextNormalizer.memoryContext(request),
                "rag", contextNormalizer.ragContext(request)
        );
        List<AgentTask> tasks = taskPlanner.plan(request, intent);
        List<AgentTaskResult> results = taskScheduler.execute(request, tasks, context);
        String answer = aggregator.answer(results);
        String diagram = aggregator.diagram(results);
        ReviewDecision review = horizon.review(answer);
        List<String> cacheCandidates = intent == AgentIntent.RAG
                ? knowledgeCacheService.cacheCandidates(request.userId(), request.projectId(), request.userQuery())
                : List.of();
        String diagramImagePath = results.stream()
                .map(result -> result.metadata().get("diagramImagePath"))
                .filter(value -> value != null && !String.valueOf(value).isBlank())
                .map(String::valueOf)
                .findFirst()
                .orElse("");
        boolean mock = toolRegistry.recentCalls().stream()
                .anyMatch(call -> call.outputSummary() != null && call.outputSummary().contains("mock=true"));
        return new AgentRunResultVO(
                review.finalDecision().name().toLowerCase(),
                answer,
                diagram,
                diagramImagePath,
                results.stream().flatMap(result -> result.citations().stream()).distinct().toList(),
                List.of("mock memory write: " + intent.name().toLowerCase()),
                cacheCandidates,
                results,
                review,
                toolRegistry.recentCalls(),
                Map.of("intent", intent.name(), "publishable", review.finalDecision() == ReviewStatus.PUBLISH),
                mock
        );
    }
}
