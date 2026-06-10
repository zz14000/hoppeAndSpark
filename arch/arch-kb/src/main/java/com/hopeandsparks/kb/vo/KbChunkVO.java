package com.hopeandsparks.kb.vo;

import java.time.LocalDateTime;

/**
 * Manage-facing chunk view.
 */
public record KbChunkVO(
        String id,
        String documentId,
        Integer chunkIndex,
        String contentText,
        Integer tokenSize,
        String chromaPointId,
        String embedStatus,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
