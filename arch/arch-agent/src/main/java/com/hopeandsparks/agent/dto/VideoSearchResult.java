package com.hopeandsparks.agent.dto;

import java.util.List;

public record VideoSearchResult(
        List<ResourceItem> videos,
        boolean mock
) {
}
