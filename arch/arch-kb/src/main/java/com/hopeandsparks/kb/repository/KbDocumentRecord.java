package com.hopeandsparks.kb.repository;

public record KbDocumentRecord(
        Long id,
        String domain,
        String projectId,
        String title,
        String fileId,
        String docType,
        String sourceType,
        String sourceUrl,
        String contentText,
        String collectionName,
        String embeddingModel,
        String embeddingVersion,
        int documentVersion,
        int totalTokens,
        int chunkCount,
        String parseStatus,
        String errorMessage,
        String userId,
        boolean deleted
) {
}
