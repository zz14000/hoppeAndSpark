package com.hopeandsparks.infra.search;

import java.time.LocalDateTime;
import java.util.List;

public class MockWebSearchGateway implements WebSearchGateway {

    @Override
    public WebSearchResponse search(WebSearchRequest request) {
        String query = request == null || request.query() == null ? "empty query" : request.query();
        return new WebSearchResponse(List.of(new WebSearchResult(
                "Mock web source for " + query,
                "https://example.com/mock-search",
                "Mock search summary. Real search adapter will replace this result.",
                LocalDateTime.now(),
                0.6
        )), true);
    }
}
