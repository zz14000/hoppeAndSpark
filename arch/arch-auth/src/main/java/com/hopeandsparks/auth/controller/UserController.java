package com.hopeandsparks.auth.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 前台用户接口，负责当前用户、公开主页、资料修改、设备安全、密码和邮箱设置。
 *
 * <p>用户身份来自 Authorization 解析后的安全上下文；当前先返回占位数据。
 * 后续应接 {@code UserService} 和 {@code UserDeviceService}，读写 {@code sys_user}、
 * {@code user_settings}、{@code user_login_session} 等表。</p>
 */
@RestController
public class UserController {

    @GetMapping("/api/v1/user/current")
    public ApiResponse<Map<String, Object>> currentUser() {
        return ApiResponse.ok(PlaceholderData.of("auth", "currentUser"));
    }

    @GetMapping("/api/v1/users/{userId}")
    public ApiResponse<Map<String, Object>> userHomepage(@PathVariable String userId) {
        return ApiResponse.ok(PlaceholderData.of("auth", "userHomepage", values("userId", userId)));
    }

    @PutMapping("/api/v1/user/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "updateProfile", values("request", request)));
    }

    @GetMapping("/api/v1/user/devices")
    public ApiResponse<Map<String, Object>> listDevices() {
        return ApiResponse.ok(PlaceholderData.of("auth", "listDevices"));
    }

    @DeleteMapping("/api/v1/user/devices/{deviceId}")
    public ApiResponse<Map<String, Object>> offlineDevice(@PathVariable String deviceId) {
        return ApiResponse.ok(PlaceholderData.of("auth", "offlineDevice", values("deviceId", deviceId)));
    }

    @PutMapping("/api/v1/user/password")
    public ApiResponse<Map<String, Object>> changePassword(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "changePassword", values("request", request)));
    }

    @PutMapping("/api/v1/user/email")
    public ApiResponse<Map<String, Object>> changeEmail(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "changeEmail", values("request", request)));
    }
}
