package com.hopeandsparks.infra.search;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tavily搜索引擎适配器
 * 
 * Tavily是专为AI应用设计的搜索API，提供：
 * - 高质量搜索结果
 * - 内容提取和摘要
 * - 来源可信度评分
 * - 适合RAG应用的优化
 * 
 * 使用方式：
 * 1. 注册Tavily账号获取API Key: https://tavily.com/
 * 2. 设置环境变量: TAVILY_API_KEY=your-api-key
 * 3. 配置application.yml中的hope.ai.search.provider为"tavily"
 */
public class TavilyWebSearchGateway implements WebSearchGateway {

    private final WebClient webClient;
    private final String apiKey;
    private final String searchDepth;
    private final int maxResults;

    public TavilyWebSearchGateway(AiProperties.Search properties, WebClient.Builder builder) {
        this.apiKey = properties.getApiKey();
        this.searchDepth = properties.getSearchDepth();
        this.maxResults = properties.getMaxResults();
        this.webClient = builder
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebSearchResponse search(WebSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            return new WebSearchResponse(List.of(), false);
        }

        Map<String, Object> body = Map.of(
                "query", request.query(),
                "search_depth", searchDepth == null || searchDepth.isBlank() ? "basic" : searchDepth,
                "max_results", maxResults(request.topK()),
                "include_answer", true,
                "include_raw_content", false,
                "include_images", false
        );

        Map<String, Object> response = webClient.post()
                .uri("/search")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<WebSearchResult> results = parseResults(response);
        return new WebSearchResponse(results, false);
    }

    @SuppressWarnings("unchecked")
    private List<WebSearchResult> parseResults(Map<String, Object> response) {
        List<WebSearchResult> results = new ArrayList<>();
        
        if (response == null) {
            return results;
        }

        // Tavily返回的结果列表
        List<Map<String, Object>> tavilyResults = (List<Map<String, Object>>) response.get("results");
        if (tavilyResults == null || tavilyResults.isEmpty()) {
            return results;
        }

        for (Map<String, Object> item : tavilyResults) {
            try {
                String title = (String) item.get("title");
                String url = (String) item.get("url");
                String content = (String) item.get("content");
                Double score = item.get("score") != null ? ((Number) item.get("score")).doubleValue() : 0.5;

                if (title != null && url != null && content != null) {
                    results.add(new WebSearchResult(
                            title,
                            url,
                            content,
                            LocalDateTime.now(),
                            score
                    ));
                }
            } catch (Exception e) {
                // 跳过解析失败的条目
                continue;
            }
        }

        return results;
    }

    private int maxResults(int requestTopK) {
        int configured = maxResults <= 0 ? 5 : maxResults;
        int requested = requestTopK <= 0 ? configured : requestTopK;
        return Math.max(1, Math.min(configured, requested));
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://api.tavily.com";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
