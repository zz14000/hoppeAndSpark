package com.hopeandsparks.study.dto;

import java.math.BigDecimal;

/**
 * 练习模块传回来的薄弱知识点信号。
 * study 模块只关心知识点、掌握度和复习建议，不关心具体题目评测过程。
 */
public record PracticeWeakPointFeedback(
        Long nodeId,
        String nodeName,
        BigDecimal accuracy,
        BigDecimal score,
        BigDecimal totalScore,
        Integer wrongCount,
        Integer questionCount,
        String suggestion
) {
}
