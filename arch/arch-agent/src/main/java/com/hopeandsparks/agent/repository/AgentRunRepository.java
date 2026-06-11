package com.hopeandsparks.agent.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AgentRunRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AgentRunRecord> mapper = (rs, rowNum) -> new AgentRunRecord(
            rs.getString("run_id"),
            rs.getString("session_id"),
            rs.getString("user_id"),
            rs.getString("project_id"),
            rs.getString("request_json"),
            rs.getString("runtime"),
            rs.getString("status"),
            rs.getString("current_node"),
            rs.getInt("current_revision"),
            rs.getInt("max_revision"),
            rs.getString("error_code"),
            rs.getString("error_message"),
            rs.getTimestamp("started_at") == null ? null : rs.getTimestamp("started_at").toLocalDateTime(),
            rs.getTimestamp("finished_at") == null ? null : rs.getTimestamp("finished_at").toLocalDateTime()
    );

    public AgentRunRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public void save(AgentRunRecord record) {
        Integer count = jdbcTemplate.queryForObject("select count(1) from agent_run where run_id = ?", Integer.class, record.runId());
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    update agent_run
                    set session_id = ?, user_id = ?, project_id = ?, request_json = ?, runtime = ?, status = ?, current_node = ?,
                        current_revision = ?, max_revision = ?, error_code = ?, error_message = ?, started_at = ?, finished_at = ?
                    where run_id = ?
                    """,
                    record.sessionId(), record.userId(), record.projectId(), record.requestJson(), record.runtime(), record.status(), record.currentNode(),
                    record.currentRevision(), record.maxRevision(), record.errorCode(), record.errorMessage(), record.startedAt(), record.finishedAt(), record.runId());
            return;
        }
        jdbcTemplate.update("""
                insert into agent_run(run_id, session_id, user_id, project_id, request_json, runtime, status, current_node, current_revision, max_revision, error_code, error_message, started_at, finished_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                record.runId(), record.sessionId(), record.userId(), record.projectId(), record.requestJson(), record.runtime(), record.status(),
                record.currentNode(), record.currentRevision(), record.maxRevision(), record.errorCode(), record.errorMessage(), record.startedAt(), record.finishedAt());
    }

    public Optional<AgentRunRecord> findByRunId(String runId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from agent_run where run_id = ?", mapper, runId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<AgentRunRecord> listRecent(int limit) {
        return jdbcTemplate.query("select * from agent_run order by started_at desc limit ?", mapper, Math.max(1, limit));
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists agent_run(
                    run_id varchar(128) primary key,
                    session_id varchar(128) not null default '',
                    user_id varchar(64) not null default '',
                    project_id varchar(64) not null default '',
                    request_json longtext not null,
                    runtime varchar(32) not null,
                    status varchar(32) not null,
                    current_node varchar(64) not null default '',
                    current_revision int not null default 0,
                    max_revision int not null default 0,
                    error_code varchar(64) not null default '',
                    error_message text null,
                    started_at datetime null,
                    finished_at datetime null,
                    created_at datetime not null default current_timestamp,
                    updated_at datetime not null default current_timestamp on update current_timestamp,
                    key idx_status_started(status, started_at),
                    key idx_session_started(session_id, started_at)
                )
                """);
    }
}
