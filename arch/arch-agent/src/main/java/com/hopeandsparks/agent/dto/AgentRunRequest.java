package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentCheckpointPolicy;
import com.hopeandsparks.agent.enums.AgentOutputFormat;
import com.hopeandsparks.agent.enums.AgentRetrievalMode;

import java.util.List;
import java.util.Map;

public record AgentRunRequest(
        String requestId,
        String userId,
        String sessionId,
        String messageId,
        String userQuery,
        String agentMode,
        String outputPreference,
        String projectId,
        String courseId,
        String courseName,
        String knowledgePoint,
        List<String> knowledgePointIds,
        boolean allowWebSearch,
        String strictnessLevel,
        boolean renderMermaid,
        Map<String, Object> pageContext,
        String runMode,
        AgentOutputFormat outputFormat,
        AgentRetrievalMode retrievalMode,
        AgentCheckpointPolicy checkpointPolicy,
        String resumeFromRunId,
        String resumeFromCheckpointId,
        boolean requireCitations,
        String responseStyle,
        int maxContextChunks,
        Map<String, Object> debugOptions
) {
}
