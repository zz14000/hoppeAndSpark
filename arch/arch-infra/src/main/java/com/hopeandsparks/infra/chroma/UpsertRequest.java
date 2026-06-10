package com.hopeandsparks.infra.chroma;

import java.util.List;

public record UpsertRequest(
        String userId,
        String projectId,
        String collection,
        List<VectorRecord> records
) {
}
