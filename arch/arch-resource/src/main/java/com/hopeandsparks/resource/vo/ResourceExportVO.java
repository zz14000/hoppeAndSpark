package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource export result")
public record ResourceExportVO(
        String resourceId,
        String format,
        String status,
        String downloadUrl
) {
}
