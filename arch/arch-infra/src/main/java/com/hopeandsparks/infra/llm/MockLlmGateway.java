package com.hopeandsparks.infra.llm;

import com.hopeandsparks.infra.config.AiProperties;

import java.util.Map;

/**
 * Deterministic model adapter used until real DeepSeek/LangChain4j wiring is enabled.
 */
public class MockLlmGateway implements LlmGateway {

    private final AiProperties properties;

    public MockLlmGateway(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String prompt = request == null ? "" : request.userPrompt();
        String content = "Mock AI response: " + (prompt == null || prompt.isBlank() ? "empty prompt" : prompt);
        return new LlmResponse(content, properties.getChat().getModel(), true, Map.of("provider", properties.getChat().getProvider()));
    }
}
