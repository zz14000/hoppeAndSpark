package com.hopeandsparks.infra.chroma;

import java.util.Map;

public record RetrievedChunk(
        String chunkId,
        String text,
        String sourceTitle,
        String sourceUrl,
        double score,
        Map<String, Object> metadata
) {
}
