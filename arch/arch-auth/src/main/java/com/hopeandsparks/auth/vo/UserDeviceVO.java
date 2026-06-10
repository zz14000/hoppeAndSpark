package com.hopeandsparks.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "登录设备会话")
public record UserDeviceVO(
        @Schema(description = "会话ID")
        String id,

        @Schema(description = "设备ID")
        String deviceId,

        @Schema(description = "设备名称")
        String deviceName,

        @Schema(description = "客户端类型")
        String clientType,

        @Schema(description = "登录IP")
        String ipAddress,

        @Schema(description = "最后活跃时间")
        LocalDateTime lastActiveAt,

        @Schema(description = "过期时间")
        LocalDateTime expiresAt,

        @Schema(description = "是否当前会话")
        boolean current
) {
}
