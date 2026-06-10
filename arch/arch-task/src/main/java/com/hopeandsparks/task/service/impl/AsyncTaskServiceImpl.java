package com.hopeandsparks.task.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.dto.RecordAsyncTaskEventCommand;
import com.hopeandsparks.task.enums.AsyncTaskStatus;
import com.hopeandsparks.task.service.AsyncTaskService;
import com.hopeandsparks.task.vo.AsyncTaskEventVO;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AsyncTaskVO> taskMapper = (rs, rowNum) -> new AsyncTaskVO(
            rs.getString("task_id"),
            rs.getString("task_type"),
            rs.getString("idempotent_key"),
            rs.getString("document_id"),
            rs.getString("project_id"),
            rs.getString("title"),
            rs.getString("status"),
            rs.getInt("progress"),
            rs.getString("message"),
            rs.getString("external_run_id"),
            rs.getInt("retry_count"),
            rs.getInt("max_retry"),
            rs.getString("failure_reason"),
            rs.getString("payload_json"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    private final RowMapper<AsyncTaskEventVO> eventMapper = (rs, rowNum) -> new AsyncTaskEventVO(
            rs.getString("event_id"),
            rs.getString("task_id"),
            rs.getString("document_id"),
            rs.getString("stage"),
            rs.getString("status"),
            rs.getTimestamp("started_at") == null ? null : rs.getTimestamp("started_at").toLocalDateTime(),
            rs.getTimestamp("finished_at") == null ? null : rs.getTimestamp("finished_at").toLocalDateTime(),
            rs.getLong("duration_ms"),
            rs.getString("input_summary"),
            rs.getString("output_summary"),
            rs.getString("error_code"),
            rs.getString("error_message"),
            rs.getInt("retry_count"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public AsyncTaskServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    @Override
    public AsyncTaskVO create(CreateAsyncTaskCommand command) {
        String taskId = blank(command.taskId()) ? "task-" + System.currentTimeMillis() : command.taskId();
        if (findByTaskId(taskId).isPresent()) {
            return getByTaskId(taskId);
        }
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into async_task(task_id, task_type, idempotent_key, status, progress, message, external_run_id,
                                           retry_count, max_retry, failure_reason, payload_json, document_id, project_id, title)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.NO_GENERATED_KEYS);
            ps.setString(1, taskId);
            ps.setString(2, safe(command.taskType(), "agent_generation"));
            ps.setString(3, safe(command.idempotentKey(), taskId));
            ps.setString(4, AsyncTaskStatus.PENDING.name());
            ps.setInt(5, 0);
            ps.setString(6, "created");
            ps.setString(7, "");
            ps.setInt(8, 0);
            ps.setInt(9, command.maxRetry() == null ? 3 : command.maxRetry());
            ps.setString(10, "");
            ps.setString(11, payloadJson(command.payload()));
            ps.setString(12, safe(command.documentId(), ""));
            ps.setString(13, safe(command.projectId(), ""));
            ps.setString(14, safe(command.title(), ""));
            return ps;
        });
        return getByTaskId(taskId);
    }

    @Override
    public AsyncTaskVO enqueue(String taskId, String message) {
        return update(taskId, AsyncTaskStatus.QUEUED.name(), 0, safe(message, "queued"), null, null, null);
    }

    @Override
    public Optional<AsyncTaskVO> findByTaskId(String taskId) {
        List<AsyncTaskVO> tasks = jdbcTemplate.query("select * from async_task where task_id = ?", taskMapper, taskId);
        return tasks.stream().findFirst();
    }

    @Override
    public AsyncTaskVO getByTaskId(String taskId) {
        return findByTaskId(taskId).orElseThrow(() -> new BusinessException(404, "任务不存在"));
    }

    @Override
    public AsyncTaskVO start(String taskId) {
        return update(taskId, AsyncTaskStatus.PROCESSING.name(), 1, "running", null, null, null);
    }

    @Override
    public AsyncTaskVO updateProgress(String taskId, int progress, String message) {
        return update(taskId, null, progress, message, null, null, null);
    }

    @Override
    public AsyncTaskVO recordExternalRunId(String taskId, String externalRunId) {
        return update(taskId, null, null, null, externalRunId, null, null);
    }

    @Override
    public AsyncTaskVO increaseRetry(String taskId) {
        AsyncTaskVO current = getByTaskId(taskId);
        return update(taskId, null, null, null, null, current.retryCount() + 1, null);
    }

    @Override
    public AsyncTaskVO markRetryWaiting(String taskId, String message) {
        return update(taskId, AsyncTaskStatus.RETRY_WAITING.name(), null, safe(message, "retry waiting"), null, null, null);
    }

    @Override
    public AsyncTaskVO markRolledBack(String taskId, String message) {
        return update(taskId, AsyncTaskStatus.ROLLED_BACK.name(), null, safe(message, "rolled back"), null, null, null);
    }

    @Override
    public AsyncTaskVO markSuccess(String taskId, String message) {
        return update(taskId, AsyncTaskStatus.SUCCESS.name(), 100, safe(message, "success"), null, null, "");
    }

    @Override
    public AsyncTaskVO markFailed(String taskId, String failureReason) {
        return update(taskId, AsyncTaskStatus.FAILED.name(), null, "failed", null, null, safe(failureReason, "failed"));
    }

    @Override
    public List<AsyncTaskVO> listByType(String taskType, int limit) {
        return jdbcTemplate.query("""
                select * from async_task
                where task_type = ?
                order by created_at desc
                limit ?
                """, taskMapper, taskType, Math.max(1, limit));
    }

    @Override
    public AsyncTaskEventVO recordEvent(RecordAsyncTaskEventCommand command) {
        String eventId = blank(command.eventId()) ? UUID.randomUUID().toString() : command.eventId();
        jdbcTemplate.update("""
                insert into async_task_event(event_id, task_id, document_id, stage, status, started_at, finished_at,
                                             duration_ms, input_summary, output_summary, error_code, error_message, retry_count)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                eventId,
                command.taskId(),
                safe(command.documentId(), ""),
                safe(command.stage(), "UNKNOWN"),
                safe(command.status(), "UNKNOWN"),
                command.startedAt(),
                command.finishedAt(),
                command.durationMs() == null ? 0L : command.durationMs(),
                safe(command.inputSummary(), ""),
                safe(command.outputSummary(), ""),
                safe(command.errorCode(), ""),
                safe(command.errorMessage(), ""),
                command.retryCount() == null ? 0 : command.retryCount());
        return jdbcTemplate.queryForObject("select * from async_task_event where event_id = ?", eventMapper, eventId);
    }

    @Override
    public List<AsyncTaskEventVO> listRecentEvents(String taskId, int limit) {
        return jdbcTemplate.query("""
                select * from async_task_event
                where task_id = ?
                order by created_at desc
                limit ?
                """, eventMapper, taskId, Math.max(1, limit));
    }

    private AsyncTaskVO update(String taskId, String status, Integer progress, String message, String externalRunId, Integer retryCount, String failureReason) {
        AsyncTaskVO current = getByTaskId(taskId);
        jdbcTemplate.update("""
                update async_task
                set status = ?, progress = ?, message = ?, external_run_id = ?, retry_count = ?, failure_reason = ?
                where task_id = ?
                """,
                status == null ? current.status() : status,
                progress == null ? current.progress() : progress,
                message == null ? current.message() : message,
                externalRunId == null ? current.externalRunId() : externalRunId,
                retryCount == null ? current.retryCount() : retryCount,
                failureReason == null ? current.failureReason() : failureReason,
                taskId);
        return getByTaskId(taskId);
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists async_task(
                    task_id varchar(64) primary key,
                    task_type varchar(64) not null,
                    idempotent_key varchar(128) not null,
                    document_id varchar(64) not null default '',
                    project_id varchar(64) not null default '',
                    title varchar(255) not null default '',
                    status varchar(32) not null,
                    progress int not null default 0,
                    message varchar(255) not null default '',
                    external_run_id varchar(128) not null default '',
                    retry_count int not null default 0,
                    max_retry int not null default 3,
                    failure_reason text null,
                    payload_json longtext null,
                    created_at datetime not null default current_timestamp,
                    updated_at datetime not null default current_timestamp on update current_timestamp,
                    key idx_task_type_created(task_type, created_at),
                    key idx_task_status_created(status, created_at),
                    key idx_document_id_created(document_id, created_at),
                    unique key uk_idempotent_key(idempotent_key)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists async_task_event(
                    event_id varchar(64) primary key,
                    task_id varchar(64) not null,
                    document_id varchar(64) not null default '',
                    stage varchar(64) not null,
                    status varchar(32) not null,
                    started_at datetime null,
                    finished_at datetime null,
                    duration_ms bigint not null default 0,
                    input_summary text null,
                    output_summary text null,
                    error_code varchar(64) not null default '',
                    error_message text null,
                    retry_count int not null default 0,
                    created_at datetime not null default current_timestamp,
                    key idx_task_stage_created(task_id, stage, created_at),
                    key idx_stage_status_created(stage, status, created_at)
                )
                """);
        ensureColumn("async_task", "document_id", "alter table async_task add column document_id varchar(64) not null default '' after idempotent_key");
        ensureColumn("async_task", "project_id", "alter table async_task add column project_id varchar(64) not null default '' after document_id");
        ensureColumn("async_task", "title", "alter table async_task add column title varchar(255) not null default '' after project_id");
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from information_schema.columns
                where table_schema = database() and table_name = ? and column_name = ?
                """, Integer.class, tableName, columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }

    private String payloadJson(Object payload) {
        return payload == null ? "{}" : String.valueOf(payload);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value, String defaultValue) {
        return blank(value) ? defaultValue : value;
    }
}
