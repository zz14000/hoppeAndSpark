package com.hopeandsparks.manage.vo;

public record KbDashboardOverviewVO(
        long totalDocuments,
        double successRate,
        double failedRate,
        double retryRate,
        long queueBacklog,
        double autoPromotionRate,
        double rollbackRate
) {
}
