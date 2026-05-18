package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台资源管理接口，负责生成资源历史列表和后台删除入口。
 *
 * <p>资源的上下架、删除、质检状态流转仍归 {@code arch-resource} 负责。manage 只做后台入口、
 * 权限校验、页面 VO 组装和操作日志。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/resources")
public class ManageResourceController {

    @GetMapping
    public ApiResponse<Map<String, Object>> resources(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "resources", values("query", query)));
    }

    @DeleteMapping("/{resourceId}")
    public ApiResponse<Map<String, Object>> deleteResource(@PathVariable String resourceId) {
        return ApiResponse.ok(PlaceholderData.of("manage", "deleteResource", values("resourceId", resourceId)));
    }
}
