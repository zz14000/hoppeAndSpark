package com.hopeandsparks.knowledge.entity;

/**
 * Knowledge point node used by graph and study topology.
 */
public record KnowledgeNode(
        Long id,
        Long courseId,
        Long parentId,
        String nodeCode,
        String nodeName,
        String nodeDesc,
        String difficultyLevel,
        Integer sortOrder
) {
}
