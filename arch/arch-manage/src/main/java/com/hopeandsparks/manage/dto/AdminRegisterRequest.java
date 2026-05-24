package com.hopeandsparks.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "后台管理员初始化注册请求")
public record AdminRegisterRequest(
        @Schema(description = "管理员账号", example = "admin")
        @NotBlank(message = "管理员账号不能为空")
        @Size(max = 50, message = "管理员账号不能超过50个字符")
        String username,

        @Schema(description = "真实姓名", example = "系统管理员")
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 50, message = "真实姓名不能超过50个字符")
        String realName,

        @Schema(description = "登录密码", example = "Admin123!")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6到64位之间")
        String password
) {
}
