package com.hopeandsparks.practice.entity;

/**
 * 练习题查询结果，包含题库和知识点的基本信息。
 */
public record PracticeQuestion(
        Long id,
        Long nodeId,
        String nodeCode,
        String nodeName,
        String questionType,
        String difficultyLevel,
        String contentText,
        String optionsJson,
        String standardAnswer,
        String analysisText,
        Integer sortOrder
) {
}
