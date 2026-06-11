package com.hopeandsparks.agent.dto;

import java.util.Map;

public record RetrievalHit(
        String sourceType,
        String collection,
        String documentId,
        String chunkId,
        String title,
        String url,
        String text,
        double score,
        int rank,
        Map<String, Object> metadata
) {
}
