package com.hopeandsparks.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "用户设置响应")
public record UserSettingsVO(
        @Schema(description = "通用设置")
        Map<String, Object> general,

        @Schema(description = "Agent 设置")
        Map<String, Object> agent,

        @Schema(description = "主题设置")
        Map<String, Object> theme,

        @Schema(description = "通知设置")
        Map<String, Object> notification,

        @Schema(description = "隐私设置")
        Map<String, Object> privacy,

        @Schema(description = "学习偏好")
        Map<String, Object> learningPreference
) {
}
