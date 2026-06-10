package com.hopeandsparks.community.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;

import java.time.LocalDateTime;

/**
 * Result returned when an article has entered the moderation queue.
 */
public record ArticlePublishVO(
        String id,
        String status,
        AsyncTaskVO task,
        String moderationMessage,
        LocalDateTime createdAt
) {
}
