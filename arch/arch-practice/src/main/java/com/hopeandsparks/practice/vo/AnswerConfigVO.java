package com.hopeandsparks.practice.vo;

/**
 * 独立答题页配置。
 */
public record AnswerConfigVO(
        Boolean richText,
        Boolean allowImageUpload,
        Integer maxImages,
        Integer maxImageSizeMb,
        Integer autoSaveIntervalSeconds,
        String language,
        String starterCode
) {
}
