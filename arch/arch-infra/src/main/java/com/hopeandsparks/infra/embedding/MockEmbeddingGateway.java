package com.hopeandsparks.infra.embedding;

import com.hopeandsparks.infra.config.AiProperties;

import java.util.ArrayList;
import java.util.List;

public class MockEmbeddingGateway implements EmbeddingGateway {

    private final AiProperties properties;

    public MockEmbeddingGateway(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        List<List<Float>> vectors = new ArrayList<>();
        List<String> texts = request == null || request.texts() == null ? List.of() : request.texts();
        for (String text : texts) {
            vectors.add(mockVector(text));
        }
        return new EmbeddingResponse(vectors, properties.getEmbedding().getModel(), true);
    }

    private List<Float> mockVector(String text) {
        int seed = text == null ? 0 : text.hashCode();
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            vector.add(((seed >> (i % 16)) & 0xff) / 255.0f);
        }
        return vector;
    }
}
