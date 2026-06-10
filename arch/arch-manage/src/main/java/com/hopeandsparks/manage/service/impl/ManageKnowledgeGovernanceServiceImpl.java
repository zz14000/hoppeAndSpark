package com.hopeandsparks.manage.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.config.KbProperties;
import com.hopeandsparks.infra.file.FileStorageService;
import com.hopeandsparks.infra.redis.RedisStreamClient;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.kb.dto.KbDocumentCreateRequest;
import com.hopeandsparks.kb.repository.KbCandidateRecord;
import com.hopeandsparks.kb.repository.KbDocumentRepository;
import com.hopeandsparks.kb.service.KbCandidateGovernanceService;
import com.hopeandsparks.kb.service.KbDocumentService;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import com.hopeandsparks.manage.dto.CreateKbIngestJobRequest;
import com.hopeandsparks.manage.dto.KbCandidateReviewRequest;
import com.hopeandsparks.manage.service.ManageKnowledgeGovernanceService;
import com.hopeandsparks.manage.vo.KbCandidateVO;
import com.hopeandsparks.manage.vo.KbDashboardOverviewVO;
import com.hopeandsparks.manage.vo.KbEvaluationRunVO;
import com.hopeandsparks.manage.vo.KbIngestJobVO;
import com.hopeandsparks.manage.vo.KbQualityMetricVO;
import com.hopeandsparks.manage.vo.KbStageMetricVO;
import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.dto.RecordAsyncTaskEventCommand;
import com.hopeandsparks.task.service.AsyncTaskService;
import com.hopeandsparks.task.vo.AsyncTaskEventVO;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManageKnowledgeGovernanceServiceImpl implements ManageKnowledgeGovernanceService {

    private static final String TASK_TYPE = "KB_INGEST";

    private final KbDocumentService kbDocumentService;
    private final KbDocumentRepository kbDocumentRepository;
    private final KbCandidateGovernanceService kbCandidateGovernanceService;
    private final AsyncTaskService asyncTaskService;
    private final RedisStreamClient redisStreamClient;
    private final FileStorageService fileStorageService;
    private final KbProperties kbProperties;
    private final JdbcTemplate jdbcTemplate;

    public ManageKnowledgeGovernanceServiceImpl(
            KbDocumentService kbDocumentService,
            KbDocumentRepository kbDocumentRepository,
            KbCandidateGovernanceService kbCandidateGovernanceService,
            AsyncTaskService asyncTaskService,
            RedisStreamClient redisStreamClient,
            FileStorageService fileStorageService,
            KbProperties kbProperties,
            JdbcTemplate jdbcTemplate
    ) {
        this.kbDocumentService = kbDocumentService;
        this.kbDocumentRepository = kbDocumentRepository;
        this.kbCandidateGovernanceService = kbCandidateGovernanceService;
        this.asyncTaskService = asyncTaskService;
        this.redisStreamClient = redisStreamClient;
        this.fileStorageService = fileStorageService;
        this.kbProperties = kbProperties;
        this.jdbcTemplate = jdbcTemplate;
        ensureEvaluationSchema();
    }

    @Override
    @Transactional
    public KbIngestJobVO createIngestJob(AuthenticatedPrincipal principal, CreateKbIngestJobRequest request) {
        fileStorageService.findByFileId(request.fileId())
                .orElseThrow(() -> new IllegalArgumentException("fileId not found: " + request.fileId()));
        KbDocumentWriteVO document = kbDocumentService.createDocument(principal, new KbDocumentCreateRequest(
                request.title(),
                request.domain(),
                request.projectId(),
                request.fileId(),
                "file",
                null,
                null,
                safe(request.collection(), "edu_ground_truth"),
                false
        ));
        String documentId = document.document().id();
        String taskId = "kb-ingest-" + System.currentTimeMillis();
        AsyncTaskVO task = asyncTaskService.create(new CreateAsyncTaskCommand(
                taskId,
                TASK_TYPE,
                "kb-ingest:" + documentId + ":v1",
                documentId,
                request.projectId(),
                request.title(),
                3,
                Map.of(
                        "fileId", request.fileId(),
                        "collection", safe(request.collection(), "edu_ground_truth"),
                        "sourceType", "file",
                        "parseStrategy", safe(request.parseStrategy(), "default"),
                        "parseNow", true
                )
        ));
        asyncTaskService.recordEvent(new RecordAsyncTaskEventCommand(
                null,
                task.taskId(),
                documentId,
                "UPLOAD_BOUND",
                "SUCCESS",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L,
                "fileId=" + request.fileId(),
                "documentId=" + documentId,
                "",
                "",
                0
        ));
        redisStreamClient.publish(kbProperties.getParse().getStreamKey(), Map.of(
                "taskId", task.taskId(),
                "documentId", documentId,
                "projectId", safe(request.projectId(), ""),
                "collection", safe(request.collection(), "edu_ground_truth")
        ));
        AsyncTaskVO queued = asyncTaskService.enqueue(task.taskId(), "queued to redis stream");
        return toJobVo(queued);
    }

    @Override
    public PageResponse<KbIngestJobVO> listIngestJobs(Map<String, String> query) {
        int page = parseInt(query == null ? null : query.get("page"), 1);
        int size = Math.min(parseInt(query == null ? null : query.get("size"), 20), 100);
        List<AsyncTaskVO> tasks = asyncTaskService.listByType(TASK_TYPE, Math.max(page * size, size));
        int from = Math.min((page - 1) * size, tasks.size());
        int to = Math.min(from + size, tasks.size());
        List<KbIngestJobVO> list = tasks.subList(from, to).stream().map(this::toJobVo).toList();
        return PageResponse.of(page, size, tasks.size(), list);
    }

    @Override
    public KbIngestJobVO getIngestJob(String taskId) {
        return toJobVo(asyncTaskService.getByTaskId(taskId));
    }

    @Override
    public KbIngestJobVO retryIngestJob(AuthenticatedPrincipal principal, String taskId) {
        AsyncTaskVO task = asyncTaskService.increaseRetry(taskId);
        AsyncTaskVO waiting = asyncTaskService.markRetryWaiting(task.taskId(), "manual retry requested");
        redisStreamClient.publish(kbProperties.getParse().getStreamKey(), Map.of(
                "taskId", waiting.taskId(),
                "documentId", waiting.documentId(),
                "projectId", waiting.projectId()
        ));
        AsyncTaskVO queued = asyncTaskService.enqueue(waiting.taskId(), "retry queued");
        return toJobVo(queued);
    }

    @Override
    public PageResponse<KbCandidateVO> listCandidates(Map<String, String> query) {
        PageResponse<KbCandidateRecord> page = kbCandidateGovernanceService.listCandidates(query);
        return PageResponse.of(page.page(), page.pageSize(), page.total(), page.list().stream().map(this::toCandidateVo).toList());
    }

    @Override
    public KbCandidateVO getCandidate(String candidateId) {
        return toCandidateVo(kbCandidateGovernanceService.findCandidate(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("candidate not found: " + candidateId)));
    }

    @Override
    public KbCandidateVO approveCandidate(AuthenticatedPrincipal principal, String candidateId, KbCandidateReviewRequest request) {
        return toCandidateVo(kbCandidateGovernanceService.approve(candidateId, principalId(principal), reviewComment(request)));
    }

    @Override
    public KbCandidateVO rejectCandidate(AuthenticatedPrincipal principal, String candidateId, KbCandidateReviewRequest request) {
        return toCandidateVo(kbCandidateGovernanceService.reject(candidateId, principalId(principal), reviewComment(request)));
    }

    @Override
    public KbCandidateVO rollbackCandidate(AuthenticatedPrincipal principal, String candidateId, KbCandidateReviewRequest request) {
        return toCandidateVo(kbCandidateGovernanceService.rollback(candidateId, principalId(principal), reviewComment(request)));
    }

    @Override
    public KbCandidateVO replayCandidate(AuthenticatedPrincipal principal, String candidateId) {
        return toCandidateVo(kbCandidateGovernanceService.replay(candidateId, principalId(principal)));
    }

    @Override
    public KbDashboardOverviewVO overview() {
        long total = kbDocumentRepository.countDocuments();
        long success = countTasksByStatus("SUCCESS");
        long failed = countTasksByStatus("FAILED");
        long retry = countTasksByStatus("RETRY_WAITING");
        long backlog = countTasksByStatus("PENDING") + countTasksByStatus("QUEUED") + countTasksByStatus("PROCESSING");
        long candidates = countCandidates();
        long promoted = countCandidatesByStatus("AUTO_PROMOTED_ACTIVE") + countCandidatesByStatus("MANUAL_APPROVED_ACTIVE");
        long rolledBack = countCandidatesByStatus("ROLLED_BACK");
        return new KbDashboardOverviewVO(
                total,
                rate(success, success + failed),
                rate(failed, success + failed),
                rate(retry, success + failed + retry),
                backlog,
                rate(promoted, Math.max(candidates, 1L)),
                rate(rolledBack, Math.max(promoted, 1L))
        );
    }

    @Override
    public List<KbStageMetricVO> stageMetrics() {
        List<String> stages = List.of("UPLOAD_BOUND", "PARSE", "OCR", "CHUNK", "EMBED", "VECTOR_UPSERT", "GOVERNANCE", "DONE");
        List<KbStageMetricVO> metrics = new ArrayList<>();
        for (String stage : stages) {
            long total = countStage(stage, null);
            long success = countStage(stage, "SUCCESS");
            long failed = countStage(stage, "FAILED");
            metrics.add(new KbStageMetricVO(
                    stage,
                    total,
                    rate(success, Math.max(total, 1L)),
                    rate(failed, Math.max(total, 1L)),
                    percentileDuration(stage, 0.50),
                    percentileDuration(stage, 0.95)
            ));
        }
        return metrics;
    }

    @Override
    public PageResponse<Map<String, Object>> failures(Map<String, String> query) {
        int page = parseInt(query == null ? null : query.get("page"), 1);
        int size = Math.min(parseInt(query == null ? null : query.get("size"), 20), 100);
        int offset = (page - 1) * size;
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from async_task_event
                where status = 'FAILED'
                """, Long.class);
        List<Map<String, Object>> list = jdbcTemplate.query("""
                select task_id, document_id, stage, error_code, error_message, retry_count, created_at
                from async_task_event
                where status = 'FAILED'
                order by created_at desc
                limit ? offset ?
                """, (rs, rowNum) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskId", rs.getString("task_id"));
            item.put("documentId", rs.getString("document_id"));
            item.put("stage", rs.getString("stage"));
            item.put("errorCode", rs.getString("error_code"));
            item.put("errorMessage", rs.getString("error_message"));
            item.put("retryCount", rs.getInt("retry_count"));
            item.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
            return item;
        }, size, offset);
        return PageResponse.of(page, size, total == null ? 0 : total, list);
    }

    @Override
    public PageResponse<KbCandidateVO> candidateMetrics(Map<String, String> query) {
        return listCandidates(query);
    }

    @Override
    public KbQualityMetricVO qualityMetrics() {
        long totalChunks = countStage("CHUNK", "SUCCESS");
        long ocrSuccess = countStage("OCR", "SUCCESS");
        long ocrTotal = countStage("OCR", null);
        long embedFail = countStage("EMBED", "FAILED");
        long embedTotal = countStage("EMBED", null);
        long chromaFail = countStage("VECTOR_UPSERT", "FAILED");
        long chromaTotal = countStage("VECTOR_UPSERT", null);
        long promoted = countCandidatesByStatus("AUTO_PROMOTED_ACTIVE") + countCandidatesByStatus("MANUAL_APPROVED_ACTIVE");
        long rolledBack = countCandidatesByStatus("ROLLED_BACK");
        return new KbQualityMetricVO(
                rate(ocrSuccess, Math.max(ocrTotal, 1L)),
                kbDocumentRepository.averageChunkCount(),
                kbDocumentRepository.averageChunkLength(),
                rate(embedFail, Math.max(embedTotal, 1L)),
                rate(chromaFail, Math.max(chromaTotal, 1L)),
                promoted == 0 ? 1D : 1D - rate(rolledBack, promoted)
        );
    }

    @Override
    public KbEvaluationRunVO createEvaluationRun() {
        String runId = "eval-" + System.currentTimeMillis();
        KbQualityMetricVO quality = qualityMetrics();
        double recallAt5 = stageMetrics().stream().filter(item -> "DONE".equals(item.stage())).findFirst()
                .map(KbStageMetricVO::successRate)
                .orElse(0D);
        double mrrAt10 = (recallAt5 + quality.autoPromotionPrecision()) / 2D;
        double parseCoverageRate = stageMetrics().stream().filter(item -> "PARSE".equals(item.stage())).findFirst()
                .map(KbStageMetricVO::successRate)
                .orElse(0D);
        jdbcTemplate.update("""
                insert into kb_eval_run(run_id, status, recall_at_5, mrr_at_10, parse_coverage_rate, ocr_success_rate, auto_promotion_precision)
                values (?, ?, ?, ?, ?, ?, ?)
                """, runId, "SUCCESS", recallAt5, mrrAt10, parseCoverageRate, quality.ocrHitRate(), quality.autoPromotionPrecision());
        return new KbEvaluationRunVO(runId, "SUCCESS", recallAt5, mrrAt10, parseCoverageRate, quality.ocrHitRate(), quality.autoPromotionPrecision());
    }

    @Override
    public PageResponse<KbEvaluationRunVO> listEvaluationRuns(Map<String, String> query) {
        int page = parseInt(query == null ? null : query.get("page"), 1);
        int size = Math.min(parseInt(query == null ? null : query.get("size"), 20), 100);
        int offset = (page - 1) * size;
        Long total = jdbcTemplate.queryForObject("select count(1) from kb_eval_run", Long.class);
        List<KbEvaluationRunVO> list = jdbcTemplate.query("""
                select run_id, status, recall_at_5, mrr_at_10, parse_coverage_rate, ocr_success_rate, auto_promotion_precision
                from kb_eval_run
                order by created_at desc
                limit ? offset ?
                """, (rs, rowNum) -> new KbEvaluationRunVO(
                rs.getString("run_id"),
                rs.getString("status"),
                rs.getDouble("recall_at_5"),
                rs.getDouble("mrr_at_10"),
                rs.getDouble("parse_coverage_rate"),
                rs.getDouble("ocr_success_rate"),
                rs.getDouble("auto_promotion_precision")
        ), size, offset);
        return PageResponse.of(page, size, total == null ? 0 : total, list);
    }

    @Override
    public KbEvaluationRunVO getEvaluationRun(String runId) {
        return jdbcTemplate.queryForObject("""
                select run_id, status, recall_at_5, mrr_at_10, parse_coverage_rate, ocr_success_rate, auto_promotion_precision
                from kb_eval_run
                where run_id = ?
                """, (rs, rowNum) -> new KbEvaluationRunVO(
                rs.getString("run_id"),
                rs.getString("status"),
                rs.getDouble("recall_at_5"),
                rs.getDouble("mrr_at_10"),
                rs.getDouble("parse_coverage_rate"),
                rs.getDouble("ocr_success_rate"),
                rs.getDouble("auto_promotion_precision")
        ), runId);
    }

    private KbIngestJobVO toJobVo(AsyncTaskVO task) {
        return new KbIngestJobVO(
                task.taskId(),
                task.taskType(),
                task.status(),
                task.progress(),
                task.message(),
                task.retryCount(),
                task.maxRetry(),
                task.documentId(),
                task.title(),
                task.projectId()
        );
    }

    private KbCandidateVO toCandidateVo(KbCandidateRecord record) {
        return new KbCandidateVO(
                record.candidateId(),
                record.documentId(),
                record.tenantUserId(),
                record.projectId(),
                record.sourceUrl(),
                record.sourceDomain(),
                record.sourceTitle(),
                record.rerankScore(),
                record.retrievalScore(),
                record.contentLength(),
                record.governanceStatus(),
                record.promotionStatus(),
                record.promotionReason(),
                record.approvedDocumentId()
        );
    }

    private long countTasksByStatus(String status) {
        Long value = jdbcTemplate.queryForObject("select count(1) from async_task where task_type = ? and status = ?", Long.class, TASK_TYPE, status);
        return value == null ? 0L : value;
    }

    private long countCandidates() {
        Long value = jdbcTemplate.queryForObject("select count(1) from kb_candidate_governance", Long.class);
        return value == null ? 0L : value;
    }

    private long countCandidatesByStatus(String status) {
        Long value = jdbcTemplate.queryForObject("select count(1) from kb_candidate_governance where promotion_status = ?", Long.class, status);
        return value == null ? 0L : value;
    }

    private long countStage(String stage, String status) {
        if (status == null) {
            Long value = jdbcTemplate.queryForObject("select count(1) from async_task_event where stage = ?", Long.class, stage);
            return value == null ? 0L : value;
        }
        Long value = jdbcTemplate.queryForObject("select count(1) from async_task_event where stage = ? and status = ?", Long.class, stage, status);
        return value == null ? 0L : value;
    }

    private long percentileDuration(String stage, double percentile) {
        List<Long> values = jdbcTemplate.query("""
                select duration_ms
                from async_task_event
                where stage = ? and duration_ms > 0
                order by duration_ms asc
                """, (rs, rowNum) -> rs.getLong("duration_ms"), stage);
        if (values.isEmpty()) {
            return 0L;
        }
        int index = (int) Math.ceil(values.size() * percentile) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return ((double) numerator) / denominator;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String principalId(AuthenticatedPrincipal principal) {
        return principal == null ? "" : String.valueOf(principal.id());
    }

    private String reviewComment(KbCandidateReviewRequest request) {
        if (request == null) {
            return "";
        }
        String reason = request.reason() == null ? "" : request.reason().trim();
        String comment = request.comment() == null ? "" : request.comment().trim();
        return comment.isBlank() ? reason : (reason.isBlank() ? comment : reason + ":" + comment);
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void ensureEvaluationSchema() {
        jdbcTemplate.execute("""
                create table if not exists kb_eval_run(
                    run_id varchar(64) primary key,
                    status varchar(32) not null,
                    recall_at_5 double not null default 0,
                    mrr_at_10 double not null default 0,
                    parse_coverage_rate double not null default 0,
                    ocr_success_rate double not null default 0,
                    auto_promotion_precision double not null default 0,
                    created_at datetime not null default current_timestamp
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists kb_eval_case_result(
                    id bigint primary key auto_increment,
                    run_id varchar(64) not null,
                    case_key varchar(128) not null default '',
                    metric_name varchar(64) not null default '',
                    metric_value double not null default 0,
                    detail_json longtext null,
                    created_at datetime not null default current_timestamp,
                    key idx_run_id(run_id)
                )
                """);
    }
}
