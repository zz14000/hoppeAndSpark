package com.hopeandsparks.agent.dto;

import java.util.Map;

public record AgentRunRequest(
        String requestId,
        String userId,
        String sessionId,
        String messageId,
        String userQuery,
        String mode,
        String projectId,
        String courseName,
        String knowledgePoint,
        boolean renderMermaid,
        Map<String, Object> pageContext
) {
}
