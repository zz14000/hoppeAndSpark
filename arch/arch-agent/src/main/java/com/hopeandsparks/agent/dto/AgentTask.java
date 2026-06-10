package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.OutputMode;
import com.hopeandsparks.agent.enums.TaskType;

import java.util.List;
import java.util.Map;

public record AgentTask(
        String taskId,
        TaskType taskType,
        AgentName targetAgent,
        String taskGoal,
        int priority,
        List<String> dependsOn,
        boolean requiresRag,
        boolean requiresDiagram,
        OutputMode outputMode,
        Map<String, Object> rawParams
) {
}
