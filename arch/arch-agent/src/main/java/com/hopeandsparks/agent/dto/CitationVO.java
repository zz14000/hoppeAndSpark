package com.hopeandsparks.agent.dto;

public record CitationVO(
        String title,
        String url,
        String sourceType,
        double score
) {
}
