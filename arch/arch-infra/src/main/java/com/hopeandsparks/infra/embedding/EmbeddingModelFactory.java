package com.hopeandsparks.infra.embedding;

public interface EmbeddingModelFactory {

    EmbeddingGateway createGateway();

    String provider();

    String modelName();

    boolean realModelConfigured();
}
