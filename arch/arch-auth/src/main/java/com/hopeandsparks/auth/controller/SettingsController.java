package com.hopeandsparks.auth.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 用户设置接口，处理偏好设置、通知设置和本地缓存清理等用户中心能力。
 *
 * <p>设置数据主要落在 {@code user_settings}，缓存清理后续会调用 Redis 或客户端协同逻辑。
 * 这里不处理认证本身，只处理登录用户的设置读写。</p>
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    @GetMapping
    public ApiResponse<Map<String, Object>> getSettings() {
        return ApiResponse.ok(PlaceholderData.of("auth", "getSettings"));
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> updateSettings(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "updateSettings", values("request", request)));
    }

    @PostMapping("/cache/clear")
    public ApiResponse<Map<String, Object>> clearCache() {
        return ApiResponse.ok(PlaceholderData.of("auth", "clearCache"));
    }
}
