package com.hopeandsparks.kb.consumer;

/**
 * 知识库解析任务消费者边界，用于消费 {@code queue:kb:parse} 这类 Redis Stream 消息。
 *
 * <p>后续这里会读取文档文件、解析切片、写入 {@code kb_chunk_record}，并继续投递向量化任务。
 * 任务开始、成功、失败和重试都应同步更新 {@code arch-task} 中的异步任务状态。</p>
 */
public class KbParseConsumer {
}
