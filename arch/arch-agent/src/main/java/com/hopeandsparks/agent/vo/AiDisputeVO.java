package com.hopeandsparks.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "AI 内容争议响应")
public record AiDisputeVO(
        String disputeId,
        String targetType,
        String targetId,
        String issueType,
        String status,
        LocalDateTime createdAt
) {
}
