package com.hopeandsparks.agent.dto;

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
        java.util.List<String> knowledgePointIds,
        boolean allowWebSearch,
        String strictnessLevel,
        boolean renderMermaid,
        Map<String, Object> pageContext
) {
}
