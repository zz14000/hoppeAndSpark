package com.hopeandsparks.infra.llm;

/**
 * LLM 文本生成结果。
 * mock=true 表示当前没有真实调用外部模型。
 */
public record LlmResponse(
        String content,
        boolean mock
) {
}
