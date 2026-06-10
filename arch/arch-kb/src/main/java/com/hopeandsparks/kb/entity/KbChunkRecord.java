package com.hopeandsparks.kb.entity;

import java.time.LocalDateTime;

/**
 * Read model for a single KB chunk.
 */
public record KbChunkRecord(
        Long id,
        Long documentId,
        Integer chunkIndex,
        String contentText,
        Integer tokenSize,
        String chromaPointId,
        Integer embedStatus,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
