package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "学习数据统计")
public record LearningStatsVO(
        long totalStudyHours,
        long streakDays,
        int resourceAdoptionRate,
        long generatedResourceCount,
        int communityRankPercent,
        List<LearningPlanStatVO> plans
) {
}
