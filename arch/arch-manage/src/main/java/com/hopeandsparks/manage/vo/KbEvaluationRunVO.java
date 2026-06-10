package com.hopeandsparks.manage.vo;

public record KbEvaluationRunVO(
        String runId,
        String status,
        double recallAt5,
        double mrrAt10,
        double parseCoverageRate,
        double ocrSuccessRate,
        double autoPromotionPrecision
) {
}
