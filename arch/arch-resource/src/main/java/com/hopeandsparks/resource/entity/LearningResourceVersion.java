package com.hopeandsparks.resource.entity;

import java.time.LocalDateTime;

/**
 * Version record for generated resources.
 */
public record LearningResourceVersion(
        Long id,
        Long resourceId,
        Integer versionNo,
        Long contentFileId,
        String changeSummary,
        Integer horizonCheckStatus,
        LocalDateTime createdAt
) {
}
