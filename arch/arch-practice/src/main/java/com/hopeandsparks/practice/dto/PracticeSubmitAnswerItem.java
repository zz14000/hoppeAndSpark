package com.hopeandsparks.practice.dto;

import java.util.List;

/**
 * 提交整套练习时携带的单题答案。
 */
public record PracticeSubmitAnswerItem(
        String questionId,
        String type,
        Object answer,
        List<String> selectedOptionKeys,
        List<BlankAnswer> blanks,
        String content,
        String language,
        String code,
        Boolean flagged,
        Integer durationSeconds
) {
    public PracticeAnswerRequest toAnswerRequest() {
        return new PracticeAnswerRequest(
                type,
                answer,
                selectedOptionKeys,
                blanks,
                content,
                null,
                null,
                language,
                code,
                flagged,
                durationSeconds
        );
    }
}
