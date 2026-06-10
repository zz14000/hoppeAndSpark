package com.hopeandsparks.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "找回密码凭证响应")
public record PasswordResetTokenVO(
        @Schema(description = "W2 mock 重置凭证。真实邮件接入后不再直接返回给前端。")
        String resetToken,

        @Schema(description = "过期秒数")
        long expiresIn
) {
}
