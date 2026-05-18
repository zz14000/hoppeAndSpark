package com.hopeandsparks.agent.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * AI 内容争议上报接口，供用户举报可疑的 Agent 回复、生成资源或知识引用。
 *
 * <p>后续会创建 {@code feedback_ticket}，由 Manage 后台复核。复核通过后可能触发知识库修正、
 * 资源版本回滚、Prompt 调整或重新生成任务。</p>
 */
@RestController
public class AiDisputeController {

    @PostMapping("/api/v1/ai-disputes")
    public ApiResponse<Map<String, Object>> report(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("agent", "reportDispute", values("request", request)));
    }
}
