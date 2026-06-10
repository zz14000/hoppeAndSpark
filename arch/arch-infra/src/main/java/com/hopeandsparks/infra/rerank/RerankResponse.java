package com.hopeandsparks.infra.rerank;

import java.util.List;

public record RerankResponse(List<RerankResult> results, String model, boolean mock) {
}
