package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "前台用户注册请求")
public record UserRegisterRequest(
        @Schema(description = "登录账号", example = "spark001")
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名不能超过50个字符")
        String username,

        @Schema(description = "登录密码", example = "Passw0rd!")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6到64位之间")
        String password,

        @Schema(description = "昵称", example = "小火花")
        @Size(max = 50, message = "昵称不能超过50个字符")
        String nickname,

        @Schema(description = "手机号", example = "13800000000")
        @Size(max = 20, message = "手机号不能超过20个字符")
        String phone,

        @Schema(description = "邮箱", example = "spark@example.com")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过100个字符")
        String email,

        @Schema(description = "设备ID", example = "browser-uuid")
        String deviceId,

        @Schema(description = "设备名称", example = "Chrome on Windows")
        String deviceName,

        @Schema(description = "客户端类型", example = "web")
        String clientType
) {
}
