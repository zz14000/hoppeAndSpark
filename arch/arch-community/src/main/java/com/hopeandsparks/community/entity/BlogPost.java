package com.hopeandsparks.community.entity;

import com.hopeandsparks.community.enums.CommunityContentStatus;

import java.time.LocalDateTime;

/**
 * Read model for blog_post joined with author and viewer interaction data.
 */
public record BlogPost(
        Long id,
        Long userId,
        String username,
        String nickname,
        String avatarUrl,
        String title,
        String summary,
        String contentMd,
        Long coverFileId,
        CommunityContentStatus postStatus,
        int viewCount,
        int likeCount,
        int favoriteCount,
        long commentCount,
        boolean liked,
        boolean collected,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
