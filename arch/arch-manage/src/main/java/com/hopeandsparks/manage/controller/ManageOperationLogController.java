package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * Manage operation log query entry.
 */
@RestController
@RequestMapping("/api/v1/manage/operation-logs")
public class ManageOperationLogController {

    @GetMapping
    public ApiResponse<Map<String, Object>> operationLogs(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "operationLogs", values("query", query)));
    }
}
