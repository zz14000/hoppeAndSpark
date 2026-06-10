package com.hopeandsparks.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "用户主页资料")
public record UserHomeVO(
        @Schema(description = "用户ID")
        String id,

        @Schema(description = "登录账号")
        String username,

        @Schema(description = "昵称")
        String nickname,

        @Schema(description = "头像地址")
        String avatar,

        @Schema(description = "资料简介，W2 从学习目标派生")
        String bio,

        @Schema(description = "学习方向")
        String learningDomain,

        @Schema(description = "学段")
        String gradeLevel,

        @Schema(description = "基础水平")
        String knowledgeBaseLevel,

        @Schema(description = "自律程度")
        String selfDiscipline,

        @Schema(description = "是否完成画像")
        boolean onboarded,

        @Schema(description = "统计数据")
        Map<String, Object> stats,

        @Schema(description = "技能标签")
        List<Map<String, Object>> skills,

        @Schema(description = "学习进度百分比")
        int progress
) {
}
