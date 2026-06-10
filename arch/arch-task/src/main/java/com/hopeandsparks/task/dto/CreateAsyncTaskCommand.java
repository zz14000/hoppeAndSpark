package com.hopeandsparks.task.dto;

/**
 * 创建异步任务时使用的简单命令对象。
 * W1 先保留最常用字段，后面接数据库时可以直接映射到 async_generation_task。
 */
public record CreateAsyncTaskCommand(
        String taskType,
        String ownerType,
        String ownerId,
        String idempotencyKey,
        Integer maxRetry
) {
}
