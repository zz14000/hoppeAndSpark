package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource network category")
public record ResourceNetworkCategoryVO(
        String id,
        String type,
        String name,
        Long count
) {
}
