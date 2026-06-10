package com.hopeandsparks.profile.repository;

import com.hopeandsparks.profile.dto.UserProfileUpdateRequest;
import com.hopeandsparks.profile.entity.CollectionItem;
import com.hopeandsparks.profile.entity.LearningPlanStat;
import com.hopeandsparks.profile.entity.SparkProfile;
import com.hopeandsparks.profile.entity.UserProfileDetail;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SparkProfile> profileMapper = (rs, rowNum) -> new SparkProfile(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("major_domain"),
            rs.getString("grade_level"),
            rs.getString("knowledge_base_level"),
            rs.getString("cognitive_style"),
            rs.getString("learning_preference"),
            rs.getString("error_preference"),
            rs.getString("learning_goal"),
            rs.getString("self_discipline"),
            rs.getString("current_weakness"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<UserProfileDetail> detailMapper = (rs, rowNum) -> {
        Long profileId = rs.getObject("profile_id", Long.class);
        return new UserProfileDetail(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                profileId,
                rs.getString("major_domain"),
                rs.getString("grade_level"),
                rs.getString("knowledge_base_level"),
                rs.getString("cognitive_style"),
                rs.getString("learning_preference"),
                rs.getString("learning_goal"),
                rs.getString("self_discipline"),
                rs.getString("current_weakness")
        );
    };

    private final RowMapper<CollectionItem> collectionMapper = (rs, rowNum) -> new CollectionItem(
            rs.getLong("id"),
            rs.getString("target_type"),
            rs.getLong("target_id"),
            rs.getString("title"),
            rs.getString("summary"),
            toLocalDateTime(rs.getTimestamp("created_at"))
    );

    private final RowMapper<LearningPlanStat> planStatMapper = (rs, rowNum) -> new LearningPlanStat(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("current_stage"),
            rs.getLong("finished_count"),
            rs.getLong("total_count"),
            rs.getInt("progress")
    );

    public ProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SparkProfile> findByUserId(Long userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, user_id, major_domain, grade_level, knowledge_base_level,
                           cognitive_style, learning_preference, json_unquote(json_extract(error_preference, '$')) as error_preference,
                           learning_goal, self_discipline, current_weakness, created_at, updated_at
                    from user_profile
                    where user_id = ? and is_deleted = 0
                    """, profileMapper, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public SparkProfile upsertProfile(
            Long userId,
            String majorDomain,
            String gradeLevel,
            String knowledgeBaseLevel,
            String cognitiveStyle,
            String learningPreference,
            String errorPreferenceJson,
            String learningGoal,
            String selfDiscipline,
            String currentWeakness
    ) {
        jdbcTemplate.update("""
                insert into user_profile(user_id, major_domain, grade_level, knowledge_base_level,
                                         cognitive_style, learning_preference, error_preference,
                                         learning_goal, self_discipline, current_weakness)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on duplicate key update
                    major_domain = values(major_domain),
                    grade_level = values(grade_level),
                    knowledge_base_level = values(knowledge_base_level),
                    cognitive_style = values(cognitive_style),
                    learning_preference = values(learning_preference),
                    error_preference = values(error_preference),
                    learning_goal = values(learning_goal),
                    self_discipline = values(self_discipline),
                    current_weakness = values(current_weakness),
                    is_deleted = 0
                """,
                blankToDefault(majorDomain, "通用学习"),
                blankToNull(gradeLevel),
                blankToDefault(knowledgeBaseLevel, "beginner"),
                blankToNull(cognitiveStyle),
                blankToNull(learningPreference),
                blankToDefault(errorPreferenceJson, "{}"),
                blankToNull(learningGoal),
                blankToDefault(selfDiscipline, "B"),
                blankToNull(currentWeakness)
        );
        return findByUserId(userId).orElseThrow(() -> new IllegalStateException("profile upsert failed"));
    }

    public void updateUserBasic(Long userId, UserProfileUpdateRequest request) {
        jdbcTemplate.update("""
                update sys_user
                set nickname = coalesce(?, nickname),
                    avatar_url = coalesce(?, avatar_url)
                where id = ? and is_deleted = 0
                """, blankToNull(request.nickname()), blankToNull(request.avatar()), userId);
    }

    public Optional<UserProfileDetail> findUserDetail(Long userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select u.id as user_id, u.username, u.nickname, u.avatar_url,
                           p.id as profile_id, p.major_domain, p.grade_level,
                           p.knowledge_base_level, p.cognitive_style, p.learning_preference,
                           p.learning_goal, p.self_discipline, p.current_weakness
                    from sys_user u
                    left join user_profile p on p.user_id = u.id and p.is_deleted = 0
                    where u.id = ? and u.is_deleted = 0
                    """, detailMapper, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void insertProfileMemory(Long userId, Long profileId, String summary, String memoryJson) {
        jdbcTemplate.update("""
                insert into agent_memory(user_id, memory_scope, memory_type, memory_key,
                                         memory_text, memory_json, importance_score,
                                         confidence_score, source_type, source_ref_id, valid_status)
                values (?, 'user_private', 'profile', 'spark_profile',
                        ?, ?, 80.00, 70.00, 'profile', ?, 1)
                """, userId, summary, blankToDefault(memoryJson, "{}"), profileId);
    }

    public void invalidateProfileMemories(Long userId) {
        jdbcTemplate.update("""
                update agent_memory
                set valid_status = 0
                where user_id = ? and memory_type = 'profile' and memory_key = 'spark_profile'
                  and is_deleted = 0
                """, userId);
    }

    public long totalStudySeconds(Long userId) {
        Long total = jdbcTemplate.queryForObject("""
                select coalesce(sum(duration_seconds), 0)
                from user_learning_record
                where user_id = ?
                """, Long.class, userId);
        return total == null ? 0 : total;
    }

    public long countDistinctStudiedResources(Long userId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(distinct resource_id)
                from user_learning_record
                where user_id = ? and resource_id is not null
                """, Long.class, userId);
        return total == null ? 0 : total;
    }

    public int averageKnowledgeProgress(Long userId) {
        BigDecimal avg = jdbcTemplate.queryForObject("""
                select coalesce(avg(progress_percent), 0)
                from user_knowledge_progress
                where user_id = ?
                """, BigDecimal.class, userId);
        return avg == null ? 0 : avg.intValue();
    }

    public List<LearningPlanStat> listPlanStats(Long userId) {
        return jdbcTemplate.query("""
                select p.id,
                       p.plan_title as title,
                       coalesce(max(case when t.task_status = 1 then t.task_title end), '未开始') as current_stage,
                       coalesce(sum(case when t.task_status = 2 then 1 else 0 end), 0) as finished_count,
                       count(t.id) as total_count,
                       case when count(t.id) = 0 then 0
                            else round(sum(case when t.task_status = 2 then 1 else 0 end) * 100 / count(t.id))
                       end as progress
                from study_plan p
                left join study_task t on t.plan_id = p.id and t.is_deleted = 0
                where p.user_id = ? and p.is_deleted = 0
                group by p.id, p.plan_title
                order by p.updated_at desc, p.id desc
                limit 5
                """, planStatMapper, userId);
    }

    public List<CollectionItem> listResourceCollections(Long userId) {
        return jdbcTemplate.query("""
                select f.id, 'resource' as target_type, r.id as target_id,
                       r.title, r.summary, f.created_at
                from user_resource_favorite f
                join learning_resource r on r.id = f.resource_id
                where f.user_id = ? and r.is_deleted = 0
                order by f.created_at desc
                """, collectionMapper, userId);
    }

    public List<CollectionItem> listArticleCollections(Long userId) {
        return jdbcTemplate.query("""
                select f.id, 'article' as target_type, p.id as target_id,
                       p.title, p.summary, f.created_at
                from blog_favorite f
                join blog_post p on p.id = f.post_id
                where f.user_id = ? and p.is_deleted = 0
                order by f.created_at desc
                """, collectionMapper, userId);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
