package com.hopeandsparks.manage.repository;

import com.hopeandsparks.manage.entity.FeedbackTicket;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for feedback_ticket management.
 */
@Repository
public class DisputeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<FeedbackTicket> mapper = (rs, rowNum) -> new FeedbackTicket(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("nickname"),
            rs.getString("target_type"),
            rs.getLong("target_id"),
            rs.getString("issue_type"),
            rs.getString("description"),
            rs.getString("snapshot_content"),
            rs.getString("status"),
            rs.getObject("admin_id", Long.class),
            rs.getString("admin_username"),
            rs.getString("process_remark"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    public DisputeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long count(Map<String, String> query) {
        List<Object> args = new ArrayList<>();
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from feedback_ticket t
                join sys_user u on u.id = t.user_id and u.is_deleted = 0
                left join sys_admin a on a.id = t.admin_id and a.is_deleted = 0
                where t.is_deleted = 0
                """ + filters(query, args), Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<FeedbackTicket> list(Map<String, String> query, long offset, long pageSize) {
        List<Object> args = new ArrayList<>();
        String sql = baseSelect() + filters(query, args) + """
                order by t.updated_at desc, t.id desc
                limit ? offset ?
                """;
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, mapper, args.toArray());
    }

    public Optional<FeedbackTicket> findById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(baseSelect() + """
                    and t.id = ?
                    """, mapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void updateTicket(Long id, Long adminId, String status, String remark) {
        jdbcTemplate.update("""
                update feedback_ticket
                set status = ?,
                    admin_id = ?,
                    process_remark = ?
                where id = ? and is_deleted = 0
                """, status, adminId, remark, id);
    }

    private String baseSelect() {
        return """
                select t.id, t.user_id, u.username, u.nickname,
                       t.target_type, t.target_id, t.issue_type,
                       t.description, t.snapshot_content, t.status,
                       t.admin_id, a.username as admin_username,
                       t.process_remark, t.created_at, t.updated_at
                from feedback_ticket t
                join sys_user u on u.id = t.user_id and u.is_deleted = 0
                left join sys_admin a on a.id = t.admin_id and a.is_deleted = 0
                where t.is_deleted = 0
                """;
    }

    private String filters(Map<String, String> query, List<Object> args) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        addTextFilter(sql, args, "t.status", query.get("status"));
        addTextFilter(sql, args, "t.target_type", query.get("targetType"));
        addTextFilter(sql, args, "t.issue_type", query.get("issueType"));
        Long userId = parseOptionalId(query.get("userId"));
        if (userId != null) {
            sql.append(" and t.user_id = ?");
            args.add(userId);
        }
        if (!isBlank(query.get("keyword"))) {
            String like = "%" + query.get("keyword").trim() + "%";
            sql.append(" and (t.description like ? or t.snapshot_content like ? or u.username like ? or u.nickname like ?)");
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        return sql.toString();
    }

    private void addTextFilter(StringBuilder sql, List<Object> args, String column, String value) {
        if (!isBlank(value)) {
            sql.append(" and ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private Long parseOptionalId(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
