package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Manage admin menu entry.
 */
@RestController
@RequestMapping("/api/v1/manage/menus")
public class ManageMenuController {

    @GetMapping
    public ApiResponse<Map<String, Object>> menus() {
        return ApiResponse.ok(PlaceholderData.of("manage", "menus"));
    }
}
