package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Spark 画像响应")
public record SparkProfileVO(
        @Schema(description = "画像ID")
        String profileId,

        @Schema(description = "画像摘要")
        String summary,

        @Schema(description = "学习方向")
        String learningDomain,

        @Schema(description = "学段")
        String stage,

        @Schema(description = "自律程度")
        String disciplineLevel,

        @Schema(description = "推荐学习计划ID，W2 暂为空")
        String recommendedPlanId,

        @Schema(description = "基础水平")
        String knowledgeBaseLevel,

        @Schema(description = "认知风格")
        String cognitiveStyle,

        @Schema(description = "学习偏好")
        String learningPreference,

        @Schema(description = "学习目标")
        String learningGoal,

        @Schema(description = "当前薄弱项")
        String currentWeakness
) {
}
