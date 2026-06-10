package com.hopeandsparks.community.entity;

import com.hopeandsparks.community.enums.CommunityContentStatus;

/**
 * Small snapshot used by the moderation consumer.
 */
public record ModerationContent(
        String targetType,
        Long targetId,
        Long userId,
        String title,
        String content,
        CommunityContentStatus status
) {
}
