package com.hopeandsparks.resource.repository;

import com.hopeandsparks.resource.dto.ResourceQuery;
import com.hopeandsparks.resource.entity.LearningResource;
import com.hopeandsparks.resource.entity.LearningResourceVersion;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ResourceRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<LearningResource> resourceMapper = (rs, rowNum) -> new LearningResource(
            rs.getLong("id"),
            rs.getLong("node_id"),
            rs.getString("node_code"),
            rs.getString("node_name"),
            rs.getString("title"),
            rs.getString("resource_type"),
            rs.getString("resource_level"),
            rs.getString("summary"),
            rs.getString("content_source_type"),
            rs.getObject("current_file_id", Long.class),
            rs.getString("generated_by"),
            rs.getInt("generate_status"),
            rs.getInt("horizon_check_status"),
            rs.getBigDecimal("quality_score"),
            rs.getInt("current_version_no"),
            rs.getString("file_name"),
            rs.getString("file_type"),
            rs.getString("object_key"),
            rs.getObject("file_size", Long.class),
            rs.getObject("duration_seconds", Integer.class),
            rs.getInt("user_progress"),
            rs.getInt("collected") == 1,
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<LearningResourceVersion> versionMapper = (rs, rowNum) -> new LearningResourceVersion(
            rs.getLong("id"),
            rs.getLong("resource_id"),
            rs.getInt("version_no"),
            rs.getObject("content_file_id", Long.class),
            rs.getString("change_summary"),
            rs.getInt("horizon_check_status"),
            toLocalDateTime(rs.getTimestamp("created_at"))
    );

    public ResourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countResources(ResourceQuery query) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select count(1)
                from learning_resource r
                join knowledge_node n on n.id = r.node_id and n.is_deleted = 0
                where r.is_deleted = 0 and r.generate_status = 1
                """ + filters(query, args);
        Long total = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<LearningResource> listResources(ResourceQuery query, long offset, long pageSize, Long userId) {
        List<Object> args = new ArrayList<>();
        Long safeUserId = userId == null ? -1L : userId;
        args.add(safeUserId);
        args.add(safeUserId);
        args.add(safeUserId);
        String sql = baseSelect() + filters(query, args) + """
                order by r.updated_at desc, r.id desc
                limit ? offset ?
                """;
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, resourceMapper, args.toArray());
    }

    public Optional<LearningResource> findById(Long resourceId, Long userId) {
        try {
            List<Object> args = new ArrayList<>();
            Long safeUserId = userId == null ? -1L : userId;
            args.add(safeUserId);
            args.add(safeUserId);
            args.add(safeUserId);
            args.add(resourceId);
            return Optional.ofNullable(jdbcTemplate.queryForObject(baseSelect() + """
                    and r.id = ?
                    """, resourceMapper, args.toArray()));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<LearningResourceVersion> listVersions(Long resourceId) {
        return jdbcTemplate.query("""
                select id, resource_id, version_no, content_file_id, change_summary,
                       horizon_check_status, created_at
                from learning_resource_version
                where resource_id = ?
                order by version_no desc
                """, versionMapper, resourceId);
    }

    public Optional<Long> firstResourceIdByNode(Long nodeId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id
                    from learning_resource
                    where node_id = ? and generate_status = 1 and is_deleted = 0
                    order by horizon_check_status desc, quality_score desc, updated_at desc, id desc
                    limit 1
                    """, Long.class, nodeId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public long countResourcesByNode(Long nodeId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from learning_resource
                where node_id = ? and generate_status = 1 and is_deleted = 0
                """, Long.class, nodeId);
        return total == null ? 0 : total;
    }

    public long countLearnedResourcesByNode(Long userId, Long nodeId) {
        if (userId == null) {
            return 0;
        }
        Long total = jdbcTemplate.queryForObject("""
                select count(distinct r.id)
                from learning_resource r
                where r.node_id = ? and r.generate_status = 1 and r.is_deleted = 0
                  and (
                    exists (
                        select 1 from user_learning_record lr
                        where lr.resource_id = r.id and lr.user_id = ?
                    )
                    or exists (
                        select 1
                        from study_task t
                        join study_plan p on p.id = t.plan_id and p.is_deleted = 0
                        where t.resource_id = r.id and p.user_id = ?
                          and t.is_deleted = 0 and t.progress_percent > 0
                    )
                  )
                """, Long.class, nodeId, userId, userId);
        return total == null ? 0 : total;
    }

    public long sumStudyDurationByNode(Long userId, Long nodeId) {
        if (userId == null) {
            return 0;
        }
        Long total = jdbcTemplate.queryForObject("""
                select coalesce(sum(lr.duration_seconds), 0)
                from user_learning_record lr
                join learning_resource r on r.id = lr.resource_id
                where lr.user_id = ? and r.node_id = ?
                """, Long.class, userId, nodeId);
        return total == null ? 0 : total;
    }

    public Optional<Long> findTaskIdForUserResource(Long userId, Long resourceId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select t.id
                    from study_task t
                    join study_plan p on p.id = t.plan_id and p.is_deleted = 0
                    where p.user_id = ? and t.resource_id = ? and t.is_deleted = 0
                    order by p.plan_status = 1 desc, p.updated_at desc, t.sort_order asc, t.id asc
                    limit 1
                    """, Long.class, userId, resourceId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void insertLearningRecord(Long userId, Long taskId, Long resourceId, String recordType, int durationSeconds) {
        jdbcTemplate.update("""
                insert into user_learning_record(user_id, task_id, resource_id, record_type, duration_seconds, record_date)
                values (?, ?, ?, ?, ?, current_date)
                """, userId, taskId, resourceId, recordType, durationSeconds);
    }

    public void updateTaskProgress(Long userId, Long resourceId, int progress) {
        jdbcTemplate.update("""
                update study_task t
                join study_plan p on p.id = t.plan_id and p.is_deleted = 0
                set t.progress_percent = greatest(t.progress_percent, ?),
                    t.task_status = case
                        when ? >= 100 then 2
                        when ? > 0 and t.task_status = 0 then 1
                        else t.task_status
                    end
                where p.user_id = ? and t.resource_id = ? and t.is_deleted = 0
                """, progress, progress, progress, userId, resourceId);
    }

    public Long insertFeedbackTicket(
            Long userId,
            Long resourceId,
            String issueType,
            String description,
            String snapshotContent
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into feedback_ticket(user_id, target_type, target_id, issue_type,
                                                description, snapshot_content, status)
                    values (?, 'resource', ?, ?, ?, ?, 'pending')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, resourceId);
            ps.setString(3, issueType);
            ps.setString(4, description);
            ps.setString(5, snapshotContent);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private String baseSelect() {
        return """
                select r.id, r.node_id, n.node_code, n.node_name, r.title,
                       r.resource_type, r.resource_level, r.summary, r.content_source_type,
                       r.current_file_id, r.generated_by, r.generate_status,
                       r.horizon_check_status, r.quality_score, r.current_version_no,
                       f.file_name, f.file_type, f.object_key, f.file_size, f.duration_seconds,
                       greatest(coalesce(tp.progress, 0), case when lr.resource_id is null then 0 else 20 end) as user_progress,
                       case when fav.resource_id is null then 0 else 1 end as collected,
                       r.created_at, r.updated_at
                from learning_resource r
                join knowledge_node n on n.id = r.node_id and n.is_deleted = 0
                left join sys_oss_file f on f.id = r.current_file_id and f.is_deleted = 0
                left join (
                    select t.resource_id, max(t.progress_percent) as progress
                    from study_task t
                    join study_plan p on p.id = t.plan_id and p.is_deleted = 0
                    where p.user_id = ? and t.resource_id is not null and t.is_deleted = 0
                    group by t.resource_id
                ) tp on tp.resource_id = r.id
                left join (
                    select distinct resource_id
                    from user_learning_record
                    where user_id = ? and resource_id is not null
                ) lr on lr.resource_id = r.id
                left join user_resource_favorite fav on fav.resource_id = r.id and fav.user_id = ?
                where r.is_deleted = 0 and r.generate_status = 1
                """;
    }

    private String filters(ResourceQuery query, List<Object> args) {
        if (query == null) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        appendTypeFilter(sql, args, query.type());
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String like = "%" + query.keyword().trim() + "%";
            sql.append(" and (r.title like ? or r.summary like ? or n.node_name like ? or n.node_code like ?)");
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (query.verified() != null) {
            sql.append(query.verified() ? " and r.horizon_check_status = 1" : " and r.horizon_check_status <> 1");
        }
        if (query.planId() != null) {
            sql.append("""
                     and exists (
                        select 1 from study_task st
                        where st.resource_id = r.id and st.plan_id = ? and st.is_deleted = 0
                     )
                    """);
            args.add(query.planId());
        }
        if (query.nodeId() != null) {
            sql.append(" and r.node_id = ?");
            args.add(query.nodeId());
        }
        return sql.toString();
    }

    private void appendTypeFilter(StringBuilder sql, List<Object> args, String type) {
        String safeType = type == null || type.isBlank() ? "all" : type.trim().toLowerCase();
        switch (safeType) {
            case "all" -> {
            }
            case "video" -> {
                sql.append(" and r.resource_type = ?");
                args.add("video");
            }
            case "document" -> sql.append(" and r.resource_type in ('doc', 'ppt', 'mindmap')");
            case "reading" -> sql.append(" and r.resource_type = 'doc' and r.content_source_type = 'text'");
            case "exercise_set", "exercise", "quiz" -> {
                sql.append(" and r.resource_type = ?");
                args.add("quiz");
            }
            case "code_case", "code", "case" -> {
                sql.append(" and r.resource_type = ?");
                args.add("code");
            }
            case "mindmap" -> {
                sql.append(" and r.resource_type = ?");
                args.add("mindmap");
            }
            case "ppt" -> {
                sql.append(" and r.resource_type = ?");
                args.add("ppt");
            }
            default -> {
                sql.append(" and r.resource_type = ?");
                args.add(safeType);
            }
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
