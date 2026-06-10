package com.hopeandsparks.kb.dto;

public record KbDocumentUpdateRequest(
        String title,
        String domain,
        String projectId,
        String sourceUrl,
        String contentText,
        String collection,
        Boolean reparse
) {
}
