package com.hopeandsparks.infra.embedding;

public interface EmbeddingGateway {

    EmbeddingResponse embed(EmbeddingRequest request);
}
