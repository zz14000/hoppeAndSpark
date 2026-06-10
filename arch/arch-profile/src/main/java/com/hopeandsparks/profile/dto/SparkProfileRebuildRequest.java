package com.hopeandsparks.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "重新构建 Spark 画像请求")
public record SparkProfileRebuildRequest(
        @Schema(description = "重建原因")
        @NotBlank(message = "reason不能为空")
        String reason,

        @Schema(description = "是否保留历史记忆")
        Boolean keepHistory,

        @Schema(description = "可选，新的画像回答")
        List<OnboardingAnswerRequest> answers
) {
}
