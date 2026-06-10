package com.hopeandsparks.infra.llm;

import java.util.List;
import java.util.Map;

public record LlmRequest(
        String systemPrompt,
        String userPrompt,
        List<String> context,
        Map<String, Object> metadata
) {
}
