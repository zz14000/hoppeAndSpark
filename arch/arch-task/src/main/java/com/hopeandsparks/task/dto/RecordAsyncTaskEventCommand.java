package com.hopeandsparks.task.dto;

import java.time.LocalDateTime;

public record RecordAsyncTaskEventCommand(
        String eventId,
        String taskId,
        String documentId,
        String stage,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationMs,
        String inputSummary,
        String outputSummary,
        String errorCode,
        String errorMessage,
        Integer retryCount
) {
}
