package com.hopeandsparks.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "后台管理员登录请求")
public record AdminLoginRequest(
        @Schema(description = "管理员账号", example = "admin")
        @NotBlank(message = "管理员账号不能为空")
        String username,

        @Schema(description = "登录密码", example = "Admin123!")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
