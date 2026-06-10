package com.hopeandsparks.manage.repository;

import com.hopeandsparks.manage.dto.OperationLogCommand;
import com.hopeandsparks.manage.entity.OperationLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository for sys_operation_log.
 */
@Repository
public class OperationLogRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<OperationLog> mapper = (rs, rowNum) -> new OperationLog(
            rs.getLong("id"),
            rs.getLong("admin_id"),
            rs.getString("admin_username"),
            rs.getString("module_name"),
            rs.getString("action_type"),
            rs.getString("target_type"),
            rs.getObject("target_id", Long.class),
            rs.getString("detail"),
            rs.getString("ip_address"),
            toLocalDateTime(rs.getTimestamp("created_at"))
    );

    public OperationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(OperationLogCommand command) {
        jdbcTemplate.update("""
                insert into sys_operation_log(admin_id, module_name, action_type, target_type,
                                              target_id, detail, ip_address)
                values (?, ?, ?, ?, ?, ?, ?)
                """, command.adminId(), command.moduleName(), command.actionType(), command.targetType(),
                command.targetId(), command.detail(), command.ipAddress());
    }

    public long count(Map<String, String> query) {
        List<Object> args = new ArrayList<>();
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from sys_operation_log l
                left join sys_admin a on a.id = l.admin_id and a.is_deleted = 0
                where 1 = 1
                """ + filters(query, args), Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<OperationLog> list(Map<String, String> query, long offset, long pageSize) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select l.id, l.admin_id, a.username as admin_username,
                       l.module_name, l.action_type, l.target_type, l.target_id,
                       l.detail, l.ip_address, l.created_at
                from sys_operation_log l
                left join sys_admin a on a.id = l.admin_id and a.is_deleted = 0
                where 1 = 1
                """ + filters(query, args) + """
                order by l.created_at desc, l.id desc
                limit ? offset ?
                """;
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, mapper, args.toArray());
    }

    private String filters(Map<String, String> query, List<Object> args) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        addTextFilter(sql, args, "l.module_name", query.get("moduleName"));
        addTextFilter(sql, args, "l.action_type", query.get("actionType"));
        addTextFilter(sql, args, "l.target_type", query.get("targetType"));
        Long adminId = parseOptionalId(query.get("adminId"));
        if (adminId != null) {
            sql.append(" and l.admin_id = ?");
            args.add(adminId);
        }
        if (!isBlank(query.get("keyword"))) {
            String like = "%" + query.get("keyword").trim() + "%";
            sql.append(" and (l.detail like ? or a.username like ?)");
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
