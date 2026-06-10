package com.hopeandsparks.infra.kb;

import java.util.Map;

public record DocumentChunk(
        int chunkIndex,
        String text,
        String sectionPath,
        int tokenSize,
        Map<String, Object> metadata
) {
}
