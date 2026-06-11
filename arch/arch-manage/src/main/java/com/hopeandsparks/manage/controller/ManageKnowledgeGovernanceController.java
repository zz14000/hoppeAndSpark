package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.dto.AgentRunResumeRequest;
import com.hopeandsparks.manage.dto.CreateKbIngestJobRequest;
import com.hopeandsparks.manage.dto.KbCandidateReviewRequest;
import com.hopeandsparks.manage.service.ManageKnowledgeGovernanceService;
import com.hopeandsparks.manage.service.ManageOperationLogService;
import com.hopeandsparks.manage.vo.AgentRunVO;
import com.hopeandsparks.manage.vo.KbCandidateVO;
import com.hopeandsparks.manage.vo.KbDashboardOverviewVO;
import com.hopeandsparks.manage.vo.KbEvaluationRunVO;
import com.hopeandsparks.manage.vo.KbIngestJobVO;
import com.hopeandsparks.manage.vo.KbQualityMetricVO;
import com.hopeandsparks.manage.vo.KbStageMetricVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/manage/knowledge-base")
public class ManageKnowledgeGovernanceController {

    private final ManageKnowledgeGovernanceService governanceService;
    private final ManageOperationLogService operationLogService;

    public ManageKnowledgeGovernanceController(
            ManageKnowledgeGovernanceService governanceService,
            ManageOperationLogService operationLogService
    ) {
        this.governanceService = governanceService;
        this.operationLogService = operationLogService;
    }

    @PostMapping("/ingest-jobs")
    public ApiResponse<KbIngestJobVO> createIngestJob(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @Valid @RequestBody CreateKbIngestJobRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbIngestJobVO result = governanceService.createIngestJob(principal, request);
        operationLogService.record(principal, "kb", "create_ingest_job", "async_task", null, "create kb ingest job: " + request.title(), servletRequest);
        return ApiResponse.ok("kb ingest job created", result);
    }

    @GetMapping("/ingest-jobs")
    public ApiResponse<PageResponse<KbIngestJobVO>> ingestJobs(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(governanceService.listIngestJobs(query));
    }

    @GetMapping("/ingest-jobs/{taskId}")
    public ApiResponse<KbIngestJobVO> ingestJob(@PathVariable String taskId) {
        return ApiResponse.ok(governanceService.getIngestJob(taskId));
    }

    @PostMapping("/ingest-jobs/{taskId}/retry")
    public ApiResponse<KbIngestJobVO> retryIngestJob(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String taskId
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbIngestJobVO result = governanceService.retryIngestJob(principal, taskId);
        operationLogService.record(principal, "kb", "retry_ingest_job", "async_task", null, "retry kb ingest job: " + taskId, servletRequest);
        return ApiResponse.ok(result);
    }

    @GetMapping("/candidates")
    public ApiResponse<PageResponse<KbCandidateVO>> candidates(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(governanceService.listCandidates(query));
    }

    @GetMapping("/candidates/{candidateId}")
    public ApiResponse<KbCandidateVO> candidate(@PathVariable String candidateId) {
        return ApiResponse.ok(governanceService.getCandidate(candidateId));
    }

    @PostMapping("/candidates/{candidateId}/approve")
    public ApiResponse<KbCandidateVO> approve(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String candidateId,
            @RequestBody(required = false) KbCandidateReviewRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbCandidateVO result = governanceService.approveCandidate(principal, candidateId, request);
        operationLogService.record(principal, "kb", "approve_candidate", "kb_candidate", null, "approve kb candidate: " + candidateId, servletRequest);
        return ApiResponse.ok(result);
    }

    @PostMapping("/candidates/{candidateId}/reject")
    public ApiResponse<KbCandidateVO> reject(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String candidateId,
            @RequestBody(required = false) KbCandidateReviewRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbCandidateVO result = governanceService.rejectCandidate(principal, candidateId, request);
        operationLogService.record(principal, "kb", "reject_candidate", "kb_candidate", null, "reject kb candidate: " + candidateId, servletRequest);
        return ApiResponse.ok(result);
    }

    @PostMapping("/candidates/{candidateId}/rollback")
    public ApiResponse<KbCandidateVO> rollback(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String candidateId,
            @RequestBody(required = false) KbCandidateReviewRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbCandidateVO result = governanceService.rollbackCandidate(principal, candidateId, request);
        operationLogService.record(principal, "kb", "rollback_candidate", "kb_candidate", null, "rollback kb candidate: " + candidateId, servletRequest);
        return ApiResponse.ok(result);
    }

    @PostMapping("/candidates/{candidateId}/replay")
    public ApiResponse<KbCandidateVO> replay(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String candidateId
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbCandidateVO result = governanceService.replayCandidate(principal, candidateId);
        operationLogService.record(principal, "kb", "replay_candidate", "kb_candidate", null, "replay kb candidate: " + candidateId, servletRequest);
        return ApiResponse.ok(result);
    }

    @GetMapping("/dashboard/overview")
    public ApiResponse<KbDashboardOverviewVO> overview() {
        return ApiResponse.ok(governanceService.overview());
    }

    @GetMapping("/dashboard/stages")
    public ApiResponse<List<KbStageMetricVO>> stages() {
        return ApiResponse.ok(governanceService.stageMetrics());
    }

    @GetMapping("/dashboard/failures")
    public ApiResponse<PageResponse<Map<String, Object>>> failures(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(governanceService.failures(query));
    }

    @GetMapping("/dashboard/candidates")
    public ApiResponse<PageResponse<KbCandidateVO>> candidateMetrics(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(governanceService.candidateMetrics(query));
    }

    @GetMapping("/dashboard/quality")
    public ApiResponse<KbQualityMetricVO> quality() {
        return ApiResponse.ok(governanceService.qualityMetrics());
    }

    @PostMapping("/evaluations/runs")
    public ApiResponse<KbEvaluationRunVO> createEvaluationRun() {
        return ApiResponse.ok(governanceService.createEvaluationRun());
    }

    @GetMapping("/evaluations/runs")
    public ApiResponse<PageResponse<KbEvaluationRunVO>> evaluationRuns(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(governanceService.listEvaluationRuns(query));
    }

    @GetMapping("/evaluations/runs/{runId}")
    public ApiResponse<KbEvaluationRunVO> evaluationRun(@PathVariable String runId) {
        return ApiResponse.ok(governanceService.getEvaluationRun(runId));
    }

    @GetMapping("/agent-runs")
    public ApiResponse<PageResponse<AgentRunVO>> agentRuns(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(governanceService.listAgentRuns(query));
    }

    @GetMapping("/agent-runs/{runId}")
    public ApiResponse<AgentRunVO> agentRun(@PathVariable String runId) {
        return ApiResponse.ok(governanceService.getAgentRun(runId));
    }

    @GetMapping("/agent-runs/{runId}/events")
    public ApiResponse<PageResponse<Map<String, Object>>> agentRunEvents(
            @PathVariable String runId,
            @RequestParam Map<String, String> query
    ) {
        return ApiResponse.ok(governanceService.listAgentRunEvents(runId, query));
    }

    @GetMapping("/agent-runs/{runId}/checkpoints")
    public ApiResponse<PageResponse<Map<String, Object>>> agentRunCheckpoints(
            @PathVariable String runId,
            @RequestParam Map<String, String> query
    ) {
        return ApiResponse.ok(governanceService.listAgentRunCheckpoints(runId, query));
    }

    @PostMapping("/agent-runs/{runId}/resume")
    public ApiResponse<AgentRunVO> resumeAgentRun(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String runId,
            @RequestBody(required = false) AgentRunResumeRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        AgentRunVO result = governanceService.resumeAgentRun(principal, runId, request);
        operationLogService.record(principal, "agent", "resume_run", "agent_run", null, "resume agent run: " + runId, servletRequest);
        return ApiResponse.ok(result);
    }

    @PostMapping("/agent-runs/{runId}/replay")
    public ApiResponse<AgentRunVO> replayAgentRun(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String runId
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        AgentRunVO result = governanceService.replayAgentRun(principal, runId);
        operationLogService.record(principal, "agent", "replay_run", "agent_run", null, "replay agent run: " + runId, servletRequest);
        return ApiResponse.ok(result);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
