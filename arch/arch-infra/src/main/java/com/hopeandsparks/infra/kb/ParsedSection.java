package com.hopeandsparks.infra.kb;

public record ParsedSection(
        String path,
        int level,
        String title,
        String content
) {
}
