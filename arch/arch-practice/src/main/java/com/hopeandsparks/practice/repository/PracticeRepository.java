package com.hopeandsparks.practice.repository;

import com.hopeandsparks.practice.dto.ExerciseSetFilter;
import com.hopeandsparks.practice.entity.EvaluationReport;
import com.hopeandsparks.practice.entity.PracticeQuestion;
import com.hopeandsparks.practice.entity.PracticeSet;
import com.hopeandsparks.practice.entity.UserQuestionRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 练习模块的数据访问。
 */
@Repository
public class PracticeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PracticeSet> setMapper = (rs, rowNum) -> new PracticeSet(
            rs.getLong("id"),
            rs.getLong("course_id"),
            rs.getString("course_name"),
            rs.getString("set_name"),
            rs.getString("set_type"),
            rs.getString("difficulty_level"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<PracticeQuestion> questionMapper = (rs, rowNum) -> new PracticeQuestion(
            rs.getLong("id"),
            rs.getLong("node_id"),
            rs.getString("node_code"),
            rs.getString("node_name"),
            rs.getString("question_type"),
            rs.getString("difficulty_level"),
            rs.getString("content_text"),
            rs.getString("options_json"),
            rs.getString("standard_answer"),
            rs.getString("analysis_text"),
            rs.getInt("sort_order")
    );

    private final RowMapper<UserQuestionRecord> recordMapper = (rs, rowNum) -> new UserQuestionRecord(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("question_id"),
            rs.getObject("practice_set_id", Long.class),
            rs.getString("user_answer"),
            toBoolean(rs.getObject("is_correct", Integer.class)),
            rs.getBigDecimal("score"),
            rs.getString("judge_mode"),
            rs.getString("feedback_text"),
            toBoolean(rs.getObject("is_flagged", Integer.class)),
            toLocalDateTime(rs.getTimestamp("created_at"))
    );

    private final RowMapper<EvaluationReport> reportMapper = (rs, rowNum) -> new EvaluationReport(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getObject("course_id", Long.class),
            rs.getObject("practice_set_id", Long.class),
            rs.getBigDecimal("overall_score"),
            rs.getString("knowledge_score_json"),
            rs.getString("ability_summary"),
            rs.getString("improvement_suggestion"),
            rs.getString("generated_by"),
            toLocalDateTime(rs.getTimestamp("created_at"))
    );

    public PracticeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countSets(Long userId, ExerciseSetFilter filter) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select count(distinct p.id)
                from practice_set p
                join course c on c.id = p.course_id and c.is_deleted = 0
                where p.is_deleted = 0
                """ + filterSql(userId, filter, args);
        Long total = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<PracticeSet> listSets(Long userId, ExerciseSetFilter filter, long offset, long pageSize) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select distinct p.id, p.course_id, c.course_name, p.set_name, p.set_type,
                       p.difficulty_level, p.created_at, p.updated_at
                from practice_set p
                join course c on c.id = p.course_id and c.is_deleted = 0
                where p.is_deleted = 0
                """ + filterSql(userId, filter, args) + """
                order by p.updated_at desc, p.id desc
                limit ? offset ?
                """;
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, setMapper, args.toArray());
    }

    public Optional<PracticeSet> findSet(Long setId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select p.id, p.course_id, c.course_name, p.set_name, p.set_type,
                           p.difficulty_level, p.created_at, p.updated_at
                    from practice_set p
                    join course c on c.id = p.course_id and c.is_deleted = 0
                    where p.id = ? and p.is_deleted = 0
                    """, setMapper, setId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<PracticeQuestion> listQuestions(Long setId) {
        return jdbcTemplate.query("""
                select q.id, q.node_id, n.node_code, n.node_name, q.question_type,
                       q.difficulty_level, q.content_text, q.options_json, q.standard_answer,
                       q.analysis_text, psq.sort_order
                from practice_set_question psq
                join question_bank q on q.id = psq.question_id and q.is_deleted = 0
                join knowledge_node n on n.id = q.node_id and n.is_deleted = 0
                where psq.practice_set_id = ?
                order by psq.sort_order asc, q.id asc
                """, questionMapper, setId);
    }

    public Optional<PracticeQuestion> findQuestionInSet(Long setId, Long questionId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select q.id, q.node_id, n.node_code, n.node_name, q.question_type,
                           q.difficulty_level, q.content_text, q.options_json, q.standard_answer,
                           q.analysis_text, psq.sort_order
                    from practice_set_question psq
                    join question_bank q on q.id = psq.question_id and q.is_deleted = 0
                    join knowledge_node n on n.id = q.node_id and n.is_deleted = 0
                    where psq.practice_set_id = ? and q.id = ?
                    """, questionMapper, setId, questionId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Map<Long, UserQuestionRecord> latestRecordMap(Long userId, Long setId) {
        return listLatestRecords(userId, setId).stream()
                .collect(Collectors.toMap(UserQuestionRecord::questionId, Function.identity()));
    }

    public List<UserQuestionRecord> listLatestRecords(Long userId, Long setId) {
        return jdbcTemplate.query("""
                select r.id, r.user_id, r.question_id, r.practice_set_id, r.user_answer,
                       r.is_correct, r.score, r.judge_mode, r.feedback_text,
                       r.is_flagged, r.created_at
                from user_question_record r
                join (
                    select question_id, max(id) as id
                    from user_question_record
                    where user_id = ? and practice_set_id = ?
                    group by question_id
                ) latest on latest.id = r.id
                order by r.id asc
                """, recordMapper, userId, setId);
    }

    public Optional<UserQuestionRecord> findLatestRecord(Long userId, Long setId, Long questionId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, user_id, question_id, practice_set_id, user_answer,
                           is_correct, score, judge_mode, feedback_text, is_flagged, created_at
                    from user_question_record
                    where user_id = ? and practice_set_id = ? and question_id = ?
                    order by id desc
                    limit 1
                    """, recordMapper, userId, setId, questionId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<UserQuestionRecord> findRecordById(Long userId, Long recordId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, user_id, question_id, practice_set_id, user_answer,
                           is_correct, score, judge_mode, feedback_text, is_flagged, created_at
                    from user_question_record
                    where user_id = ? and id = ?
                    """, recordMapper, userId, recordId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public UserQuestionRecord saveRecord(
            Long userId,
            Long setId,
            Long questionId,
            String userAnswer,
            Boolean correct,
            BigDecimal score,
            String judgeMode,
            String feedbackText,
            boolean flagged
    ) {
        Optional<UserQuestionRecord> oldRecord = findLatestRecord(userId, setId, questionId);
        if (oldRecord.isPresent()) {
            Long recordId = oldRecord.get().id();
            jdbcTemplate.update("""
                    update user_question_record
                    set user_answer = ?, is_correct = ?, score = ?, judge_mode = ?,
                        feedback_text = ?, is_flagged = ?, created_at = current_timestamp
                    where id = ?
                    """, userAnswer, toTinyInt(correct), score, judgeMode, feedbackText, flagged ? 1 : 0, recordId);
            return findRecordById(userId, recordId).orElseThrow();
        }
        Long recordId = insertRecord(userId, setId, questionId, userAnswer, correct, score, judgeMode, feedbackText, flagged);
        return findRecordById(userId, recordId).orElseThrow();
    }

    public UserQuestionRecord saveFlag(Long userId, Long setId, Long questionId, boolean flagged) {
        Optional<UserQuestionRecord> oldRecord = findLatestRecord(userId, setId, questionId);
        if (oldRecord.isPresent()) {
            Long recordId = oldRecord.get().id();
            jdbcTemplate.update("""
                    update user_question_record
                    set is_flagged = ?, created_at = current_timestamp
                    where id = ?
                    """, flagged ? 1 : 0, recordId);
            return findRecordById(userId, recordId).orElseThrow();
        }
        Long recordId = insertRecord(userId, setId, questionId, null, null, BigDecimal.ZERO, "auto", null, flagged);
        return findRecordById(userId, recordId).orElseThrow();
    }

    public int countAnswered(Long userId, Long setId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(distinct question_id)
                from user_question_record
                where user_id = ? and practice_set_id = ?
                  and user_answer is not null and trim(user_answer) <> ''
                """, Long.class, userId, setId);
        return total == null ? 0 : total.intValue();
    }

    public int countFlagged(Long userId, Long setId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(distinct question_id)
                from user_question_record
                where user_id = ? and practice_set_id = ? and is_flagged = 1
                """, Long.class, userId, setId);
        return total == null ? 0 : total.intValue();
    }

    public Optional<EvaluationReport> findReport(Long userId, Long reportId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, user_id, course_id, practice_set_id, overall_score,
                           knowledge_score_json, ability_summary, improvement_suggestion,
                           generated_by, created_at
                    from evaluation_report
                    where user_id = ? and id = ? and is_deleted = 0
                    """, reportMapper, userId, reportId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<EvaluationReport> findLatestReportForSet(Long userId, Long setId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, user_id, course_id, practice_set_id, overall_score,
                           knowledge_score_json, ability_summary, improvement_suggestion,
                           generated_by, created_at
                    from evaluation_report
                    where user_id = ? and practice_set_id = ? and is_deleted = 0
                    order by id desc
                    limit 1
                    """, reportMapper, userId, setId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Long insertReport(
            Long userId,
            Long courseId,
            Long setId,
            BigDecimal overallScore,
            String knowledgeScoreJson,
            String abilitySummary,
            String improvementSuggestion
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into evaluation_report(user_id, course_id, practice_set_id, overall_score,
                                                  knowledge_score_json, ability_summary,
                                                  improvement_suggestion, generated_by)
                    values (?, ?, ?, ?, ?, ?, ?, 'Coach')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, courseId);
            ps.setLong(3, setId);
            ps.setBigDecimal(4, overallScore);
            ps.setString(5, knowledgeScoreJson);
            ps.setString(6, abilitySummary);
            ps.setString(7, improvementSuggestion);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("evaluation report insert failed");
        }
        return key.longValue();
    }

    private Long insertRecord(
            Long userId,
            Long setId,
            Long questionId,
            String userAnswer,
            Boolean correct,
            BigDecimal score,
            String judgeMode,
            String feedbackText,
            boolean flagged
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into user_question_record(user_id, question_id, practice_set_id,
                                                     user_answer, is_correct, score, judge_mode,
                                                     feedback_text, is_flagged)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.setLong(3, setId);
            ps.setString(4, userAnswer);
            if (correct == null) {
                ps.setObject(5, null);
            } else {
                ps.setInt(5, correct ? 1 : 0);
            }
            ps.setBigDecimal(6, score == null ? BigDecimal.ZERO : score);
            ps.setString(7, judgeMode);
            ps.setString(8, feedbackText);
            ps.setInt(9, flagged ? 1 : 0);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("question record insert failed");
        }
        return key.longValue();
    }

    private String filterSql(Long userId, ExerciseSetFilter filter, List<Object> args) {
        if (filter == null) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        appendTypeFilter(filter.type(), sql, args);
        appendNodeFilter(filter.nodeId(), sql, args);
        appendPlanFilter(filter.planId(), sql, args);
        appendStatusFilter(userId, filter.status(), sql, args);
        return sql.toString();
    }

    private void appendTypeFilter(String type, StringBuilder sql, List<Object> args) {
        String safeType = type == null || type.isBlank() ? "all" : type.trim().toLowerCase();
        switch (safeType) {
            case "all", "available", "in_progress", "submitted", "graded" -> {
            }
            case "practice" -> sql.append(" and p.set_type in ('daily', 'review', 'practice')");
            case "test" -> sql.append(" and p.set_type in ('mock', 'exam', 'test')");
            case "challenge", "code_lab" -> {
                sql.append(" and p.set_type = ?");
                args.add(safeType);
            }
            default -> {
                sql.append(" and p.set_type = ?");
                args.add(safeType);
            }
        }
    }

    private void appendNodeFilter(String nodeId, StringBuilder sql, List<Object> args) {
        if (nodeId == null || nodeId.isBlank()) {
            return;
        }
        sql.append("""
                 and exists (
                    select 1
                    from practice_set_question psq
                    join question_bank q on q.id = psq.question_id and q.is_deleted = 0
                    join knowledge_node n on n.id = q.node_id and n.is_deleted = 0
                    where psq.practice_set_id = p.id
                """);
        String safeNodeId = nodeId.trim();
        if (safeNodeId.matches("\\d+")) {
            sql.append(" and q.node_id = ?)");
            args.add(Long.parseLong(safeNodeId));
        } else {
            sql.append(" and n.node_code = ?)");
            args.add(safeNodeId);
        }
    }

    private void appendPlanFilter(Long planId, StringBuilder sql, List<Object> args) {
        if (planId == null) {
            return;
        }
        sql.append("""
                 and exists (
                    select 1
                    from study_task st
                    where st.plan_id = ? and st.is_deleted = 0
                      and (
                        st.resource_id is null
                        or exists (
                            select 1
                            from learning_resource lr
                            where lr.id = st.resource_id and lr.resource_type = 'quiz'
                        )
                      )
                      and exists (
                        select 1
                        from practice_set_question psq2
                        join question_bank q2 on q2.id = psq2.question_id and q2.is_deleted = 0
                        where psq2.practice_set_id = p.id
                          and (st.node_id is null or st.node_id = q2.node_id)
                      )
                 )
                """);
        args.add(planId);
    }

    private void appendStatusFilter(Long userId, String status, StringBuilder sql, List<Object> args) {
        if (status == null || status.isBlank() || userId == null) {
            return;
        }
        String safeStatus = status.trim().toLowerCase();
        switch (safeStatus) {
            case "available" -> {
                sql.append("""
                         and not exists (
                            select 1 from user_question_record r
                            where r.user_id = ? and r.practice_set_id = p.id
                         )
                        """);
                args.add(userId);
            }
            case "in_progress" -> {
                sql.append("""
                         and exists (
                            select 1 from user_question_record r
                            where r.user_id = ? and r.practice_set_id = p.id
                         )
                         and not exists (
                            select 1 from evaluation_report er
                            where er.user_id = ? and er.practice_set_id = p.id and er.is_deleted = 0
                         )
                        """);
                args.add(userId);
                args.add(userId);
            }
            case "submitted", "graded" -> {
                sql.append("""
                         and exists (
                            select 1 from evaluation_report er
                            where er.user_id = ? and er.practice_set_id = p.id and er.is_deleted = 0
                         )
                        """);
                args.add(userId);
            }
            default -> {
            }
        }
    }

    private Integer toTinyInt(Boolean value) {
        return value == null ? null : (value ? 1 : 0);
    }

    private Boolean toBoolean(Integer value) {
        if (value == null) {
            return null;
        }
        return value == 1;
    }

    private String placeholders(int count) {
        return "?,".repeat(count - 1) + "?";
    }

    public List<PracticeSet> listSetsByIds(Collection<Long> setIds) {
        if (setIds == null || setIds.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                select p.id, p.course_id, c.course_name, p.set_name, p.set_type,
                       p.difficulty_level, p.created_at, p.updated_at
                from practice_set p
                join course c on c.id = p.course_id and c.is_deleted = 0
                where p.id in (%s) and p.is_deleted = 0
                """.formatted(placeholders(setIds.size())), setMapper, setIds.toArray());
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
