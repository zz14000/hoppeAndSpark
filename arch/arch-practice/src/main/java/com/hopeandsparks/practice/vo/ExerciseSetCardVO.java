package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 练习列表卡片。
 */
public record ExerciseSetCardVO(
        String id,
        String title,
        String type,
        String knowledgeNodeId,
        Integer questionCount,
        Integer timeLimitSeconds,
        Integer answeredCount,
        Integer flaggedCount,
        BigDecimal objectiveScore,
        BigDecimal totalScore,
        Map<String, Long> questionTypeStats
) {
}
