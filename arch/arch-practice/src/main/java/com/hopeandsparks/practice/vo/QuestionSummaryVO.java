package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 练习详情页中的题目摘要。
 */
public record QuestionSummaryVO(
        String id,
        Integer number,
        String type,
        BigDecimal score,
        String stem,
        List<QuestionOptionVO> options,
        List<BlankSlotVO> blanks,
        String answerStatus,
        Boolean flagged,
        Boolean requiresDedicatedAnswerPage,
        String answerPageUrl,
        Boolean richText,
        Boolean allowImageUpload,
        String language,
        String starterCode
) {
}
