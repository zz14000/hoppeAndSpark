package com.hopeandsparks.task.enums;

/**
 * 异步任务统一状态枚举，对应 {@code async_generation_task} 的生命周期。
 *
 * <p>资源生成、知识库解析、社区审核、学习计划生成等异步流程都可以复用这些状态。
 * 业务 consumer 消费 Redis Stream 后，应通过 {@code AsyncTaskService} 更新任务状态。</p>
 */
public enum AsyncTaskStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELED
}
