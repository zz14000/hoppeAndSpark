package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resource version")
public record ResourceVersionVO(
        String id,
        Integer versionNo,
        String fileId,
        String changeSummary,
        String horizonStatus,
        LocalDateTime createdAt
) {
}
