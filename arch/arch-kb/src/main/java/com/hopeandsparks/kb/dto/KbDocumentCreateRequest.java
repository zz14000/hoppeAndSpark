package com.hopeandsparks.kb.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request used by Manage to create a KB document from an uploaded file id.
 */
public record KbDocumentCreateRequest(
        @NotBlank String fileId,
        @NotBlank String title,
        @NotBlank String kbDomain,
        String courseId,
        String nodeId,
        String docType,
        String sourceType,
        String collectionName,
        String parseStrategyId,
        String embeddingModel,
        String embeddingVersion
) {
}
