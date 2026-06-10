package com.hopeandsparks.practice.dto;

/**
 * 请求 Coach 提示时传入当前草稿。
 */
public record CoachHintRequest(
        Integer hintLevel,
        Object currentAnswer,
        String currentCode,
        String question
) {
}
