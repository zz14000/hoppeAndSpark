package com.hopeandsparks.agent.vo;

import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.infra.tool.ToolCallRecord;

import java.util.List;
import java.util.Map;

public record AgentRunResultVO(
        String runId,
        long stateVersion,
        String intent,
        String reviewStatus,
        String finalAnswer,
        List<String> stepList,
        String diagramScript,
        String diagramImagePath,
        List<String> citations,
        List<String> learningPlan,
        List<String> memoryUpdates,
        List<String> cacheCandidates,
        List<AgentTaskResult> taskResults,
        ReviewDecision reviewDecision,
        List<ToolCallRecord> toolTrace,
        Map<String, Object> artifacts,
        List<String> qualityFlags,
        Map<String, Object> payload,
        boolean mock
) {
}
