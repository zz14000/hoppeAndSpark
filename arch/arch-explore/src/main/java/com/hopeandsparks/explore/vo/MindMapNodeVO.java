package com.hopeandsparks.explore.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "思维导图节点")
public record MindMapNodeVO(
        String id,
        String label,
        String parentId,
        String type,
        String resourceId
) {
}
