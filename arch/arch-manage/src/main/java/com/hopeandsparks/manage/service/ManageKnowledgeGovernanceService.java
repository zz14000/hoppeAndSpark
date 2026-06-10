package com.hopeandsparks.manage.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.dto.CreateKbIngestJobRequest;
import com.hopeandsparks.manage.dto.KbCandidateReviewRequest;
import com.hopeandsparks.manage.vo.KbCandidateVO;
import com.hopeandsparks.manage.vo.KbDashboardOverviewVO;
import com.hopeandsparks.manage.vo.KbEvaluationRunVO;
import com.hopeandsparks.manage.vo.KbIngestJobVO;
import com.hopeandsparks.manage.vo.KbQualityMetricVO;
import com.hopeandsparks.manage.vo.KbStageMetricVO;

import java.util.List;
import java.util.Map;

public interface ManageKnowledgeGovernanceService {

    KbIngestJobVO createIngestJob(AuthenticatedPrincipal principal, CreateKbIngestJobRequest request);

    PageResponse<KbIngestJobVO> listIngestJobs(Map<String, String> query);

    KbIngestJobVO getIngestJob(String taskId);

    KbIngestJobVO retryIngestJob(AuthenticatedPrincipal principal, String taskId);

    PageResponse<KbCandidateVO> listCandidates(Map<String, String> query);

    KbCandidateVO getCandidate(String candidateId);

    KbCandidateVO approveCandidate(AuthenticatedPrincipal principal, String candidateId, KbCandidateReviewRequest request);

    KbCandidateVO rejectCandidate(AuthenticatedPrincipal principal, String candidateId, KbCandidateReviewRequest request);

    KbCandidateVO rollbackCandidate(AuthenticatedPrincipal principal, String candidateId, KbCandidateReviewRequest request);

    KbCandidateVO replayCandidate(AuthenticatedPrincipal principal, String candidateId);

    KbDashboardOverviewVO overview();

    List<KbStageMetricVO> stageMetrics();

    PageResponse<Map<String, Object>> failures(Map<String, String> query);

    PageResponse<KbCandidateVO> candidateMetrics(Map<String, String> query);

    KbQualityMetricVO qualityMetrics();

    KbEvaluationRunVO createEvaluationRun();

    PageResponse<KbEvaluationRunVO> listEvaluationRuns(Map<String, String> query);

    KbEvaluationRunVO getEvaluationRun(String runId);
}
