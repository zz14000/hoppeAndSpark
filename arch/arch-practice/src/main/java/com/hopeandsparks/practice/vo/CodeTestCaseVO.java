package com.hopeandsparks.practice.vo;

/**
 * 代码题 mock 测试用例结果。
 */
public record CodeTestCaseVO(
        String name,
        String expected,
        String actual,
        Boolean passed
) {
}
