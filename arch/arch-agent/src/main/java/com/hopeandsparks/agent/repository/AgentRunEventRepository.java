package com.hopeandsparks.agent.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AgentRunEventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AgentRunEventRecord> mapper = (rs, rowNum) -> new AgentRunEventRecord(
            rs.getString("event_id"),
            rs.getString("run_id"),
            rs.getString("node_name"),
            rs.getString("stage"),
            rs.getString("status"),
            rs.getString("summary"),
            rs.getString("payload_json"),
            rs.getLong("duration_ms"),
            rs.getInt("retry_count"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public AgentRunEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public void save(AgentRunEventRecord record) {
        jdbcTemplate.update("""
                insert into agent_run_event(event_id, run_id, node_name, stage, status, summary, payload_json, duration_ms, retry_count)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                record.eventId() == null || record.eventId().isBlank() ? UUID.randomUUID().toString() : record.eventId(),
                record.runId(), record.nodeName(), record.stage(), record.status(), record.summary(),
                record.payloadJson(), record.durationMs(), record.retryCount());
    }

    public List<AgentRunEventRecord> listByRunId(String runId) {
        return jdbcTemplate.query("""
                select * from agent_run_event
                where run_id = ?
                order by created_at asc
                """, mapper, runId);
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists agent_run_event(
                    event_id varchar(128) primary key,
                    run_id varchar(128) not null,
                    node_name varchar(64) not null default '',
                    stage varchar(64) not null,
                    status varchar(32) not null,
                    summary varchar(255) not null default '',
                    payload_json longtext null,
                    duration_ms bigint not null default 0,
                    retry_count int not null default 0,
                    created_at datetime not null default current_timestamp,
                    key idx_run_created(run_id, created_at),
                    key idx_node_status_created(node_name, status, created_at)
                )
                """);
    }
}
