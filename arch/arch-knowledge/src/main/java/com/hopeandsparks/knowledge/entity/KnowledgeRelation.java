package com.hopeandsparks.knowledge.entity;

/**
 * Directed relation between two knowledge nodes.
 */
public record KnowledgeRelation(
        Long id,
        Long sourceNodeId,
        Long targetNodeId,
        String relationType
) {
}
