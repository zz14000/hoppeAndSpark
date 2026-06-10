package com.hopeandsparks.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Resource progress update request")
public record ResourceProgressUpdateRequest(
        @Schema(description = "Progress percent, preferred field")
        @Min(0) @Max(100)
        Integer progressPercent,

        @Schema(description = "Progress percent, compatible field")
        @Min(0) @Max(100)
        Integer progress,

        @Schema(description = "Video/audio position seconds")
        @Min(0)
        Integer positionSeconds,

        @Schema(description = "Compatible position seconds")
        @Min(0)
        Integer lastPosition,

        @Schema(description = "Current section")
        String currentSection,

        @Schema(description = "Whether resource is completed")
        Boolean completed,

        @Schema(description = "Client status")
        String status,

        @Schema(description = "Study duration seconds")
        @Min(0)
        Integer durationSeconds
) {
}
