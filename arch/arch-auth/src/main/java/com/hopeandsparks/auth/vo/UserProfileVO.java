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
        String avatarUrl,

        @Schema(description = "手机号", example = "13800000000")
        String phone,

        @Schema(description = "邮箱", example = "spark@example.com")
        String email
) {
}
