package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;

/**
 * 报告中的单题评测结果。
 */
public record QuestionResultVO(
        String questionId,
        Integer number,
        String type,
        String nodeId,
        String nodeName,
        Object userAnswer,
        Boolean isCorrect,
        BigDecimal score,
        BigDecimal totalScore,
        String judgeMode,
        String feedback,
        Boolean flagged
) {
}
