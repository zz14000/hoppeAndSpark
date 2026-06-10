package com.hopeandsparks.practice.dto;

import java.util.List;

/**
 * 代码题即时评测请求。
 * MVP 不执行真实沙箱，只做文本级 mock 评阅。
 */
public record CodeRunRequest(
        String language,
        String code,
        String input,
        List<String> testcaseIds
) {
}
