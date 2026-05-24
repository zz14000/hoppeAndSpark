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
 * Manage admin account entry.
 */
@RestController
@RequestMapping("/api/v1/manage/admins")
public class ManageAdminController {

    @GetMapping
    public ApiResponse<Map<String, Object>> admins(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "admins", values("query", query)));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createAdmin(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "createAdmin", values("request", request)));
    }

    @PutMapping("/{adminId}/roles")
    public ApiResponse<Map<String, Object>> updateAdminRoles(
            @PathVariable String adminId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateAdminRoles", values("adminId", adminId, "request", request)));
    }
}
