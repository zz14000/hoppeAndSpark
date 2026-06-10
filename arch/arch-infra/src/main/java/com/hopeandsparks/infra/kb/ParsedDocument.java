package com.hopeandsparks.infra.kb;

import java.util.List;
import java.util.Map;

public record ParsedDocument(
        String title,
        String plainText,
        String mediaType,
        List<ParsedSection> sections,
        Map<String, Object> metadata
) {
}
