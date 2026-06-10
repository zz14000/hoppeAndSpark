package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "学习计划统计")
public record LearningPlanStatVO(
        String id,
        String title,
        String currentStage,
        long finishedCount,
        long totalCount,
        int progress
) {
}
