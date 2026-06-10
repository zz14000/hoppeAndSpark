package com.hopeandsparks.infra.embedding;

import java.util.List;
import java.util.Map;

public record EmbeddingRequest(List<String> texts, Map<String, Object> metadata) {
}
