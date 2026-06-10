package com.hopeandsparks.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "重置凭证不能为空")
        String resetToken,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度应为 6 到 64 位")
        String newPassword
) {
}
