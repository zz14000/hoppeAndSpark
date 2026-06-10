package com.hopeandsparks.infra.llm;

import java.util.Map;

/**
 * LLM 文本生成请求。
 * prompt 是主要输入，extra 用来临时放业务侧补充参数。
 */
public record LlmRequest(
        String prompt,
        Map<String, String> extra
) {
}
