package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Learning resource list card")
public record ResourceCardVO(
        @Schema(description = "Resource ID")
        String id,

        @Schema(description = "Frontend resource type")
        String type,

        @Schema(description = "Resource title")
        String title,

        @Schema(description = "Short description")
        String description,

        @Schema(description = "Duration seconds")
        Integer duration,

        @Schema(description = "Study progress")
        Integer progress,

        @Schema(description = "Study status")
        String status,

        @Schema(description = "Quality verifier")
        String verifiedBy,

        @Schema(description = "Display tags")
        List<String> tags,

        @Schema(description = "Frontend route")
        String detailRoute,

        @Schema(description = "Detail API")
        String detailApi,

        @Schema(description = "Whether current user collected it")
        Boolean collected
) {
}
