package com.hopeandsparks.community.vo;

/**
 * Small author block returned by community article and comment APIs.
 */
public record ArticleAuthorVO(
        String id,
        String nickname,
        String avatar
) {
}
