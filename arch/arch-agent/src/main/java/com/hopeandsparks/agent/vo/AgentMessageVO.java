package com.hopeandsparks.agent.vo;

import java.time.LocalDateTime;

public record AgentMessageVO(
        String id,
        String sessionId,
        String role,
        String content,
        LocalDateTime createdAt,
        boolean mock
) {
}
