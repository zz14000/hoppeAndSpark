package com.hopeandsparks.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Knowledge graph node")
public record KnowledgeGraphNodeVO(
        @Schema(description = "Knowledge node ID")
        String id,

        @Schema(description = "Knowledge node name")
        String name,

        @Schema(description = "Knowledge node description")
        String desc,

        @Schema(description = "Stable business key from knowledge_node.node_code")
        String knowledgeKey,

        @Schema(description = "Course ID")
        String courseId,

        @Schema(description = "Parent node ID")
        String parentId,

        @Schema(description = "Difficulty level")
        String difficultyLevel
) {
}
