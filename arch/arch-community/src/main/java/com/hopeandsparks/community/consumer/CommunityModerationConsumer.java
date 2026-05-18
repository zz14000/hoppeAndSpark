package com.hopeandsparks.community.consumer;

/**
 * 社区内容审核消费者边界，用于消费文章和评论的异步审核任务。
 *
 * <p>发布接口只负责先落库为 pending，真正审核由这个 consumer 触发 AI 审核或规则审核。
 * 审核结果再回写文章/评论状态，并通过 {@code arch-task} 记录任务进度和失败原因。</p>
 */
public class CommunityModerationConsumer {
}
