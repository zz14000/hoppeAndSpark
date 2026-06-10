package com.hopeandsparks.practice.vo;

/**
 * 题目标记结果。
 */
public record FlagVO(
        String questionId,
        Boolean flagged,
        Integer flaggedCount
) {
}
