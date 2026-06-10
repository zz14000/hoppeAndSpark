package com.hopeandsparks.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource export request")
public record ResourceExportRequest(
        @Schema(description = "Export format, such as pdf or markdown")
        String format,

        @Schema(description = "Whether notes should be included")
        Boolean includeNotes,

        @Schema(description = "Whether mind map should be included")
        Boolean includeMindmap
) {
}
