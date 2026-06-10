package com.hopeandsparks.community.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Compact article item used by list pages.
 */
public record ArticleCardVO(
        String id,
        String title,
        String summary,
        ArticleAuthorVO author,
        List<String> tags,
        String status,
        int readCount,
        int likeCount,
        long commentCount,
        int collectCount,
        boolean liked,
        boolean collected,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
