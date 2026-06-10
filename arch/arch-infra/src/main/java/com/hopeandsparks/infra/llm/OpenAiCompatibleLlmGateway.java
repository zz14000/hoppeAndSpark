package com.hopeandsparks.infra.llm;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAiCompatibleLlmGateway implements LlmGateway {

    private final AiProperties properties;
    private final WebClient webClient;

    public OpenAiCompatibleLlmGateway(AiProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder
                .baseUrl(trimTrailingSlash(properties.getChat().getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getChat().getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResponse generate(LlmRequest request) {
        Map<String, Object> body = Map.of(
                "model", properties.getChat().getModel(),
                "messages", messages(request),
                "temperature", 0.2
        );
        Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        List<Map<String, Object>> choices = response == null ? List.of() : (List<Map<String, Object>>) response.getOrDefault("choices", List.of());
        String content = "";
        if (!choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.getFirst().getOrDefault("message", Map.of());
            content = String.valueOf(message.getOrDefault("content", ""));
        }
        return new LlmResponse(content, properties.getChat().getModel(), false, Map.of("provider", properties.getChat().getProvider()));
    }

    private List<Map<String, String>> messages(LlmRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }
        if (request.context() != null && !request.context().isEmpty()) {
            messages.add(Map.of("role", "system", "content", "Context:\n" + String.join("\n\n", request.context())));
        }
        messages.add(Map.of("role", "user", "content", request.userPrompt() == null ? "" : request.userPrompt()));
        return messages;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
