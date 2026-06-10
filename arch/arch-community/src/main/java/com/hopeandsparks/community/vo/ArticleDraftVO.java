package com.hopeandsparks.community.vo;

import java.time.LocalDateTime;

/**
 * Draft save result.
 */
public record ArticleDraftVO(
        String id,
        String status,
        LocalDateTime updatedAt
) {
}
