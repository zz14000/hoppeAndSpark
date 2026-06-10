package com.hopeandsparks.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record AiDisputeCreateRequest(
        String sessionId,
        String messageId,
        @NotBlank(message = "争议内容不能为空")
        String reason,
        String detail
) {
}
