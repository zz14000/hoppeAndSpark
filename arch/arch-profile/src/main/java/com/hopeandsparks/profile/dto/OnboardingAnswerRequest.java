package com.hopeandsparks.profile.dto;

import jakarta.validation.constraints.NotBlank;

public record OnboardingAnswerRequest(
        @NotBlank(message = "问题ID不能为空")
        String questionId,
        String answer
) {
}
