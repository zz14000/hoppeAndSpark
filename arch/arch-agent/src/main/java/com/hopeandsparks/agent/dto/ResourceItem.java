package com.hopeandsparks.agent.dto;

import java.util.List;
import java.util.Map;

public record ResourceItem(
        String resourceId,
        String type,
        String title,
        String url,
        String source,
        String summary,
        String thumbnailUrl,
        long durationSec,
        String difficulty,
        List<String> knowledgePoints,
        double confidence,
        Map<String, Object> metadata
) {
}
