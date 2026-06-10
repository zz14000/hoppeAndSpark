package com.hopeandsparks.infra.embedding;

import java.util.List;

public record EmbeddingResponse(List<List<Float>> vectors, String model, boolean mock) {
}
