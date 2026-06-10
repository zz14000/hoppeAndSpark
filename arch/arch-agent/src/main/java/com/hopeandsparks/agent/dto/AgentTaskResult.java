package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentName;

import java.util.List;
import java.util.Map;

public record AgentTaskResult(
        String taskId,
        AgentName sourceAgent,
        String status,
        String answerText,
        List<String> steps,
        String diagramScript,
        List<String> citations,
        boolean needAsyncGeneration,
        Map<String, Object> metadata,
        List<String> errors
) {
}
