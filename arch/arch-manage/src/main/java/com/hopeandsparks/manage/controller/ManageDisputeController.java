package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.dto.DisputeHandleRequest;
import com.hopeandsparks.manage.service.ManageDisputeService;
import com.hopeandsparks.manage.service.ManageOperationLogService;
import com.hopeandsparks.manage.vo.ManageDisputeHandleVO;
import com.hopeandsparks.manage.vo.ManageDisputeVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Manage AI dispute ticket APIs.
 */
@RestController
@RequestMapping("/api/v1/manage/ai-disputes")
public class ManageDisputeController {

    private final ManageDisputeService manageDisputeService;
    private final ManageOperationLogService manageOperationLogService;

    public ManageDisputeController(
            ManageDisputeService manageDisputeService,
            ManageOperationLogService manageOperationLogService
    ) {
        this.manageDisputeService = manageDisputeService;
        this.manageOperationLogService = manageOperationLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ManageDisputeVO>> disputes(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(manageDisputeService.list(query));
    }

    @PutMapping("/{disputeId}")
    public ApiResponse<ManageDisputeHandleVO> handleDispute(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String disputeId,
            @Valid @RequestBody DisputeHandleRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        ManageDisputeHandleVO result = manageDisputeService.handle(principal, disputeId, request);
        manageOperationLogService.record(
                principal,
                "dispute",
                "handle_dispute",
                "feedback_ticket",
                parseTargetId(disputeId),
                "handle dispute, status=" + result.dispute().status(),
                servletRequest
        );
        return ApiResponse.ok(result);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }

    private Long parseTargetId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
