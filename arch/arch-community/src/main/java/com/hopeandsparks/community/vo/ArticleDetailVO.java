package com.hopeandsparks.community.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full article detail returned after visibility checks.
 */
public record ArticleDetailVO(
        String id,
        String title,
        String content,
        String summary,
        ArticleAuthorVO author,
        List<String> tags,
        String coverFileId,
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
