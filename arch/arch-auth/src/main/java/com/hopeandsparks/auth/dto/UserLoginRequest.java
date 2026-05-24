package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "前台用户登录请求")
public record UserLoginRequest(
        @Schema(description = "登录账号", example = "spark001")
        @NotBlank(message = "用户名不能为空")
        String username,

        @Schema(description = "登录密码", example = "Passw0rd!")
        @NotBlank(message = "密码不能为空")
        String password,

        @Schema(description = "设备ID", example = "browser-uuid")
        String deviceId,

        @Schema(description = "设备名称", example = "Chrome on Windows")
        String deviceName,

        @Schema(description = "客户端类型", example = "web")
        String clientType
) {
}
