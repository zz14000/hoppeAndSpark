package com.hopeandsparks.practice.vo;

import java.util.List;

/**
 * 代码题即时评测结果。
 */
public record CodeRunVO(
        Boolean passed,
        Integer runtimeMs,
        Integer memoryMb,
        List<CodeTestCaseVO> testCases,
        String coachReview
) {
}
