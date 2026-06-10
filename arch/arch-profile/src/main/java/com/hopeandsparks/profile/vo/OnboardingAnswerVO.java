package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "单轮画像回答响应")
public record OnboardingAnswerVO(
        @Schema(description = "画像引导会话ID")
        String sessionId,

        @Schema(description = "下一题，已完成时为空")
        OnboardingQuestionVO nextQuestion,

        @Schema(description = "是否已答完")
        boolean finished
) {
}
