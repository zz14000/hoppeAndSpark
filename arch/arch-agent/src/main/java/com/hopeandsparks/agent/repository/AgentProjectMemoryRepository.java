package com.hopeandsparks.agent.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgentProjectMemoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgentProjectMemoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public List<AgentProjectMemoryRecord> listByProjectAndPoints(String projectId, String userId, List<String> knowledgePoints) {
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            return jdbcTemplate.query("""
                    select project_id, user_id, course_id, course_name, knowledge_point, mastery_level,
                           weakness_tags_json, last_learning_plan_json, updated_at
                    from agent_project_memory
                    where project_id = ? and user_id = ?
                    order by updated_at desc
                    limit 10
                    """, (rs, rowNum) -> new AgentProjectMemoryRecord(
                    rs.getString("project_id"),
                    rs.getString("user_id"),
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getString("knowledge_point"),
                    rs.getInt("mastery_level"),
                    rs.getString("weakness_tags_json"),
                    rs.getString("last_learning_plan_json"),
                    rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime()
            ), projectId, userId);
        }
        String placeholders = String.join(",", knowledgePoints.stream().map(item -> "?").toList());
        Object[] args = new Object[knowledgePoints.size() + 2];
        args[0] = projectId;
        args[1] = userId;
        for (int index = 0; index < knowledgePoints.size(); index++) {
            args[index + 2] = knowledgePoints.get(index);
        }
        return jdbcTemplate.query("""
                select project_id, user_id, course_id, course_name, knowledge_point, mastery_level,
                       weakness_tags_json, last_learning_plan_json, updated_at
                from agent_project_memory
                where project_id = ? and user_id = ? and knowledge_point in (""" + placeholders + ")",
                (rs, rowNum) -> new AgentProjectMemoryRecord(
                        rs.getString("project_id"),
                        rs.getString("user_id"),
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getString("knowledge_point"),
                        rs.getInt("mastery_level"),
                        rs.getString("weakness_tags_json"),
                        rs.getString("last_learning_plan_json"),
                        rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                args
        );
    }

    public void save(AgentProjectMemoryRecord record) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from agent_project_memory
                where project_id = ? and user_id = ? and knowledge_point = ?
                """, Integer.class, record.projectId(), record.userId(), record.knowledgePoint());
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    update agent_project_memory
                    set course_id = ?, course_name = ?, mastery_level = ?, weakness_tags_json = ?, last_learning_plan_json = ?, updated_at = current_timestamp
                    where project_id = ? and user_id = ? and knowledge_point = ?
                    """,
                    record.courseId(), record.courseName(), record.masteryLevel(), record.weaknessTagsJson(), record.lastLearningPlanJson(),
                    record.projectId(), record.userId(), record.knowledgePoint());
            return;
        }
        jdbcTemplate.update("""
                insert into agent_project_memory(project_id, user_id, course_id, course_name, knowledge_point, mastery_level, weakness_tags_json, last_learning_plan_json, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                record.projectId(), record.userId(), record.courseId(), record.courseName(), record.knowledgePoint(),
                record.masteryLevel(), record.weaknessTagsJson(), record.lastLearningPlanJson());
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists agent_project_memory(
                    id bigint primary key auto_increment,
                    project_id varchar(64) not null,
                    user_id varchar(64) not null,
                    course_id varchar(64) not null default '',
                    course_name varchar(255) not null default '',
                    knowledge_point varchar(255) not null,
                    mastery_level int not null default 0,
                    weakness_tags_json longtext null,
                    last_learning_plan_json longtext null,
                    updated_at datetime not null default current_timestamp on update current_timestamp,
                    unique key uk_project_user_knowledge(project_id, user_id, knowledge_point)
                )
                """);
    }
}
