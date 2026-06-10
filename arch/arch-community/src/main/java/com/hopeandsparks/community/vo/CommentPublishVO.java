package com.hopeandsparks.community.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;

import java.time.LocalDateTime;

/**
 * Result returned when a comment has entered the moderation queue.
 */
public record CommentPublishVO(
        String id,
        String articleId,
        String status,
        AsyncTaskVO task,
        LocalDateTime createdAt
) {
}
