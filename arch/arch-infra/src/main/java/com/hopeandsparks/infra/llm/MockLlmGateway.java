package com.hopeandsparks.infra.llm;

import org.springframework.stereotype.Service;

/**
 * LLM 的空实现。
 * 只返回占位文本，不需要任何 API Key。
 */
@Service
public class MockLlmGateway implements LlmGateway {

    @Override
    public LlmResponse generate(LlmRequest request) {
        return new LlmResponse("LLM 接入暂未开启，当前为 mock 结果。", true);
    }
}
