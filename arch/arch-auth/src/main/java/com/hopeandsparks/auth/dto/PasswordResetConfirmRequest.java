package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "重置密码确认请求")
public record PasswordResetConfirmRequest(
        @Schema(description = "重置凭证", example = "reset-token")
        @NotBlank(message = "resetToken不能为空")
        String resetToken,

        @Schema(description = "新密码", example = "NewPassw0rd!")
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 64, message = "新密码长度需要在6到64位之间")
        String newPassword
) {
}
