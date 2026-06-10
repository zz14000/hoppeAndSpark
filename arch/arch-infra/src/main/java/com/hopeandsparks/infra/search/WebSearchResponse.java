package com.hopeandsparks.infra.search;

import java.util.List;

public record WebSearchResponse(List<WebSearchResult> results, boolean mock) {
}
