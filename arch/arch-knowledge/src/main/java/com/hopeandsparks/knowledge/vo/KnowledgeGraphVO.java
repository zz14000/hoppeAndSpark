package com.hopeandsparks.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Knowledge graph response")
public record KnowledgeGraphVO(
        @Schema(description = "Graph nodes")
        List<KnowledgeGraphNodeVO> nodes,

        @Schema(description = "Graph edges")
        List<KnowledgeGraphEdgeVO> edges
) {
}
