package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Study task")
public record StudyTaskVO(
        String id,
        String nodeId,
        String resourceId,
        String title,
        String type,
        String status,
        Integer progress,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
