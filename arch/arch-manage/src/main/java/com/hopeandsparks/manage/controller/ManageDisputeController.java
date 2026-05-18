package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台争议工单接口，负责 AI 内容争议列表和处理动作。
 *
 * <p>用户上报的可疑 Agent 回复、资源内容或知识引用会形成工单。管理员处理后可能触发知识库修正、
 * 资源版本调整、Prompt 修改或重新生成任务，并需要写操作日志。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/ai-disputes")
public class ManageDisputeController {

    @GetMapping
    public ApiResponse<Map<String, Object>> disputes(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "disputes", values("query", query)));
    }

    @PutMapping("/{disputeId}")
    public ApiResponse<Map<String, Object>> handleDispute(
            @PathVariable String disputeId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "handleDispute", values("disputeId", disputeId, "request", request)));
    }
}
