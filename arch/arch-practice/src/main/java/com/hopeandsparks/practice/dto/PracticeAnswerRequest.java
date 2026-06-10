package com.hopeandsparks.practice.dto;

import java.util.List;

/**
 * 保存单题答案请求。
 * 不同题型字段不同，所以保留 answer 兜底字段。
 */
public record PracticeAnswerRequest(
        String type,
        Object answer,
        List<String> selectedOptionKeys,
        List<BlankAnswer> blanks,
        String content,
        String contentFormat,
        List<String> attachments,
        String language,
        String code,
        Boolean flagged,
        Integer durationSeconds
) {
}
