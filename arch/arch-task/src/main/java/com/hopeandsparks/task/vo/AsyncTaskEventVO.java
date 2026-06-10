package com.hopeandsparks.task.vo;

import java.time.LocalDateTime;

public record AsyncTaskEventVO(
        String eventId,
        String taskId,
        String documentId,
        String stage,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        long durationMs,
        String inputSummary,
        String outputSummary,
        String errorCode,
        String errorMessage,
        int retryCount,
        LocalDateTime createdAt
) {
}
