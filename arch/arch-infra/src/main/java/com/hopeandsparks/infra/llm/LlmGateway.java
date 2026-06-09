package com.hopeandsparks.infra.llm;

/**
 * 通用 LLM 网关。
 * W1 先留接口和 mock 实现，后续接入具体模型供应商时业务模块不用改。
 */
public interface LlmGateway {

    LlmResponse generate(LlmRequest request);
}
