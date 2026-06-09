package com.hopeandsparks.agent.controller;

import com.hopeandsparks.agent.dto.AiDisputeCreateRequest;
import com.hopeandsparks.agent.service.AiDisputeService;
import com.hopeandsparks.agent.vo.AiDisputeVO;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 内容争议上报接口，只负责收集用户反馈，处理决策交给 Manage。
 */
@RestController
public class AiDisputeController {

    private final AiDisputeService aiDisputeService;

    public AiDisputeController(AiDisputeService aiDisputeService) {
        this.aiDisputeService = aiDisputeService;
    }

    @PostMapping("/api/v1/ai-disputes")
    public ApiResponse<AiDisputeVO> report(
            Authentication authentication,
            @Valid @RequestBody AiDisputeCreateRequest request
    ) {
        return ApiResponse.ok("争议已提交", aiDisputeService.report(principal(authentication), request));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
