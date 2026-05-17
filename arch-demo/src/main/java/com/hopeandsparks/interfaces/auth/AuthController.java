package com.hopeandsparks.interfaces.auth;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接 API 文档“用户与认证”章节，暴露注册、登录、刷新 token、退出和密码找回相关入口。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "auth", "register", Map.of(), Map.of(), body);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "auth", "login", Map.of(), Map.of(), body);
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "auth", "refresh", Map.of(), Map.of(), body);
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "auth", "logout");
    }

    @PostMapping("/password/reset-request")
    public ApiResponse<Map<String, Object>> resetRequest(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "auth", "passwordResetRequest", Map.of(), Map.of(), body);
    }

    @PostMapping("/password/reset-confirm")
    public ApiResponse<Map<String, Object>> resetConfirm(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "auth", "passwordResetConfirm", Map.of(), Map.of(), body);
    }
}
