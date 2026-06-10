package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "找回密码验证码请求")
public record PasswordResetRequest(
        @Schema(description = "邮箱", example = "spark@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email
) {
}
