package com.hopeandsparks.auth.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 前台认证接口，承接注册、登录、刷新 token、退出登录和找回密码。
 *
 * <p>这个 Controller 只负责暴露 API 文档中的路径和接收请求体，后续真正实现时应调用
 * {@code AuthService} 完成密码加密、用户校验、token 签发、登录会话落库和 Redis 登录态维护。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "register", values("request", request)));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "login", values("request", request)));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "refresh", values("request", request)));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        return ApiResponse.ok(PlaceholderData.of("auth", "logout"));
    }

    @PostMapping("/password/reset-request")
    public ApiResponse<Map<String, Object>> requestPasswordReset(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "requestPasswordReset", values("request", request)));
    }

    @PostMapping("/password/reset-confirm")
    public ApiResponse<Map<String, Object>> confirmPasswordReset(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("auth", "confirmPasswordReset", values("request", request)));
    }
}
