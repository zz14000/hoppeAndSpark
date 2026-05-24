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
 * Manage role and role authorization entry.
 */
@RestController
@RequestMapping("/api/v1/manage/roles")
public class ManageRoleController {

    @GetMapping
    public ApiResponse<Map<String, Object>> roles(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "roles", values("query", query)));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createRole(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "createRole", values("request", request)));
    }

    @PutMapping("/{roleId}")
    public ApiResponse<Map<String, Object>> updateRole(
            @PathVariable String roleId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateRole", values("roleId", roleId, "request", request)));
    }

}
