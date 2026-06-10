package com.hopeandsparks.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "个人资料更新请求")
public record UserProfileUpdateRequest(
        @Schema(description = "昵称")
        String nickname,

        @Schema(description = "头像地址")
        String avatar,

        @Schema(description = "个人简介。SQL 暂无 bio 字段，W2 会映射到 learningGoal 展示。")
        String bio,

        @Schema(description = "学习方向")
        String learningDomain,

        @Schema(description = "学段/年级")
        String gradeLevel,

        @Schema(description = "基础水平")
        String knowledgeBaseLevel,

        @Schema(description = "自律程度")
        String selfDiscipline,

        @Schema(description = "兴趣标签，W2 会合并到学习偏好文本")
        List<String> interests,

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
