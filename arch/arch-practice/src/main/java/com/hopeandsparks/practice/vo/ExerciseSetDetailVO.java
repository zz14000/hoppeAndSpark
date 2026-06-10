package com.hopeandsparks.practice.vo;

import java.util.List;

/**
 * 练习集详情。
 */
public record ExerciseSetDetailVO(
        String id,
        String title,
        String type,
        String knowledgeNodeId,
        String chapter,
        Integer timeLimitSeconds,
        Integer remainingSeconds,
        Integer questionCount,
        Integer answeredCount,
        Integer flaggedCount,
        List<QuestionSummaryVO> questions
) {
}
