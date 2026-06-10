package com.hopeandsparks.infra.llm;

/**
 * Boundary for future LangChain4j chat model construction.
 */
public interface ChatModelFactory {

    LlmGateway createGateway();

    String provider();

    String modelName();

    boolean realModelConfigured();
}
