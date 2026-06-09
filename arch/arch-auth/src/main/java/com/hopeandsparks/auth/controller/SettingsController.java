package com.hopeandsparks.auth.controller;

import com.hopeandsparks.auth.dto.UserSettingsUpdateRequest;
import com.hopeandsparks.auth.service.UserAuthService;
import com.hopeandsparks.auth.vo.UserSettingsVO;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户设置接口，处理偏好设置、通知设置和本地缓存清理等用户中心能力。
 *
 * <p>设置数据主要落在 {@code user_settings}，缓存清理后续会调用 Redis 或客户端协同逻辑。
 * 这里不处理认证本身，只处理登录用户的设置读写。</p>
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final UserAuthService userAuthService;

    public SettingsController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @GetMapping
    public ApiResponse<UserSettingsVO> getSettings(Authentication authentication) {
        return ApiResponse.ok(userAuthService.getSettings(principal(authentication)));
    }

    @PutMapping
    public ApiResponse<UserSettingsVO> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UserSettingsUpdateRequest request
    ) {
        return ApiResponse.ok("设置已更新", userAuthService.updateSettings(principal(authentication), request));
    }

    @PostMapping("/cache/clear")
    public ApiResponse<Void> clearCache(Authentication authentication) {
        userAuthService.clearCache(principal(authentication));
        return ApiResponse.ok("缓存清理成功", null);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
