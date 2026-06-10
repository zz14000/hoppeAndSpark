package com.hopeandsparks.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "智能体会话响应")
public record AgentSessionVO(
        String sessionId,
        String agentId,
        String title,
        String source,
        String contextNodeId,
        String contextResourceId,
        String status,
        String externalConversationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
