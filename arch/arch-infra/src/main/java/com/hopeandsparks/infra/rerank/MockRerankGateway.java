package com.hopeandsparks.infra.rerank;

import com.hopeandsparks.infra.config.AiProperties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MockRerankGateway implements RerankGateway {

    private final AiProperties properties;

    public MockRerankGateway(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public RerankResponse rerank(RerankRequest request) {
        List<RerankResult> results = new ArrayList<>();
        List<String> documents = request == null || request.documents() == null ? List.of() : request.documents();
        for (int i = 0; i < documents.size(); i++) {
            double score = score(request.query(), documents.get(i));
            results.add(new RerankResult(i, documents.get(i), score));
        }
        results.sort(Comparator.comparingDouble(RerankResult::score).reversed());
        int topK = request == null || request.topK() <= 0 ? results.size() : Math.min(request.topK(), results.size());
        return new RerankResponse(results.subList(0, topK), properties.getRerank().getModel(), true);
    }

    private double score(String query, String document) {
        if (query == null || document == null || document.isBlank()) {
            return 0.1;
        }
        int matches = 0;
        for (String part : query.split("\\s+")) {
            if (!part.isBlank() && document.contains(part)) {
                matches++;
            }
        }
        return 0.5 + matches;
    }
}
