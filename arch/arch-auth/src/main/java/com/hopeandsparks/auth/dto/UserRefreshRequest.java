package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "前台 access token 刷新请求")
public record UserRefreshRequest(
        @Schema(description = "登录会话令牌，来自登录响应 sessionToken", example = "4a5f3b9c...")
        @NotBlank(message = "sessionToken不能为空")
        String sessionToken
) {
}
