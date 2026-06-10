package com.hopeandsparks.profile.entity;

import java.time.LocalDateTime;

public record CollectionItem(
        Long id,
        String targetType,
        Long targetId,
        String title,
        String summary,
        LocalDateTime createdAt
) {
}
