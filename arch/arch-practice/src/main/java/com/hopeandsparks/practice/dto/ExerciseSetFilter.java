package com.hopeandsparks.practice.dto;

/**
 * 练习列表查询条件。
 */
public record ExerciseSetFilter(
        Long planId,
        String nodeId,
        String status,
        String type
) {
}
