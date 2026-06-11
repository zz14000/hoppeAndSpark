package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.OutputMode;

import java.util.List;

public record AgentExecutionPlan(
        String primaryGoal,
        List<String> mustProduce,
        List<AgentTask> taskList,
        List<String> dependencies,
        boolean requiresDiagram,
        boolean requiresResources,
        boolean requiresRag,
        boolean requiresReview,
        OutputMode outputMode,
        List<String> resourceGoals,
        List<String> preferredResourceTypes,
        String resourceOutputMode
) {
}
