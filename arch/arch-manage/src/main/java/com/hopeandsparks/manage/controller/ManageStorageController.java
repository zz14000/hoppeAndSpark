package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台存储监控接口，负责查看 MinIO 文件、生成资源、知识库文件等存储占用概览。
 *
 * <p>该接口以只读统计为主，后续可以聚合 {@code sys_oss_file}、资源版本和知识库文档信息，
 * 用于后台运维页面观察容量和文件类型分布。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/storage")
public class ManageStorageController {

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(PlaceholderData.of("manage", "storageOverview"));
    }
}
