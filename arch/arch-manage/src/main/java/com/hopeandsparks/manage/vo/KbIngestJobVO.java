package com.hopeandsparks.manage.vo;

public record KbIngestJobVO(
        String taskId,
        String taskType,
        String status,
        int progress,
        String message,
        int retryCount,
        int maxRetry,
        String documentId,
        String title,
        String projectId
) {
}
