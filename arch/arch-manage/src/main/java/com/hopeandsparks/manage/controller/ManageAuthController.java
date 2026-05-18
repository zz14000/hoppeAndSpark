package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台登录接口，负责管理员账号登录和后续后台 token 签发。
 *
 * <p>后台管理员身份独立于前台用户，数据来自 {@code sys_admin} 和后台 RBAC 表。
 * 后续实现时应校验管理员状态、角色权限，并记录必要的登录审计日志。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/auth")
public class ManageAuthController {

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "adminLogin", values("request", request)));
    }
}
