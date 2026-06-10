package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;

/**
 * 评测报告中的知识点得分。
 */
public record KnowledgeScoreVO(
        String nodeId,
        String nodeName,
        BigDecimal score,
        BigDecimal totalScore,
        BigDecimal accuracy,
        String masteryStatus,
        Integer wrongCount,
        Integer questionCount,
        String suggestion
) {
}
