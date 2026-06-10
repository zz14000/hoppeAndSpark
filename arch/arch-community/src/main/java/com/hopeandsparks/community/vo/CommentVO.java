package com.hopeandsparks.community.vo;

import java.time.LocalDateTime;

/**
 * Comment item returned by article comment APIs.
 */
public record CommentVO(
        String id,
        String articleId,
        ArticleAuthorVO author,
        String content,
        String parentId,
        String replyToUserId,
        String status,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
