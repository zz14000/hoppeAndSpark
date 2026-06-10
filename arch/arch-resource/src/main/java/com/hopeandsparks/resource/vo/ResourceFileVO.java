package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource file metadata")
public record ResourceFileVO(
        String fileId,
        String fileName,
        String fileType,
        String fileUrl,
        Long fileSize
) {
}
