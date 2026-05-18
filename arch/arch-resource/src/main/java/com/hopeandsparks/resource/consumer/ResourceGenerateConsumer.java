package com.hopeandsparks.resource.consumer;

/**
 * 学习资源生成任务消费者边界，用于消费 Redis Stream 中的资源生成消息。
 *
 * <p>后续这里会调用 Nebula / Horizon 等 Agent 流程，生成完成后通过 resource Service 写
 * {@code learning_resource} 和 {@code learning_resource_version}，并通过 {@code arch-task}
 * 更新异步任务状态。</p>
 */
public class ResourceGenerateConsumer {
}
