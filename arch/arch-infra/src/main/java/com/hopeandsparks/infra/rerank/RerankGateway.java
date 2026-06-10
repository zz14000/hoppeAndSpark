package com.hopeandsparks.infra.rerank;

public interface RerankGateway {

    RerankResponse rerank(RerankRequest request);
}
