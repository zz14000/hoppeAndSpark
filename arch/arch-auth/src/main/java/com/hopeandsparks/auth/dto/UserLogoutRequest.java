package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "前台退出登录请求")
public record UserLogoutRequest(
        @Schema(description = "可选，传入后会同步失效 user_login_session", example = "4a5f3b9c...")
        String sessionToken
) {
}
