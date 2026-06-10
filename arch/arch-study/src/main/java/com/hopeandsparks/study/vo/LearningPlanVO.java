package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Current learning plan")
public record LearningPlanVO(
        String id,
        String title,
        String domain,
        Integer estimatedHours,
        Integer progress,
        String createdBy,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        List<StudyTaskVO> tasks
) {
}
