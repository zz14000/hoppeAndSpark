package com.hopeandsparks.community.entity;

import com.hopeandsparks.community.enums.CommunityContentStatus;

import java.time.LocalDateTime;

/**
 * Read model for blog_comment joined with author data.
 */
public record BlogComment(
        Long id,
        Long postId,
        Long userId,
        String username,
        String nickname,
        String avatarUrl,
        Long parentId,
        Long replyToUserId,
        String content,
        CommunityContentStatus commentStatus,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
