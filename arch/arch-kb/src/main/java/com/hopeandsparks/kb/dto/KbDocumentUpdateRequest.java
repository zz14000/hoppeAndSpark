package com.hopeandsparks.kb.dto;

/**
 * Optional fields for editing KB document metadata from Manage.
 */
public record KbDocumentUpdateRequest(
        String title,
        String kbDomain,
        String courseId,
        String nodeId,
        String docType,
        String sourceType,
        String collectionName,
        String parseStrategyId,
        String embeddingModel,
        String embeddingVersion,
        Boolean reparse
) {
}
