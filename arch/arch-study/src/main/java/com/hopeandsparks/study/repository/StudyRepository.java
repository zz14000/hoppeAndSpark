package com.hopeandsparks.study.repository;

import com.hopeandsparks.study.entity.StudyPlan;
import com.hopeandsparks.study.entity.StudyTask;
import com.hopeandsparks.study.entity.UserKnowledgeProgress;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class StudyRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<StudyPlan> planMapper = (rs, rowNum) -> new StudyPlan(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("course_id"),
            rs.getString("course_name"),
            rs.getString("major_domain"),
            rs.getString("plan_title"),
            rs.getInt("plan_status"),
            rs.getObject("start_date", LocalDate.class),
            rs.getObject("end_date", LocalDate.class),
            rs.getString("generated_by"),
            rs.getLong("finished_count"),
            rs.getLong("total_count"),
            rs.getInt("progress"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<StudyTask> taskMapper = (rs, rowNum) -> new StudyTask(
            rs.getLong("id"),
            rs.getLong("plan_id"),
            rs.getObject("node_id", Long.class),
            rs.getObject("resource_id", Long.class),
            rs.getString("task_title"),
            rs.getString("task_type"),
            rs.getInt("sort_order"),
            toLocalDateTime(rs.getTimestamp("plan_start_time")),
            toLocalDateTime(rs.getTimestamp("plan_end_time")),
            rs.getInt("task_status"),
            rs.getInt("progress_percent")
    );

    private final RowMapper<UserKnowledgeProgress> progressMapper = (rs, rowNum) -> new UserKnowledgeProgress(
            rs.getLong("node_id"),
            rs.getString("progress_status"),
            rs.getInt("progress_percent"),
            rs.getBigDecimal("mastery_score")
    );

    public StudyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<StudyPlan> findCurrentPlan(Long userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(planSelect() + """
                    where p.user_id = ? and p.is_deleted = 0
                    group by p.id, p.user_id, p.course_id, c.course_name, c.major_domain,
                             p.plan_title, p.plan_status, p.start_date, p.end_date,
                             p.generated_by, p.created_at, p.updated_at
                    order by case p.plan_status when 1 then 0 when 0 then 1 when 3 then 2 else 3 end,
                             p.updated_at desc, p.id desc
                    limit 1
                    """, planMapper, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<StudyPlan> findPlanForUser(Long userId, Long planId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(planSelect() + """
                    where p.user_id = ? and p.id = ? and p.is_deleted = 0
                    group by p.id, p.user_id, p.course_id, c.course_name, c.major_domain,
                             p.plan_title, p.plan_status, p.start_date, p.end_date,
                             p.generated_by, p.created_at, p.updated_at
                    """, planMapper, userId, planId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Long insertPlan(Long userId, Long courseId, String title, LocalDate startDate, LocalDate endDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into study_plan(user_id, course_id, plan_title, plan_status,
                                           start_date, end_date, generated_by)
                    values (?, ?, ?, 1, ?, ?, 'Strict')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, courseId);
            ps.setString(3, title);
            if (startDate == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(startDate));
            }
            if (endDate == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(endDate));
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("study plan insert failed");
        }
        return key.longValue();
    }

    public void insertTask(
            Long planId,
            Long nodeId,
            Long resourceId,
            String title,
            String taskType,
            int sortOrder,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into study_task(plan_id, node_id, resource_id, task_title, task_type,
                                           sort_order, plan_start_time, plan_end_time,
                                           task_status, progress_percent)
                    values (?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                    """);
            ps.setLong(1, planId);
            if (nodeId == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, nodeId);
            }
            if (resourceId == null) {
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(3, resourceId);
            }
            ps.setString(4, title);
            ps.setString(5, taskType);
            ps.setInt(6, sortOrder);
            ps.setTimestamp(7, startTime == null ? null : Timestamp.valueOf(startTime));
            ps.setTimestamp(8, endTime == null ? null : Timestamp.valueOf(endTime));
            return ps;
        });
    }

    public List<StudyTask> listTasks(Long planId) {
        return jdbcTemplate.query("""
                select id, plan_id, node_id, resource_id, task_title, task_type,
                       sort_order, plan_start_time, plan_end_time, task_status, progress_percent
                from study_task
                where plan_id = ? and is_deleted = 0
                order by sort_order asc, id asc
                """, taskMapper, planId);
    }

    public List<Long> listPlanNodeIds(Long planId) {
        return jdbcTemplate.queryForList("""
                select distinct node_id
                from study_task
                where plan_id = ? and node_id is not null and is_deleted = 0
                order by node_id asc
                """, Long.class, planId);
    }

    public List<UserKnowledgeProgress> listProgressByUserAndNodes(Long userId, Collection<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }
        String placeholders = placeholders(nodeIds.size());
        List<Object> args = new ArrayList<>();
        args.add(userId);
        args.addAll(nodeIds);
        return jdbcTemplate.query("""
                select node_id, progress_status, progress_percent, mastery_score
                from user_knowledge_progress
                where user_id = ? and node_id in (%s) and is_deleted = 0
                """.formatted(placeholders), progressMapper, args.toArray());
    }

    public void upsertKnowledgeProgress(Long userId, Long nodeId, String status, int progress) {
        jdbcTemplate.update("""
                insert into user_knowledge_progress(user_id, node_id, progress_status, progress_percent, mastery_score)
                values (?, ?, ?, ?, 0)
                on duplicate key update
                    progress_status = values(progress_status),
                    progress_percent = greatest(progress_percent, values(progress_percent)),
                    updated_at = current_timestamp,
                    is_deleted = 0
                """, userId, nodeId, status, progress);
    }

    public void applyPracticeFeedback(
            Long userId,
            Long nodeId,
            String status,
            int progress,
            BigDecimal masteryScore,
            int nextReviewDays
    ) {
        jdbcTemplate.update("""
                insert into user_knowledge_progress(user_id, node_id, progress_status, progress_percent,
                                                    mastery_score, last_review_time, review_due_time)
                values (?, ?, ?, ?, ?, current_timestamp, date_add(current_timestamp, interval ? day))
                on duplicate key update
                    progress_status = values(progress_status),
                    progress_percent = case
                        when values(mastery_score) < 60 and progress_percent = 0 then values(progress_percent)
                        when values(mastery_score) < 60 then least(progress_percent, values(progress_percent))
                        else greatest(progress_percent, values(progress_percent))
                    end,
                    mastery_score = values(mastery_score),
                    last_review_time = current_timestamp,
                    review_due_time = values(review_due_time),
                    updated_at = current_timestamp,
                    is_deleted = 0
                """, userId, nodeId, status, progress, masteryScore, nextReviewDays);
    }

    public void adjustPlan(Long userId, Long planId, String strategy) {
        int status = switch (strategy == null ? "" : strategy.toLowerCase()) {
            case "pause", "paused" -> 3;
            case "resume", "continue" -> 1;
            default -> -1;
        };
        if (status >= 0) {
            jdbcTemplate.update("""
                    update study_plan
                    set plan_status = ?
                    where id = ? and user_id = ? and is_deleted = 0
                    """, status, planId, userId);
            return;
        }
        jdbcTemplate.update("""
                update study_plan
                set updated_at = current_timestamp
                where id = ? and user_id = ? and is_deleted = 0
                """, planId, userId);
    }

    public void adjustTasks(Long planId, List<Long> taskIds, String strategy) {
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }
        String safeStrategy = strategy == null ? "" : strategy.toLowerCase();
        int status = safeStrategy.contains("remove")
                || safeStrategy.contains("skip")
                || safeStrategy.contains("complete") ? 2 : 0;
        int progress = status == 2 ? 100 : 0;
        String placeholders = placeholders(taskIds.size());
        List<Object> args = new ArrayList<>();
        args.add(status);
        args.add(progress);
        args.add(planId);
        args.addAll(taskIds);
        jdbcTemplate.update("""
                update study_task
                set task_status = ?, progress_percent = ?
                where plan_id = ? and id in (%s) and is_deleted = 0
                """.formatted(placeholders), args.toArray());
    }

    public boolean planContainsNode(Long planId, Long nodeId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from study_task
                where plan_id = ? and node_id = ? and is_deleted = 0
                """, Long.class, planId, nodeId);
        return total != null && total > 0;
    }

    private String planSelect() {
        return """
                select p.id, p.user_id, p.course_id, c.course_name, c.major_domain,
                       p.plan_title, p.plan_status, p.start_date, p.end_date,
                       p.generated_by, p.created_at, p.updated_at,
                       coalesce(sum(case when t.task_status = 2 then 1 else 0 end), 0) as finished_count,
                       count(t.id) as total_count,
                       case when count(t.id) = 0 then 0
                            else round(sum(case when t.task_status = 2 then 1 else 0 end) * 100 / count(t.id))
                       end as progress
                from study_plan p
                join course c on c.id = p.course_id and c.is_deleted = 0
                left join study_task t on t.plan_id = p.id and t.is_deleted = 0
                """;
    }

    private String placeholders(int count) {
        return "?,".repeat(count - 1) + "?";
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
