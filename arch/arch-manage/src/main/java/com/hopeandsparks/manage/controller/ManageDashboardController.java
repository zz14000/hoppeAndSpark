package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台数据看板接口，负责聚合用户、资源、知识库、社区审核、Agent 调用等只读统计。
 *
 * <p>这个 Controller 可以调用专用只读 Mapper 或各业务模块查询 Service，但不直接修改业务状态。
 * 它服务后台首页和运营概览页面。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/dashboard")
public class ManageDashboardController {

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(PlaceholderData.of("manage", "dashboardOverview"));
    }
}
