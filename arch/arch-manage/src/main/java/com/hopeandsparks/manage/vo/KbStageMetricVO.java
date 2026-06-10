package com.hopeandsparks.manage.vo;

public record KbStageMetricVO(
        String stage,
        long totalCount,
        double successRate,
        double failedRate,
        long p50DurationMs,
        long p95DurationMs
) {
}
