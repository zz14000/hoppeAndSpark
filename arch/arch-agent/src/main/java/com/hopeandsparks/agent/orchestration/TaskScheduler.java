package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.agent.SpecialistAgent;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component("agentTaskScheduler")
public class TaskScheduler {

    private final Map<AgentName, SpecialistAgent> agents = new EnumMap<>(AgentName.class);

    public TaskScheduler(List<SpecialistAgent> specialists) {
        for (SpecialistAgent specialist : specialists) {
            agents.put(specialist.name(), specialist);
        }
    }

    public List<AgentTaskResult> execute(AgentRunRequest request, List<AgentTask> tasks, Map<String, Object> context) {
        return tasks.stream()
                .map(task -> agents.get(task.targetAgent()).execute(request, task, context))
                .toList();
    }
}
