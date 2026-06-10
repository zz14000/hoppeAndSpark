package com.hopeandsparks.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Schema(description = "Resource quality feedback request")
public record ResourceFeedbackRequest(
        @Schema(description = "Issue type")
        @NotBlank(message = "问题类型不能为空")
        String issueType,

        @Schema(description = "Rating from 1 to 5")
        @Min(1) @Max(5)
        Integer rating,

        @Schema(description = "Feedback content")
        @NotBlank(message = "反馈内容不能为空")
        String content,

        @Schema(description = "Evidence list")
        List<Map<String, Object>> evidence
) {
}
