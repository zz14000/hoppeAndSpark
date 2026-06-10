package com.hopeandsparks.agent.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AgentMemorySnapshotRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgentMemorySnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public void save(AgentMemorySnapshotRecord record) {
        jdbcTemplate.update("""
                insert into agent_memory_snapshot(run_id, session_id, user_id, project_id, course_id, knowledge_point, memory_level, payload_json, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                record.runId(), record.sessionId(), record.userId(), record.projectId(), record.courseId(),
                record.knowledgePoint(), record.memoryLevel(), record.payloadJson());
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists agent_memory_snapshot(
                    id bigint primary key auto_increment,
                    run_id varchar(128) not null,
                    session_id varchar(128) not null default '',
                    user_id varchar(64) not null default '',
                    project_id varchar(64) not null default '',
                    course_id varchar(64) not null default '',
                    knowledge_point varchar(255) not null default '',
                    memory_level varchar(32) not null,
                    payload_json longtext not null,
                    created_at datetime not null default current_timestamp,
                    key idx_run_id(run_id),
                    key idx_session_id(session_id)
                )
                """);
    }
}
