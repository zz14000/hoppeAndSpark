package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.SecurityProperties;
import com.hopeandsparks.manage.dto.AdminLoginRequest;
import com.hopeandsparks.manage.dto.AdminRegisterRequest;
import com.hopeandsparks.manage.service.ManageAuthService;
import com.hopeandsparks.manage.vo.AdminAuthTokenVO;
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
 * 后台登录接口，负责 sys_admin 管理员账号登录和后续后台 token 签发。
 *
 * <p>后台管理员身份独立于前台用户，数据来自 {@code sys_admin} 和后台 RBAC 表。
 * 后续实现时应校验管理员状态、角色权限，并记录必要的登录审计日志。</p>
 */
@Tag(name = "后台认证", description = "Manage 后台管理员初始化注册、登录和退出")
@RestController
@RequestMapping("/api/v1/manage/auth")
public class ManageAuthController {

    private final ManageAuthService manageAuthService;
    private final SecurityProperties securityProperties;

    public ManageAuthController(ManageAuthService manageAuthService, SecurityProperties securityProperties) {
        this.manageAuthService = manageAuthService;
        this.securityProperties = securityProperties;
    }

    @Operation(summary = "初始化后台管理员", description = "仅当 sys_admin 为空时允许注册首个超级管理员，并自动授予全部菜单与 Controller 资源")
    @PostMapping("/register")
    public ApiResponse<AdminAuthTokenVO> register(
            @Valid @RequestBody AdminRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok("初始化管理员成功", manageAuthService.register(request, servletRequest));
    }

    @Operation(summary = "后台管理员登录", description = "校验 sys_admin 后签发 JWT，并写入 Redis key：auth:admin:token:{token}")
    @PostMapping("/login")
    public ApiResponse<AdminAuthTokenVO> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok("登录成功", manageAuthService.login(request, servletRequest));
    }

    @Operation(summary = "后台管理员退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        manageAuthService.logout(extractToken(authorization));
        return ApiResponse.ok("退出登录成功", null);
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
