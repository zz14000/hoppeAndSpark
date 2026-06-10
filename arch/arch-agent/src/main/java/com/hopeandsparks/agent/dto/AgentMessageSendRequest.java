package com.hopeandsparks.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentMessageSendRequest(
        @NotBlank(message = "消息不能为空")
        String content,
        String mode,
        Boolean renderMermaid
) {
}
