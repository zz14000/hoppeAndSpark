package com.hopeandsparks.study.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Study plan row with course and progress summary.
 */
public record StudyPlan(
        Long id,
        Long userId,
        Long courseId,
        String courseName,
        String majorDomain,
        String planTitle,
        Integer planStatus,
        LocalDate startDate,
        LocalDate endDate,
        String generatedBy,
        Long finishedCount,
        Long totalCount,
        Integer progress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
