package com.hopeandsparks.practice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户某道题的最近作答记录。
 */
public record UserQuestionRecord(
        Long id,
        Long userId,
        Long questionId,
        Long practiceSetId,
        String userAnswer,
        Boolean correct,
        BigDecimal score,
        String judgeMode,
        String feedbackText,
        Boolean flagged,
        LocalDateTime createdAt
) {
}
