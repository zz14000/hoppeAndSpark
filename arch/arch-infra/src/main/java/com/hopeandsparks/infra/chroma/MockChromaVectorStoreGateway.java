package com.hopeandsparks.infra.chroma;

import com.hopeandsparks.infra.config.AiProperties;

import java.util.List;
import java.util.Map;

public class MockChromaVectorStoreGateway implements ChromaVectorStoreGateway {

    private final AiProperties properties;

    public MockChromaVectorStoreGateway(AiProperties properties) {
        this.properties = properties;
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
    public VectorSearchResponse search(VectorSearchRequest request) {
        ChromaScope scope = scope(request.userId(), request.projectId(), request.collection());
        String query = blankToDefault(request.query(), "empty query");
        RetrievedChunk chunk = new RetrievedChunk(
                "mock-chunk-1",
                "Mock Chroma chunk for query: " + query,
                "mock knowledge base",
                "",
                0.88,
                Map.of("tenant", scope.tenant(), "database", scope.database(), "collection", scope.collection())
        );
        return new VectorSearchResponse(scope, List.of(chunk), true);
    }

    @Override
    public void ensureScope(ChromaScope scope) {
    }

    @Override
    public UpsertResult upsert(UpsertRequest request) {
        ChromaScope scope = scope(request.userId(), request.projectId(), request.collection());
        return new UpsertResult(scope, request.records() == null ? 0 : request.records().size(), true);
    }

    @Override
    public void deleteByDocument(String userId, String projectId, String collection, String documentId) {
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
