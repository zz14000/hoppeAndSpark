package com.hopeandsparks.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "智能体消息响应")
public record AgentMessageVO(
        String messageId,
        String sessionId,
        String role,
        String contentType,
        String content,
        String agentId,
        List<Map<String, Object>> attachments,
        String externalMessageId,
        boolean mock,
        LocalDateTime createdAt
) {
}
