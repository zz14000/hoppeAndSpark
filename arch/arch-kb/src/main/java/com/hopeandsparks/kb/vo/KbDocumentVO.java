package com.hopeandsparks.kb.vo;

public record KbDocumentVO(
        String id,
        String title,
        String domain,
        String sourceType,
        String sourceUrl,
        String fileId,
        String collection,
        String parseStatus,
        boolean mock
) {
}
