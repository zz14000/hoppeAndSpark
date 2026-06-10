package com.hopeandsparks.agent.vo;

import java.time.LocalDateTime;

public record AgentSessionVO(
        String id,
        String agentKey,
        String title,
        String projectId,
        String courseName,
        LocalDateTime createdAt,
        boolean mock
) {
}
