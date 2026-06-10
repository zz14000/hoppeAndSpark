package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resource progress update result")
public record ResourceProgressVO(
        String resourceId,
        Integer progress,
        String status,
        String recordType,
        LocalDateTime learnedAt
) {
}
