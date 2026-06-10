package com.hopeandsparks.profile.entity;

import java.time.LocalDateTime;

public record SparkProfile(
        Long id,
        Long userId,
        String majorDomain,
        String gradeLevel,
        String knowledgeBaseLevel,
        String cognitiveStyle,
        String learningPreference,
        String errorPreference,
        String learningGoal,
        String selfDiscipline,
        String currentWeakness,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
