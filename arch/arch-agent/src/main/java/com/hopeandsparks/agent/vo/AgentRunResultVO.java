package com.hopeandsparks.agent.vo;

import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.infra.tool.ToolCallRecord;

import java.util.List;
import java.util.Map;

public record AgentRunResultVO(
        String reviewStatus,
        String answerText,
        String diagramScript,
        String diagramImagePath,
        List<String> citations,
        List<String> memoryWrites,
        List<String> cacheCandidates,
        List<AgentTaskResult> taskResults,
        ReviewDecision reviewDecision,
        List<ToolCallRecord> toolCalls,
        Map<String, Object> payload,
        boolean mock
) {
}
