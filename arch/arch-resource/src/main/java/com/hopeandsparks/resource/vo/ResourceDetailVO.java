package com.hopeandsparks.resource.vo;

public record ResourceDetailVO(
        String id,
        String title,
        String resourceType,
        String summary,
        String content,
        boolean mock
) {
}
