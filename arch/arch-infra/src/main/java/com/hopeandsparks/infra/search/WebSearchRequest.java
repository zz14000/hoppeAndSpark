package com.hopeandsparks.infra.search;

import java.util.Map;

public record WebSearchRequest(String query, int topK, Map<String, Object> metadata) {
}
