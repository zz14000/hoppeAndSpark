package com.hopeandsparks.infra.rerank;

import java.util.List;
import java.util.Map;

public record RerankRequest(String query, List<String> documents, int topK, Map<String, Object> metadata) {
}
