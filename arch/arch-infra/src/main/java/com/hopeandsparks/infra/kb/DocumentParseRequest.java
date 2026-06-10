package com.hopeandsparks.infra.kb;

public record DocumentParseRequest(
        String documentId,
        String title,
        String userId,
        String projectId,
        String domain,
        String collection,
        DocumentSourceType sourceType,
        String fileId,
        String sourceUrl,
        String contentText
) {
}
