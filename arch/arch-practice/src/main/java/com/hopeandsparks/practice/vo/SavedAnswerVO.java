package com.hopeandsparks.practice.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 已保存的答案草稿。
 */
public record SavedAnswerVO(
        String answerId,
        Object content,
        List<Map<String, Object>> attachments,
        LocalDateTime savedAt
) {
}
