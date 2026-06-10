package com.hopeandsparks.practice.dto;

import java.util.List;

/**
 * 提交整套练习请求。
 */
public record PracticeSubmitRequest(
        Integer durationSeconds,
        List<PracticeSubmitAnswerItem> answers,
        Boolean forceSubmit
) {
}
