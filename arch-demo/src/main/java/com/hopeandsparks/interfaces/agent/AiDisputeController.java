package com.hopeandsparks.interfaces.agent;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接前台 AI 内容争议举报入口，后续会创建 feedback_ticket 并进入 Manage 复核闭环。
 */
@RestController
@RequestMapping("/api/v1/ai-disputes")
public class AiDisputeController {

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "agent", "aiDisputeCreate", Map.of(), Map.of(), body);
    }
}
