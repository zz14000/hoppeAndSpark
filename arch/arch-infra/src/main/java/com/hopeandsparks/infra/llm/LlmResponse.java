package com.hopeandsparks.infra.llm;

import java.util.Map;

public record LlmResponse(
        String content,
        String model,
        boolean mock,
        Map<String, Object> metadata
) {
}
