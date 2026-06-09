package com.hopeandsparks.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "前台用户基础资料")
public record UserProfileVO(
        @Schema(description = "用户ID", example = "1001")
        String id,

        @Schema(description = "登录账号", example = "spark001")
        String username,

        @Schema(description = "昵称", example = "小火花")
        String nickname,

        @Schema(description = "头像URL")
        String avatar,

        @Schema(description = "手机号", example = "13800000000")
        String phone,

        @Schema(description = "邮箱", example = "spark@example.com")
        String email,

        @Schema(description = "是否完成首次画像")
        boolean onboarded,

        @Schema(description = "画像摘要或学习目标")
        String bio,

        @Schema(description = "学习方向")
        String learningDomain,

        @Schema(description = "学段/年级")
        String gradeLevel,

        @Schema(description = "基础水平")
        String knowledgeBaseLevel,

        @Schema(description = "自律程度")
        String selfDiscipline
) {
}
