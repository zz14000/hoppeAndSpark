package com.hopeandsparks.community.vo;

import java.util.List;

/**
 * Mock polishing suggestion returned by Horizon placeholder logic.
 */
public record ArticlePolishVO(
        String summary,
        List<String> suggestedTags,
        String suggestedTitle,
        String polishedContent,
        String riskLevel
) {
}
