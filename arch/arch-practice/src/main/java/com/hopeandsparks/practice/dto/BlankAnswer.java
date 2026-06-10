package com.hopeandsparks.practice.dto;

/**
 * 填空题单个空的答案。
 */
public record BlankAnswer(
        String blankId,
        String content
) {
}
