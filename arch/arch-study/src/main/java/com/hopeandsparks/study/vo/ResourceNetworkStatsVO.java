package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource network stats")
public record ResourceNetworkStatsVO(
        Long learnedResourceCount,
        Long totalResourceCount,
        Long studyDurationSeconds,
        Integer exerciseAccuracy
) {
}
