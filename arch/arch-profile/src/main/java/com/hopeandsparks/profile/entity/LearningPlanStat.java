package com.hopeandsparks.profile.entity;

public record LearningPlanStat(
        Long id,
        String title,
        String currentStage,
        long finishedCount,
        long totalCount,
        int progress
) {
}
