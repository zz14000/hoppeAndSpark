package com.hopeandsparks.infra.chroma;

import java.util.List;

public record VectorSearchResponse(ChromaScope scope, List<RetrievedChunk> chunks, boolean mock) {
}
