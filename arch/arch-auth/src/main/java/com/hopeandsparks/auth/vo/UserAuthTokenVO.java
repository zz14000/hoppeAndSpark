package com.hopeandsparks.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "前台用户认证响应")
public record UserAuthTokenVO(
        @Schema(description = "JWT access token")
        String accessToken,

        @Schema(description = "请求头 token 类型", example = "Bearer")
        String tokenType,

        @Schema(description = "过期秒数", example = "604800")
        long expiresIn,

        @Schema(description = "用户会话令牌，用于刷新 access token")
        String sessionToken,

        @Schema(description = "用户资料")
        UserProfileVO user
) {
}
