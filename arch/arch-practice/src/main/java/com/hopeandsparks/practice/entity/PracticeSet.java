package com.hopeandsparks.practice.entity;

import java.time.LocalDateTime;

/**
 * 练习集查询结果。
 */
public record PracticeSet(
        Long id,
        Long courseId,
        String courseName,
        String setName,
        String setType,
        String difficultyLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
