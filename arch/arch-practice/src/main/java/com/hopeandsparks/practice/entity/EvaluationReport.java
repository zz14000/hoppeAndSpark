package com.hopeandsparks.practice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 练习评测报告记录。
 */
public record EvaluationReport(
        Long id,
        Long userId,
        Long courseId,
        Long practiceSetId,
        BigDecimal overallScore,
        String knowledgeScoreJson,
        String abilitySummary,
        String improvementSuggestion,
        String generatedBy,
        LocalDateTime createdAt
) {
}
