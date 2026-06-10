package com.hopeandsparks.practice.vo;

import java.time.LocalDateTime;

/**
 * 保存答案结果。
 */
public record AnswerSaveVO(
        String answerId,
        String answerStatus,
        LocalDateTime savedAt,
        Integer answeredCount
) {
}
