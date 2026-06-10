package com.hopeandsparks.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "收藏操作请求")
public record CollectionToggleRequest(
        @Schema(description = "目标类型：resource/article")
        @NotBlank(message = "targetType不能为空")
        String targetType,

        @Schema(description = "目标ID")
        @NotBlank(message = "targetId不能为空")
        String targetId,

        @Schema(description = "动作：collect/uncollect/toggle")
        String action
) {
}
