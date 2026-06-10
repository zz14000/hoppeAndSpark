package com.hopeandsparks.infra.chroma;

import java.util.List;
import java.util.Map;

public record VectorSearchRequest(
        String userId,
        String projectId,
        String collection,
        String query,
        List<Float> vector,
        int topK,
        Map<String, Object> filters
) {
}
