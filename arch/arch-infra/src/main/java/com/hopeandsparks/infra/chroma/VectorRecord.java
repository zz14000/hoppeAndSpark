package com.hopeandsparks.infra.chroma;

import java.util.List;
import java.util.Map;

public record VectorRecord(
        String id,
        String document,
        List<Float> embedding,
        Map<String, Object> metadata
) {
}
