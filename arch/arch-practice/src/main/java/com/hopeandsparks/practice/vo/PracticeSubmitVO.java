package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交练习结果。
 */
public record PracticeSubmitVO(
        String attemptId,
        Boolean submitted,
        Integer usedSeconds,
        Integer answeredCount,
        Integer unansweredCount,
        Integer flaggedCount,
        BigDecimal objectiveScore,
        BigDecimal objectiveTotalScore,
        String subjectiveStatus,
        String message,
        List<Integer> unansweredQuestionNumbers
) {
}
