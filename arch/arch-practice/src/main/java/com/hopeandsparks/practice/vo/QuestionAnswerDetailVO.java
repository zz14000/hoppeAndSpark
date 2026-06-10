package com.hopeandsparks.practice.vo;

/**
 * 单题答题详情。
 */
public record QuestionAnswerDetailVO(
        String exerciseSetId,
        QuestionAnswerVO question,
        TimerVO timer
) {
}
