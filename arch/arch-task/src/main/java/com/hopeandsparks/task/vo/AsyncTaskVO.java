package com.hopeandsparks.task.vo;

import com.hopeandsparks.task.enums.AsyncTaskStatus;

import java.time.LocalDateTime;

/**
 * 返回给前端或后台页面看的任务状态。
 * ID 用字符串返回，符合项目里“前端 ID 用字符串”的约定。
 */
public record AsyncTaskVO(
        String taskId,
        String taskType,
        String ownerType,
        String ownerId,
        AsyncTaskStatus status,
        int progress,
        String message,
        String failureReason,
        String externalRunId,
        int retryCount,
        int maxRetry,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        LocalDateTime updatedAt
) {
}
