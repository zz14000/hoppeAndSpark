package com.hopeandsparks.practice.dto;

/**
 * 标记或取消错题本状态。
 */
public record QuestionFlagRequest(
        Boolean flagged
) {
}
