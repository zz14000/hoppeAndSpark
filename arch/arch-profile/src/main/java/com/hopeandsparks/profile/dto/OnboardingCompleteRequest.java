package com.hopeandsparks.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "完成画像引导请求")
public record OnboardingCompleteRequest(
        @Schema(description = "画像引导会话ID")
        String sessionId,

        @Schema(description = "可选，批量提交的回答")
        List<OnboardingAnswerRequest> answers
) {
}
