package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "收藏聚合项")
public record CollectionItemVO(
        String id,
        String targetType,
        String targetId,
        String title,
        String summary,
        LocalDateTime createdAt
) {
}
