package com.hopeandsparks.im.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 好友申请接口，负责发起好友申请和处理申请。
 *
 * <p>该能力属于 IM/社交后置模块。后续会结合用户关系表、通知模块和风控策略，
 * 完成申请、同意、拒绝、重复申请校验等流程。</p>
 */
@RestController
@RequestMapping("/api/v1/friend-requests")
public class FriendRequestController {

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("im", "createFriendRequest", values("request", request)));
    }

    @PutMapping("/{requestId}")
    public ApiResponse<Map<String, Object>> handle(
            @PathVariable String requestId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("im", "handleFriendRequest", values("requestId", requestId, "request", request)));
    }
}
