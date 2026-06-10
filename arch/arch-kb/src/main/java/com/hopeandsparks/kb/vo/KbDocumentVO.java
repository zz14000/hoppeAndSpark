package com.hopeandsparks.kb.vo;

import java.time.LocalDateTime;

/**
 * Manage-facing document view object.
 */
public record KbDocumentVO(
        String id,
        String kbDomain,
        String courseId,
        String nodeId,
        String title,
        String fileId,
        String fileName,
        String fileType,
        Long fileSize,
        String docType,
        String sourceType,
        String collectionName,
        String parseStrategyId,
        String embeddingModel,
        String embeddingVersion,
        Integer documentVersion,
        Integer totalTokens,
        Integer chunkCount,
        String parseStatus,
        Boolean vectorized,
        String uploaderId,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
