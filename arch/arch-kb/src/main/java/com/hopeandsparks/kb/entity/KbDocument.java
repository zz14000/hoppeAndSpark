package com.hopeandsparks.kb.entity;

import java.time.LocalDateTime;

/**
 * Read model for kb_document with basic file metadata.
 */
public record KbDocument(
        Long id,
        String kbDomain,
        Long courseId,
        Long nodeId,
        String title,
        Long fileId,
        String fileName,
        String fileType,
        Long fileSize,
        String docType,
        String sourceType,
        String collectionName,
        Long parseStrategyId,
        String embeddingModel,
        String embeddingVersion,
        Integer documentVersion,
        Integer totalTokens,
        Integer chunkCount,
        String parseStatus,
        Long uploaderId,
        String errorMsg,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
