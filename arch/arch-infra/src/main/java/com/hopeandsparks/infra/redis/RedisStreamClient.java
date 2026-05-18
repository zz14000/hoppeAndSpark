package com.hopeandsparks.infra.redis;

/**
 * Redis Stream 基础客户端接口，封装消息发布、消费、ACK、重试和死信队列等通用能力。
 *
 * <p>{@code arch-infra} 只负责“怎么和 Redis Stream 通信”，不处理资源生成、知识库解析、
 * 社区审核等业务含义。具体 consumer 放在各业务模块里，消费后调用自己的 Service 和
 * {@code arch-task} 更新任务状态。</p>
 */
public interface RedisStreamClient {
}
