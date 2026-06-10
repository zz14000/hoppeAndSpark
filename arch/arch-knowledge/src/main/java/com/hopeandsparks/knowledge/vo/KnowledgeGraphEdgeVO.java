package com.hopeandsparks.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Knowledge graph edge")
public record KnowledgeGraphEdgeVO(
        @Schema(description = "Edge ID")
        String id,

        @Schema(description = "Source node ID")
        String from,

        @Schema(description = "Target node ID")
        String to,

        @Schema(description = "Relation type")
        String type
) {
}
