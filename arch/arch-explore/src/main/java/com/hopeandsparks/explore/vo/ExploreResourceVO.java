package com.hopeandsparks.explore.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nebula 探索资源草案")
public record ExploreResourceVO(
        String id,
        String type,
        String title,
        String summary,
        String nodeId,
        String status
) {
}
