package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.agent.SpecialistAgent;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.service.AgentOutputValidator;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("agentTaskScheduler")
public class TaskScheduler {

    private final Map<AgentName, SpecialistAgent> agents = new EnumMap<>(AgentName.class);
    private final AgentOutputValidator outputValidator;

    public TaskScheduler(List<SpecialistAgent> specialists, AgentOutputValidator outputValidator) {
        for (SpecialistAgent specialist : specialists) {
            agents.put(specialist.name(), specialist);
        }
        this.outputValidator = outputValidator;
    }

    public List<AgentTaskResult> execute(
            AgentRunRequest request,
            List<AgentTask> tasks,
            MemoryContext memory,
            RetrievalBundle retrieval,
            List<AgentTaskResult> previousResults,
            AgentName revisionAgent
    ) {
        Map<AgentName, AgentTaskResult> historyByAgent = new EnumMap<>(AgentName.class);
        if (previousResults != null) {
            previousResults.forEach(result -> historyByAgent.put(result.sourceAgent(), result));
        }
        Map<String, AgentTaskResult> completedByTaskId = new LinkedHashMap<>();
        List<AgentTaskResult> results = new java.util.ArrayList<>();
        for (AgentTask task : tasks) {
            if (!dependenciesSatisfied(task, completedByTaskId)) {
                continue;
            }
            if (revisionAgent != null && task.targetAgent() != revisionAgent && historyByAgent.containsKey(task.targetAgent())) {
                AgentTaskResult existing = historyByAgent.get(task.targetAgent());
                completedByTaskId.put(task.taskId(), existing);
                results.add(existing);
                continue;
            }
            Map<String, Object> context = Map.of(
                    "memory", memory,
                    "retrieval", retrieval,
                    "priorResults", List.copyOf(results),
                    "historyByAgent", historyByAgent
            );
            AgentTaskResult result = agents.get(task.targetAgent()).execute(request, task, context);
            outputValidator.validate(result);
            completedByTaskId.put(task.taskId(), result);
            historyByAgent.put(task.targetAgent(), result);
            results.add(result);
        }
        return results;
    }

    public List<AgentTaskResult> execute(AgentRunRequest request, List<AgentTask> tasks, MemoryContext memory, RetrievalBundle retrieval) {
        return execute(request, tasks, memory, retrieval, List.of(), null);
    }

    private boolean dependenciesSatisfied(AgentTask task, Map<String, AgentTaskResult> completedByTaskId) {
        if (task.dependsOn() == null || task.dependsOn().isEmpty()) {
            return true;
        }
        return task.dependsOn().stream().allMatch(completedByTaskId::containsKey);
    }
}
