package com.hopeandsparks.agent.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AgentCheckpointRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AgentRunCheckpointRecord> mapper = (rs, rowNum) -> new AgentRunCheckpointRecord(
            rs.getString("checkpoint_id"),
            rs.getString("run_id"),
            rs.getString("node_name"),
            rs.getLong("state_version"),
            rs.getString("checkpoint_state_json"),
            rs.getString("payload_json"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public AgentCheckpointRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public void save(AgentRunCheckpointRecord record) {
        jdbcTemplate.update("""
                insert into agent_run_checkpoint(checkpoint_id, run_id, node_name, state_version, checkpoint_state_json, payload_json)
                values (?, ?, ?, ?, ?, ?)
                """,
                record.checkpointId(), record.runId(), record.nodeName(), record.stateVersion(), record.checkpointStateJson(), record.payloadJson());
    }

    public Optional<AgentRunCheckpointRecord> findLatest(String runId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select * from agent_run_checkpoint
                    where run_id = ?
                    order by created_at desc
                    limit 1
                    """, mapper, runId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<AgentRunCheckpointRecord> findByCheckpointId(String checkpointId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from agent_run_checkpoint where checkpoint_id = ?", mapper, checkpointId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<AgentRunCheckpointRecord> listByRunId(String runId) {
        return jdbcTemplate.query("""
                select * from agent_run_checkpoint
                where run_id = ?
                order by created_at desc
                """, mapper, runId);
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists agent_run_checkpoint(
                    checkpoint_id varchar(128) primary key,
                    run_id varchar(128) not null,
                    node_name varchar(64) not null,
                    state_version bigint not null,
                    checkpoint_state_json longtext not null,
                    payload_json longtext null,
                    created_at datetime not null default current_timestamp,
                    key idx_run_created(run_id, created_at),
                    key idx_run_node_created(run_id, node_name, created_at)
                )
                """);
    }
}
