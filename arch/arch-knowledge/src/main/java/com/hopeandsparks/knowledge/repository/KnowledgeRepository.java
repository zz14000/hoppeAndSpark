package com.hopeandsparks.knowledge.repository;

import com.hopeandsparks.knowledge.entity.Course;
import com.hopeandsparks.knowledge.entity.KnowledgeNode;
import com.hopeandsparks.knowledge.entity.KnowledgeRelation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class KnowledgeRepository {

    private static final int GRAPH_NODE_LIMIT = 80;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Course> courseMapper = (rs, rowNum) -> new Course(
            rs.getLong("id"),
            rs.getString("course_code"),
            rs.getString("course_name"),
            rs.getString("course_desc"),
            rs.getString("major_domain"),
            rs.getString("difficulty_level")
    );

    private final RowMapper<KnowledgeNode> nodeMapper = (rs, rowNum) -> new KnowledgeNode(
            rs.getLong("id"),
            rs.getLong("course_id"),
            rs.getObject("parent_id", Long.class),
            rs.getString("node_code"),
            rs.getString("node_name"),
            rs.getString("node_desc"),
            rs.getString("difficulty_level"),
            rs.getInt("sort_order")
    );

    private final RowMapper<KnowledgeRelation> relationMapper = (rs, rowNum) -> new KnowledgeRelation(
            rs.getLong("id"),
            rs.getLong("source_node_id"),
            rs.getLong("target_node_id"),
            rs.getString("relation_type")
    );

    public KnowledgeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Course> listActiveCourses() {
        return jdbcTemplate.query("""
                select id, course_code, course_name, course_desc, major_domain, difficulty_level
                from course
                where status = 1 and is_deleted = 0
                order by id asc
                """, courseMapper);
    }

    public Optional<Course> findCourseById(Long courseId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, course_code, course_name, course_desc, major_domain, difficulty_level
                    from course
                    where id = ? and status = 1 and is_deleted = 0
                    """, courseMapper, courseId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Course> findCourseByKeyword(String keyword) {
        String like = "%" + keyword + "%";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, course_code, course_name, course_desc, major_domain, difficulty_level
                    from course
                    where status = 1 and is_deleted = 0
                      and (course_name like ? or major_domain like ? or course_code like ?)
                    order by id asc
                    limit 1
                    """, courseMapper, like, like, like));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<KnowledgeNode> listActiveNodesByCourse(Long courseId) {
        return jdbcTemplate.query("""
                select id, course_id, parent_id, node_code, node_name, node_desc, difficulty_level, sort_order
                from knowledge_node
                where course_id = ? and status = 1 and is_deleted = 0
                order by sort_order asc, id asc
                """, nodeMapper, courseId);
    }

    public List<KnowledgeNode> listGraphSeedNodes(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return jdbcTemplate.query("""
                    select id, course_id, parent_id, node_code, node_name, node_desc, difficulty_level, sort_order
                    from knowledge_node
                    where status = 1 and is_deleted = 0
                    order by course_id asc, sort_order asc, id asc
                    limit ?
                    """, nodeMapper, GRAPH_NODE_LIMIT);
        }
        String like = "%" + keyword + "%";
        return jdbcTemplate.query("""
                select n.id, n.course_id, n.parent_id, n.node_code, n.node_name,
                       n.node_desc, n.difficulty_level, n.sort_order
                from knowledge_node n
                join course c on c.id = n.course_id and c.is_deleted = 0
                where n.status = 1 and n.is_deleted = 0
                  and (n.node_name like ? or n.node_code like ? or n.node_desc like ?
                       or c.course_name like ? or c.major_domain like ?)
                order by n.course_id asc, n.sort_order asc, n.id asc
                limit ?
                """, nodeMapper, like, like, like, like, like, GRAPH_NODE_LIMIT);
    }

    public Optional<KnowledgeNode> findNodeById(Long nodeId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, course_id, parent_id, node_code, node_name, node_desc, difficulty_level, sort_order
                    from knowledge_node
                    where id = ? and status = 1 and is_deleted = 0
                    """, nodeMapper, nodeId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<KnowledgeNode> findNodeByCode(String nodeCode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, course_id, parent_id, node_code, node_name, node_desc, difficulty_level, sort_order
                    from knowledge_node
                    where node_code = ? and status = 1 and is_deleted = 0
                    """, nodeMapper, nodeCode));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<KnowledgeNode> listNodesByIds(Collection<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }
        String placeholders = placeholders(nodeIds.size());
        return jdbcTemplate.query("""
                select id, course_id, parent_id, node_code, node_name, node_desc, difficulty_level, sort_order
                from knowledge_node
                where status = 1 and is_deleted = 0
                  and id in (%s)
                order by sort_order asc, id asc
                """.formatted(placeholders), nodeMapper, nodeIds.toArray());
    }

    public List<KnowledgeRelation> listRelationsBetween(Collection<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }
        String placeholders = placeholders(nodeIds.size());
        return jdbcTemplate.query("""
                select id, source_node_id, target_node_id, relation_type
                from knowledge_node_relation
                where source_node_id in (%s)
                  and target_node_id in (%s)
                order by id asc
                """.formatted(placeholders, placeholders), relationMapper, repeatArgs(nodeIds, nodeIds));
    }

    public List<KnowledgeRelation> listRelationsAround(Collection<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }
        String placeholders = placeholders(nodeIds.size());
        return jdbcTemplate.query("""
                select id, source_node_id, target_node_id, relation_type
                from knowledge_node_relation
                where source_node_id in (%s)
                   or target_node_id in (%s)
                order by id asc
                """.formatted(placeholders, placeholders), relationMapper, repeatArgs(nodeIds, nodeIds));
    }

    private String placeholders(int count) {
        return "?,".repeat(count - 1) + "?";
    }

    private Object[] repeatArgs(Collection<Long> first, Collection<Long> second) {
        List<Long> values = new ArrayList<>(first.size() + second.size());
        values.addAll(first);
        values.addAll(second);
        return values.toArray();
    }
}
