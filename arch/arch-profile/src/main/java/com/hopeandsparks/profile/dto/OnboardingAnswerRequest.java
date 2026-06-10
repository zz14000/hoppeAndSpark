package com.hopeandsparks.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "单轮画像回答请求")
public record OnboardingAnswerRequest(
        @Schema(description = "画像引导会话ID，可不传")
        String sessionId,

        @Schema(description = "问题ID", example = "learning_domain")
        @NotBlank(message = "questionId不能为空")
        String questionId,

        @Schema(description = "通用答案")
        Object answer,

        @Schema(description = "文本答案")
        String answerText,

        @Schema(description = "选择题选项")
        List<String> selectedOptions
) {
}
