package com.hopeandsparks.profile.dto;

import jakarta.validation.constraints.NotBlank;

public record CollectionToggleRequest(
        @NotBlank(message = "收藏对象ID不能为空")
        String targetId,
        String targetType
) {
}
