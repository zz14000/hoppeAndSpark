package com.hopeandsparks.task.vo;

import java.time.LocalDateTime;

public record AsyncTaskVO(
        String taskId,
        String taskType,
        String idempotentKey,
        String documentId,
        String projectId,
        String title,
        String status,
        int progress,
        String message,
        String externalRunId,
        int retryCount,
        int maxRetry,
        String failureReason,
        String payloadJson,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
