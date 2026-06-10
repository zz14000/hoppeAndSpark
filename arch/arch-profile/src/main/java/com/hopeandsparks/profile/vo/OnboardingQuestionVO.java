package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "画像引导问题")
public record OnboardingQuestionVO(
        @Schema(description = "问题ID")
        String id,

        @Schema(description = "问题文本")
        String question,

        @Schema(description = "题型")
        String type,

        @Schema(description = "选项")
        List<String> options
) {
}
