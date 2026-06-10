package com.hopeandsparks.practice.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 独立答题页中的题目信息。
 */
public record QuestionAnswerVO(
        String id,
        Integer number,
        String type,
        BigDecimal score,
        String stem,
        List<QuestionOptionVO> options,
        List<BlankSlotVO> blanks,
        AnswerConfigVO answerConfig,
        SavedAnswerVO savedAnswer
) {
}
