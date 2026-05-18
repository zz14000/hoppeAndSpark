package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台用户管理接口，负责用户列表、封禁/解封/警告动作和学习轨迹查询。
 *
 * <p>按照 manage 模块定位，这里是后台入口和权限校验层；真正的用户状态流转应调用
 * {@code arch-auth} 的用户 Service，操作完成后写 {@code sys_operation_log}。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/users")
public class ManageUserController {

    @GetMapping
    public ApiResponse<Map<String, Object>> users(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "users", values("query", query)));
    }

    @PostMapping("/{userId}/actions")
    public ApiResponse<Map<String, Object>> userAction(
            @PathVariable String userId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "userAction", values("userId", userId, "request", request)));
    }

    @GetMapping("/{userId}/learning-trace")
    public ApiResponse<Map<String, Object>> learningTrace(@PathVariable String userId) {
        return ApiResponse.ok(PlaceholderData.of("manage", "learningTrace", values("userId", userId)));
    }
}
