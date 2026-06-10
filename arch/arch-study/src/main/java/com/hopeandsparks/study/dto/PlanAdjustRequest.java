package com.hopeandsparks.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Adjust study plan request")
public record PlanAdjustRequest(
        @Schema(description = "Adjust reason")
        String reason,

        @Schema(description = "Adjust strategy")
        String strategy,

        @Schema(description = "Affected task IDs")
        List<String> taskIds
) {
}
