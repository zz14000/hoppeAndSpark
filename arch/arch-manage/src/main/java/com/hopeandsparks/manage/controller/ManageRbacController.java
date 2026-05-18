package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台 RBAC 接口，负责菜单、角色、权限、管理员授权和操作日志查询。
 *
 * <p>MVP 做到管理员能登录、能按角色看到菜单、关键按钮/API 能鉴权、关键写操作能追溯。
 * 暂不做组织架构、数据范围权限、字段级权限和审批流。后续这里会读写 {@code sys_admin}、
 * {@code sys_role}、{@code sys_permission}、关系表和 {@code sys_operation_log}。</p>
 */
@RestController
@RequestMapping("/api/v1/manage")
public class ManageRbacController {

    @GetMapping("/menus")
    public ApiResponse<Map<String, Object>> menus() {
        return ApiResponse.ok(PlaceholderData.of("manage", "menus"));
    }

    @GetMapping("/roles")
    public ApiResponse<Map<String, Object>> roles(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "roles", values("query", query)));
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "createRole", values("request", request)));
    }

    @PutMapping("/roles/{roleId}")
    public ApiResponse<Map<String, Object>> updateRole(
            @PathVariable String roleId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateRole", values("roleId", roleId, "request", request)));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ApiResponse<Map<String, Object>> updateRolePermissions(
            @PathVariable String roleId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateRolePermissions", values("roleId", roleId, "request", request)));
    }

    @GetMapping("/permissions")
    public ApiResponse<Map<String, Object>> permissions(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "permissions", values("query", query)));
    }

    @GetMapping("/admins")
    public ApiResponse<Map<String, Object>> admins(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "admins", values("query", query)));
    }

    @PostMapping("/admins")
    public ApiResponse<Map<String, Object>> createAdmin(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "createAdmin", values("request", request)));
    }

    @PutMapping("/admins/{adminId}/roles")
    public ApiResponse<Map<String, Object>> updateAdminRoles(
            @PathVariable String adminId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateAdminRoles", values("adminId", adminId, "request", request)));
    }

    @GetMapping("/operation-logs")
    public ApiResponse<Map<String, Object>> operationLogs(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "operationLogs", values("query", query)));
    }
}
