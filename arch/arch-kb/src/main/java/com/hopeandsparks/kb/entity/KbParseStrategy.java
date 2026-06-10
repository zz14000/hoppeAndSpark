package com.hopeandsparks.kb.entity;

/**
 * Minimal parse strategy fields used by the mock parser.
 */
public record KbParseStrategy(
        Long id,
        String strategyName,
        Integer chunkSize,
        Integer chunkOverlap
) {
}
