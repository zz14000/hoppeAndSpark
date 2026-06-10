package com.hopeandsparks.agent.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class AgentSessionMemoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgentSessionMemoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public Optional<AgentSessionMemoryRecord> findBySessionId(String sessionId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select session_id, user_id, project_id, recent_summary, last_plan_json, unfinished_task_json, updated_at
                    from agent_session_memory
                    where session_id = ?
                    """, (rs, rowNum) -> new AgentSessionMemoryRecord(
                    rs.getString("session_id"),
                    rs.getString("user_id"),
                    rs.getString("project_id"),
                    rs.getString("recent_summary"),
                    rs.getString("last_plan_json"),
                    rs.getString("unfinished_task_json"),
                    rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime()
            ), sessionId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void save(AgentSessionMemoryRecord record) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from agent_session_memory where session_id = ?",
                Integer.class,
                record.sessionId()
        );
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    update agent_session_memory
                    set user_id = ?, project_id = ?, recent_summary = ?, last_plan_json = ?, unfinished_task_json = ?, updated_at = current_timestamp
                    where session_id = ?
                    """,
                    record.userId(), record.projectId(), record.recentSummary(), record.lastPlanJson(), record.unfinishedTaskJson(), record.sessionId());
            return;
        }
        jdbcTemplate.update("""
                insert into agent_session_memory(session_id, user_id, project_id, recent_summary, last_plan_json, unfinished_task_json, updated_at)
                values (?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                record.sessionId(), record.userId(), record.projectId(), record.recentSummary(), record.lastPlanJson(), record.unfinishedTaskJson());
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists agent_session_memory(
                    session_id varchar(128) primary key,
                    user_id varchar(64) not null default '',
                    project_id varchar(64) not null default '',
                    recent_summary text null,
                    last_plan_json longtext null,
                    unfinished_task_json longtext null,
                    updated_at datetime not null default current_timestamp on update current_timestamp
                )
                """);
    }
}
