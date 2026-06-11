package com.hopeandsparks.infra.chroma;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChromaRestVectorStoreGateway implements ChromaVectorStoreGateway {

    private final AiProperties properties;
    private final WebClient webClient;
    private final String apiVersion;

    public ChromaRestVectorStoreGateway(AiProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.apiVersion = properties.getChroma().getApiVersion();
        this.webClient = builder.baseUrl(trimTrailingSlash(properties.getChroma().getBaseUrl())).build();
    }

    @Override
    public ChromaScope scope(String userId, String projectId, String collection) {
        String safeUser = blankToDefault(userId, "anonymous");
        String safeProject = blankToDefault(projectId, "default");
        String tenant = properties.getChroma().getTenantTemplate().replace("{userId}", safeUser);
        String database = properties.getChroma().getDatabaseTemplate().replace("{projectId}", safeProject);
        return new ChromaScope(safeUser, safeProject, tenant, database, blankToDefault(collection, "edu_ground_truth"));
    }

    @Override
    public void ensureScope(ChromaScope scope) {
        createTenant(scope);
        createDatabase(scope);
        createCollection(scope);
    }

    @Override
    public UpsertResult upsert(UpsertRequest request) {
        ChromaScope scope = scope(request.userId(), request.projectId(), request.collection());
        ensureScope(scope);
        Map<String, Object> collection = findCollection(scope);
        if (collection.isEmpty()) {
            throw new IllegalStateException("Chroma collection not found after ensure: " + scope.collection());
        }
        String collectionId = String.valueOf(collection.get("id"));
        List<VectorRecord> records = request.records() == null ? List.of() : request.records();
        Map<String, Object> body = Map.of(
                "ids", records.stream().map(VectorRecord::id).toList(),
                "documents", records.stream().map(VectorRecord::document).toList(),
                "embeddings", records.stream().map(VectorRecord::embedding).toList(),
                "metadatas", records.stream().map(VectorRecord::metadata).toList()
        );
        webClient.post()
                .uri(upsertUri(scope, collectionId))
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
        return new UpsertResult(scope, records.size(), false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public VectorSearchResponse search(VectorSearchRequest request) {
        List<String> targetCollections = request.collections() == null || request.collections().isEmpty()
                ? List.of(blankToDefault(request.collection(), "edu_ground_truth"))
                : request.collections();
        List<RetrievedChunk> allChunks = new ArrayList<>();
        ChromaScope effectiveScope = null;
        for (String collectionName : targetCollections) {
            ChromaScope scope = scope(request.userId(), request.projectId(), collectionName);
            effectiveScope = scope;
            Map<String, Object> collection = findCollection(scope);
            if (collection.isEmpty()) {
                continue;
            }
            String collectionId = String.valueOf(collection.get("id"));
            Map<String, Object> body = request.vector() != null && !request.vector().isEmpty()
                    ? Map.of(
                    "query_embeddings", List.of(request.vector()),
                    "n_results", request.topK() <= 0 ? 5 : request.topK(),
                    "where", sanitizeFilters(request.filters()),
                    "include", List.of("documents", "metadatas", "distances")
            )
                    : Map.of(
                    "query_texts", List.of(blankToDefault(request.query(), "")),
                    "n_results", request.topK() <= 0 ? 5 : request.topK(),
                    "where", sanitizeFilters(request.filters()),
                    "include", List.of("documents", "metadatas", "distances")
            );
            Map<String, Object> response = webClient.post()
                    .uri(queryUri(scope, collectionId))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            allChunks.addAll(chunks(response));
        }
        return new VectorSearchResponse(effectiveScope == null ? scope(request.userId(), request.projectId(), request.collection()) : effectiveScope, allChunks, false);
    }

    @Override
    public void deleteByDocument(String userId, String projectId, String collection, String documentId) {
        ChromaScope scope = scope(userId, projectId, collection);
        Map<String, Object> existing = findCollection(scope);
        if (existing.isEmpty()) {
            return;
        }
        String collectionId = String.valueOf(existing.get("id"));
        Map<String, Object> body = Map.of(
                "where", Map.of("documentId", documentId)
        );
        webClient.post()
                .uri(deleteUri(scope, collectionId))
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findCollection(ChromaScope scope) {
        List<Map<String, Object>> collections = webClient.get()
                .uri(collectionsUri(scope))
                .retrieve()
                .bodyToMono(List.class)
                .block();
        if (collections == null) {
            return Map.of();
        }
        return collections.stream()
                .filter(item -> scope.collection().equals(String.valueOf(item.get("name"))))
                .findFirst()
                .orElse(Map.of());
    }

    private void createTenant(ChromaScope scope) {
        try {
            webClient.post()
                    .uri("/api/v2/tenants")
                    .bodyValue(Map.of("name", scope.tenant()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (RuntimeException exception) {
            if (!isConflict(exception)) {
                throw exception;
            }
        }
    }

    private void createDatabase(ChromaScope scope) {
        try {
            webClient.post()
                    .uri("/api/v2/tenants/" + scope.tenant() + "/databases")
                    .bodyValue(Map.of("name", scope.database()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (RuntimeException exception) {
            if (!isConflict(exception)) {
                throw exception;
            }
        }
    }

    private void createCollection(ChromaScope scope) {
        try {
            webClient.post()
                    .uri(collectionsUri(scope))
                    .bodyValue(Map.of(
                            "name", scope.collection(),
                            "metadata", Map.of("userId", scope.userId(), "projectId", scope.projectId()),
                            "get_or_create", true
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (RuntimeException exception) {
            if (!isConflict(exception)) {
                throw exception;
            }
        }
    }

    private String collectionsUri(ChromaScope scope) {
        if ("v2".equalsIgnoreCase(apiVersion)) {
            return "/api/v2/tenants/" + scope.tenant() + "/databases/" + scope.database() + "/collections";
        }
        return "/api/v1/collections";
    }

    private String queryUri(ChromaScope scope, String collectionId) {
        if ("v2".equalsIgnoreCase(apiVersion)) {
            return "/api/v2/tenants/" + scope.tenant() + "/databases/" + scope.database() + "/collections/" + collectionId + "/query";
        }
        return "/api/v1/collections/" + collectionId + "/query";
    }

    private String upsertUri(ChromaScope scope, String collectionId) {
        if ("v2".equalsIgnoreCase(apiVersion)) {
            return "/api/v2/tenants/" + scope.tenant() + "/databases/" + scope.database() + "/collections/" + collectionId + "/upsert";
        }
        return "/api/v1/collections/" + collectionId + "/upsert";
    }

    private String deleteUri(ChromaScope scope, String collectionId) {
        if ("v2".equalsIgnoreCase(apiVersion)) {
            return "/api/v2/tenants/" + scope.tenant() + "/databases/" + scope.database() + "/collections/" + collectionId + "/delete";
        }
        return "/api/v1/collections/" + collectionId + "/delete";
    }

    @SuppressWarnings("unchecked")
    private List<RetrievedChunk> chunks(Map<String, Object> response) {
        if (response == null) {
            return List.of();
        }
        List<List<String>> documents = (List<List<String>>) response.getOrDefault("documents", List.of());
        List<List<Map<String, Object>>> metadatas = (List<List<Map<String, Object>>>) response.getOrDefault("metadatas", List.of());
        List<List<Number>> distances = (List<List<Number>>) response.getOrDefault("distances", List.of());
        List<RetrievedChunk> chunks = new ArrayList<>();
        if (documents.isEmpty()) {
            return chunks;
        }
        List<String> firstDocuments = documents.getFirst();
        for (int index = 0; index < firstDocuments.size(); index++) {
            Map<String, Object> metadata = metadatas.isEmpty() || metadatas.getFirst().size() <= index
                    ? Map.of()
                    : metadatas.getFirst().get(index);
            double distance = distances.isEmpty() || distances.getFirst().size() <= index
                    ? 0.0
                    : distances.getFirst().get(index).doubleValue();
            chunks.add(new RetrievedChunk(
                    String.valueOf(metadata.getOrDefault("chunkId", "chroma-" + index)),
                    firstDocuments.get(index),
                    String.valueOf(metadata.getOrDefault("sourceTitle", "")),
                    String.valueOf(metadata.getOrDefault("sourceUrl", "")),
                    1.0 - distance,
                    metadata
            ));
        }
        return chunks;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private boolean isConflict(RuntimeException exception) {
        String message = Objects.toString(exception.getMessage(), "").toLowerCase();
        return message.contains("409") || message.contains("already exists") || message.contains("conflict");
    }

    private Map<String, Object> sanitizeFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new java.util.LinkedHashMap<>();
        copyIfPresent(filters, sanitized, "documentId");
        copyIfPresent(filters, sanitized, "sourceDomain");
        copyIfPresent(filters, sanitized, "projectId");
        copyIfPresent(filters, sanitized, "userId");
        copyIfPresent(filters, sanitized, "promotionStatus");
        return sanitized;
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        Object value = source.get(key);
        if (value != null && !String.valueOf(value).isBlank()) {
            target.put(key, value);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8000";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
