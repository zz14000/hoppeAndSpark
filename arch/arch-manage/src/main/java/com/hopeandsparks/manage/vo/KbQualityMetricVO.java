package com.hopeandsparks.manage.vo;

public record KbQualityMetricVO(
        double ocrHitRate,
        double avgChunkCount,
        double avgChunkLength,
        double embeddingFailureRate,
        double chromaFailureRate,
        double autoPromotionPrecision
) {
}
