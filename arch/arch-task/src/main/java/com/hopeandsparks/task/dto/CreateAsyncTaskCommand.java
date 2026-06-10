package com.hopeandsparks.task.dto;

import java.util.Map;

public record CreateAsyncTaskCommand(
        String taskId,
        String taskType,
        String idempotentKey,
        String documentId,
        String projectId,
        String title,
        Integer maxRetry,
        Map<String, Object> payload
) {
}
