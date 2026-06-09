package com.hopeandsparks.auth.controller;

import com.hopeandsparks.auth.dto.UserChangeEmailRequest;
import com.hopeandsparks.auth.dto.UserChangePasswordRequest;
import com.hopeandsparks.auth.service.UserAuthService;
import com.hopeandsparks.auth.vo.UserDeviceVO;
import com.hopeandsparks.auth.vo.UserProfileVO;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台用户接口，负责当前用户、公开主页、资料修改、设备安全、密码和邮箱设置。
 *
 * <p>用户身份来自 Authorization 解析后的安全上下文；当前先返回占位数据。
 * 后续应接 {@code UserService} 和 {@code UserDeviceService}，读写 {@code sys_user}、
 * {@code user_settings}、{@code user_login_session} 等表。</p>
 */
@Tag(name = "前台用户", description = "当前用户、公开主页和账号设置")
@RestController
public class UserController {

    private final UserAuthService userAuthService;

    public UserController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @Operation(summary = "获取当前登录用户", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/v1/user/current")
    public ApiResponse<UserProfileVO> currentUser(Authentication authentication) {
        return ApiResponse.ok(userAuthService.currentUser(principal(authentication)));
    }

    @GetMapping("/api/v1/user/devices")
    public ApiResponse<List<UserDeviceVO>> listDevices(Authentication authentication) {
        return ApiResponse.ok(userAuthService.listDevices(principal(authentication)));
    }

    @DeleteMapping("/api/v1/user/devices/{sessionId}")
    public ApiResponse<Void> offlineDevice(Authentication authentication, @PathVariable String sessionId) {
        userAuthService.offlineDevice(principal(authentication), sessionId);
        return ApiResponse.ok("设备已下线", null);
    }

    @PutMapping("/api/v1/user/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody UserChangePasswordRequest request
    ) {
        userAuthService.changePassword(principal(authentication), request);
        return ApiResponse.ok("密码修改成功，请重新登录", null);
    }

    @PutMapping("/api/v1/user/email")
    public ApiResponse<Void> changeEmail(
            Authentication authentication,
            @Valid @RequestBody UserChangeEmailRequest request
    ) {
        userAuthService.changeEmail(principal(authentication), request);
        return ApiResponse.ok("邮箱更新成功", null);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
