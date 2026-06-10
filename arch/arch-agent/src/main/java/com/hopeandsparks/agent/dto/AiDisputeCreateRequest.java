package com.hopeandsparks.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Schema(description = "AI 内容争议上报请求")
public record AiDisputeCreateRequest(
        @Schema(description = "争议目标类型，如 agent_message/resource/comment")
        @NotBlank(message = "争议目标类型不能为空")
        String targetType,

        @Schema(description = "争议目标ID")
        @NotBlank(message = "争议目标ID不能为空")
        String targetId,

        @Schema(description = "问题类型")
        String issueType,

        @Schema(description = "兼容旧字段：争议原因")
        String reason,

        @Schema(description = "问题描述")
        String description,

        @Schema(description = "证据材料")
        List<Map<String, Object>> evidence
) {
}
