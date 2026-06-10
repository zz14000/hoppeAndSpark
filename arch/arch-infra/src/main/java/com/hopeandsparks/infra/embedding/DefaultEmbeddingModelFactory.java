package com.hopeandsparks.infra.embedding;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.web.reactive.function.client.WebClient;

public class DefaultEmbeddingModelFactory implements EmbeddingModelFactory {

    private final AiProperties properties;
    private final WebClient.Builder builder;

    public DefaultEmbeddingModelFactory(AiProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.builder = builder;
    }

    @Override
    public EmbeddingGateway createGateway() {
        if (realModelConfigured()) {
            return new OpenAiCompatibleEmbeddingGateway(properties, builder);
        }
        return new MockEmbeddingGateway(properties);
    }

    @Override
    public String provider() {
        return properties.getEmbedding().getProvider();
    }

    @Override
    public String modelName() {
        return properties.getEmbedding().getModel();
    }

    @Override
    public boolean realModelConfigured() {
        return properties.getEmbedding().hasApiKey();
    }
}
