package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.manage.service.ManageOperationLogService;
import com.hopeandsparks.manage.vo.OperationLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Manage operation log query entry.
 */
@RestController
@RequestMapping("/api/v1/manage/operation-logs")
public class ManageOperationLogController {

    private final ManageOperationLogService manageOperationLogService;

    public ManageOperationLogController(ManageOperationLogService manageOperationLogService) {
        this.manageOperationLogService = manageOperationLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<OperationLogVO>> operationLogs(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(manageOperationLogService.list(query));
    }
}
