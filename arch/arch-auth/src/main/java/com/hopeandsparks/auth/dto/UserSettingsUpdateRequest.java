package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "用户设置更新请求")
public record UserSettingsUpdateRequest(
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
        Map<String, Object> learningPreference,

        @Schema(description = "TTS 开关")
        Boolean enableTts,

        @Schema(description = "Ava 弹窗开关")
        Boolean enableAvaPopup,

        @Schema(description = "专注模式开关")
        Boolean enableFocusMode,

        @Schema(description = "是否公开收藏")
        Boolean publicCollection,

        @Schema(description = "主题模式", example = "dark")
        String themeMode,

        @Schema(description = "字体大小", example = "normal")
        String fontScale
) {
}
