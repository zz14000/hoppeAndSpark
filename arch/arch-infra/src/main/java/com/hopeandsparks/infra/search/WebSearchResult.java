package com.hopeandsparks.infra.search;

import java.time.LocalDateTime;

public record WebSearchResult(
        String title,
        String url,
        String summary,
        LocalDateTime fetchedAt,
        double confidence
) {
}
