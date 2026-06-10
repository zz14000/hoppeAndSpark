package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习评测报告。
 */
public record EvaluationReportVO(
        String id,
        String attemptId,
        String exerciseSetId,
        String title,
        BigDecimal overallScore,
        BigDecimal totalScore,
        BigDecimal objectiveScore,
        BigDecimal subjectiveScore,
        String abilitySummary,
        String improvementSuggestion,
        List<KnowledgeScoreVO> knowledgeScores,
        List<KnowledgeScoreVO> weakPoints,
        List<QuestionResultVO> questionResults,
        String generatedBy,
        LocalDateTime createdAt
) {
}
