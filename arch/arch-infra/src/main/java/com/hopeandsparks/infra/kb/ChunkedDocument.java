package com.hopeandsparks.infra.kb;

import java.util.List;
import java.util.Map;

public record ChunkedDocument(
        String title,
        List<DocumentChunk> chunks,
        Map<String, Object> metadata
) {
}
