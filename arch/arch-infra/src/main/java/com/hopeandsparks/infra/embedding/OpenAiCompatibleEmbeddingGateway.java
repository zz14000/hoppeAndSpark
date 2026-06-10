package com.hopeandsparks.infra.embedding;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAiCompatibleEmbeddingGateway implements EmbeddingGateway {

    private final AiProperties properties;
    private final WebClient webClient;

    public OpenAiCompatibleEmbeddingGateway(AiProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder
                .baseUrl(trimTrailingSlash(properties.getEmbedding().getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getEmbedding().getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public EmbeddingResponse embed(EmbeddingRequest request) {
        Map<String, Object> body = Map.of(
                "model", properties.getEmbedding().getModel(),
                "input", request.texts() == null ? List.of() : request.texts()
        );
        Map<String, Object> response = webClient.post()
                .uri("/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        List<Map<String, Object>> data = response == null ? List.of() : (List<Map<String, Object>>) response.getOrDefault("data", List.of());
        List<List<Float>> vectors = new ArrayList<>();
        for (Map<String, Object> item : data) {
            List<Object> embedding = (List<Object>) item.getOrDefault("embedding", List.of());
            vectors.add(embedding.stream()
                    .map(value -> value instanceof Number number ? number.floatValue() : Float.parseFloat(String.valueOf(value)))
                    .toList());
        }
        return new EmbeddingResponse(vectors, properties.getEmbedding().getModel(), false);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
