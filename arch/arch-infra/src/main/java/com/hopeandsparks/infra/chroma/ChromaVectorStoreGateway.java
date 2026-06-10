package com.hopeandsparks.infra.chroma;

public interface ChromaVectorStoreGateway {

    ChromaScope scope(String userId, String projectId, String collection);

    void ensureScope(ChromaScope scope);

    UpsertResult upsert(UpsertRequest request);

    VectorSearchResponse search(VectorSearchRequest request);

    void deleteByDocument(String userId, String projectId, String collection, String documentId);
}
