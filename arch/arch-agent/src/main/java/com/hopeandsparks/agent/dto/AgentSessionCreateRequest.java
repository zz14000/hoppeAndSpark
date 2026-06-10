package com.hopeandsparks.agent.dto;

public record AgentSessionCreateRequest(
        String agentKey,
        String title,
        String projectId,
        String courseName
) {
}
