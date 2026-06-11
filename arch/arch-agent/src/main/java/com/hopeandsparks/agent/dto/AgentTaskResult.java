package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentName;

import java.util.List;
import java.util.Map;

public record AgentTaskResult(
        String taskId,
        AgentName sourceAgent,
        String status,
        String answerText,
        String schemaVersion,
        Map<String, Object> structuredPayload,
        String renderedText,
        List<String> citations,
        boolean needAsyncGeneration,
        Map<String, Object> artifacts,
        List<String> toolCalls,
        List<String> issues,
        double confidence,
        String rawModelOutput
) {
}
