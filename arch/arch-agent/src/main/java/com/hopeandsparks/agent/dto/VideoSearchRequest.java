package com.hopeandsparks.agent.dto;

import java.util.List;

public record VideoSearchRequest(
        String query,
        List<String> allowedPlatforms,
        int topK
) {
}
