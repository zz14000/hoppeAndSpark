package com.hopeandsparks.resource.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Learning resource row with optional knowledge node and file metadata.
 */
public record LearningResource(
        Long id,
        Long nodeId,
        String nodeCode,
        String nodeName,
        String title,
        String resourceType,
        String resourceLevel,
        String summary,
        String contentSourceType,
        Long currentFileId,
        String generatedBy,
        Integer generateStatus,
        Integer horizonCheckStatus,
        BigDecimal qualityScore,
        Integer currentVersionNo,
        String fileName,
        String fileType,
        String objectKey,
        Long fileSize,
        Integer durationSeconds,
        Integer progress,
        Boolean collected,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
