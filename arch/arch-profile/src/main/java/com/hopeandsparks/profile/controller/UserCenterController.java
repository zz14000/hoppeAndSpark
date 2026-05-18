package com.hopeandsparks.profile.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 用户中心聚合接口，负责学习统计和“我的收藏”等展示型接口。
 *
 * <p>收藏本身不归 profile 模块写入：资源收藏在 {@code arch-resource}，文章收藏在
 * {@code arch-community}。这里后续只做聚合查询，方便前端一个页面展示多类用户数据。</p>
 */
@RestController
public class UserCenterController {

    @GetMapping("/api/v1/user/learning-stats")
    public ApiResponse<Map<String, Object>> learningStats() {
        return ApiResponse.ok(PlaceholderData.of("profile", "learningStats"));
    }

    @GetMapping("/api/v1/user/collections")
    public ApiResponse<Map<String, Object>> collections(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("profile", "collections", values("query", query)));
    }

    @PostMapping("/api/v1/user/collections")
    public ApiResponse<Map<String, Object>> toggleCollection(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("profile", "toggleCollection", values("request", request)));
    }

    @DeleteMapping("/api/v1/user/collections/{collectionId}")
    public ApiResponse<Map<String, Object>> deleteCollection(@PathVariable String collectionId) {
        return ApiResponse.ok(PlaceholderData.of("profile", "deleteCollection", values("collectionId", collectionId)));
    }
}
