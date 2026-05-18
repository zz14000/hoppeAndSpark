package com.hopeandsparks.community.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 社区关注接口，负责关注和取消关注其他用户。
 *
 * <p>关注关系服务于社区作者页、动态流和互动提醒。当前先占住 API 契约，后续可复用
 * 用户关系表或单独扩展关注关系表。</p>
 */
@RestController
public class FollowController {

    @PostMapping("/api/v1/users/{userId}/follow")
    public ApiResponse<Map<String, Object>> follow(@PathVariable String userId) {
        return ApiResponse.ok(PlaceholderData.of("community", "follow", values("userId", userId)));
    }

    @DeleteMapping("/api/v1/users/{userId}/follow")
    public ApiResponse<Map<String, Object>> unfollow(@PathVariable String userId) {
        return ApiResponse.ok(PlaceholderData.of("community", "unfollow", values("userId", userId)));
    }
}
