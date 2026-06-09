package com.hopeandsparks.auth.controller;

import com.hopeandsparks.auth.dto.PasswordResetConfirmRequest;
import com.hopeandsparks.auth.dto.PasswordResetRequest;
import com.hopeandsparks.auth.dto.UserLoginRequest;
import com.hopeandsparks.auth.dto.UserLogoutRequest;
import com.hopeandsparks.auth.dto.UserRefreshRequest;
import com.hopeandsparks.auth.dto.UserRegisterRequest;
import com.hopeandsparks.auth.service.UserAuthService;
import com.hopeandsparks.auth.vo.PasswordResetTokenVO;
import com.hopeandsparks.auth.vo.UserAuthTokenVO;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.SecurityProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台认证接口，承接注册、登录、session token 刷新 access token、退出登录和找回密码。
 *
 * <p>这个 Controller 只负责暴露 API 文档中的路径和接收请求体，后续真正实现时应调用
 * {@code AuthService} 完成密码加密、用户校验、access token 签发、登录会话落库和 Redis 登录态维护。</p>
 */
@Tag(name = "前台认证", description = "Spark 前台注册、登录、刷新和退出登录")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserAuthService userAuthService;
    private final SecurityProperties securityProperties;

    public AuthController(UserAuthService userAuthService, SecurityProperties securityProperties) {
        this.userAuthService = userAuthService;
        this.securityProperties = securityProperties;
    }

    @Operation(summary = "前台用户注册", description = "写入 sys_user、user_settings、user_login_session，并按 auth:user:token:{token} 写入 Redis 登录态")
    @PostMapping("/register")
    public ApiResponse<UserAuthTokenVO> register(
            @Valid @RequestBody UserRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok("注册成功", userAuthService.register(request, servletRequest));
    }

    @Operation(summary = "前台用户登录", description = "校验 sys_user 密码后签发 JWT，并写入 Redis key：auth:user:token:{token}")
    @PostMapping("/login")
    public ApiResponse<UserAuthTokenVO> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok("登录成功", userAuthService.login(request, servletRequest));
    }

    @Operation(summary = "刷新前台 access token", description = "通过 user_login_session.session_token 重新签发 access token")
    @PostMapping("/refresh")
    public ApiResponse<UserAuthTokenVO> refresh(
            @Valid @RequestBody UserRefreshRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.ok(userAuthService.refresh(request, extractToken(authorization)));
    }

    @Operation(summary = "前台用户退出登录", description = "删除 Redis 登录态，并可同步失效 user_login_session")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody(required = false) UserLogoutRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        userAuthService.logout(request, extractToken(authorization));
        return ApiResponse.ok("退出登录成功", null);
    }

    @PostMapping("/password/reset-request")
    public ApiResponse<PasswordResetTokenVO> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return ApiResponse.ok("重置凭证已生成", userAuthService.requestPasswordReset(request));
    }

    @PostMapping("/password/reset-confirm")
    public ApiResponse<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        userAuthService.confirmPasswordReset(request);
        return ApiResponse.ok("密码重置成功", null);
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String prefix = securityProperties.getJwt().getTokenPrefix();
        if (authorization.startsWith(prefix + " ")) {
            return authorization.substring(prefix.length() + 1);
        }
        if (authorization.startsWith(prefix)) {
            return authorization.substring(prefix.length());
        }
        return authorization;
    }
}
