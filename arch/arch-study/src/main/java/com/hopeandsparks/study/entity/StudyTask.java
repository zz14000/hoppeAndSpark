package com.hopeandsparks.study.entity;

import java.time.LocalDateTime;

/**
 * Task inside a study plan.
 */
public record StudyTask(
        Long id,
        Long planId,
        Long nodeId,
        Long resourceId,
        String taskTitle,
        String taskType,
        Integer sortOrder,
        LocalDateTime planStartTime,
        LocalDateTime planEndTime,
        Integer taskStatus,
        Integer progressPercent
) {
}
