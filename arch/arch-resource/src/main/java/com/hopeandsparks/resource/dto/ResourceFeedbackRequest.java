package com.hopeandsparks.resource.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceFeedbackRequest(
        Integer score,
        @NotBlank(message = "反馈内容不能为空")
        String content
) {
}
