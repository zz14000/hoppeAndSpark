package com.hopeandsparks.infra.rerank;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DashScopeRerankGateway implements RerankGateway {

    private final AiProperties properties;
    private final WebClient webClient;

    public DashScopeRerankGateway(AiProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder
                .baseUrl(trimTrailingSlash(properties.getRerank().getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getRerank().getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RerankResponse rerank(RerankRequest request) {
        Map<String, Object> body = Map.of(
                "model", properties.getRerank().getModel(),
                "input", Map.of(
                        "query", request.query() == null ? "" : request.query(),
                        "documents", request.documents() == null ? List.of() : request.documents()
                ),
                "parameters", Map.of("top_n", request.topK() <= 0 ? 5 : request.topK())
        );
        
        Map<String, Object> response = webClient.post()
                .uri("/services/rerank/text-rerank/text-rerank")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        Map<String, Object> output = response == null ? Map.of() : (Map<String, Object>) response.getOrDefault("output", Map.of());
        List<Map<String, Object>> results = (List<Map<String, Object>>) output.getOrDefault("results", List.of());
        List<String> documents = request.documents() == null ? List.of() : request.documents();
        List<RerankResult> ranked = results.stream()
                .map(item -> toResult(item, documents))
                .sorted(Comparator.comparingDouble(RerankResult::score).reversed())
                .toList();
        return new RerankResponse(ranked, properties.getRerank().getModel(), false);
    }

    private RerankResult toResult(Map<String, Object> item, List<String> documents) {
        int index = number(item.getOrDefault("index", item.getOrDefault("document_index", 0))).intValue();
        double score = number(item.getOrDefault("relevance_score", item.getOrDefault("score", 0.0))).doubleValue();
        String document = index >= 0 && index < documents.size() ? documents.get(index) : "";
        return new RerankResult(index, document, score);
    }

    private Number number(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
