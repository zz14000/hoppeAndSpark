package com.hopeandsparks.explore.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "思维导图边")
public record MindMapEdgeVO(
        String id,
        String sourceId,
        String targetId,
        String label
) {
}
