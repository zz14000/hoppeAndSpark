package com.hopeandsparks.community.dto;

/**
 * Parsed article list query. Tags and category are kept at API level even when
 * the current SQL schema cannot persist them yet.
 */
public record ArticleQuery(
        String keyword,
        String category,
        String tag,
        Long authorId,
        String status
) {
}
