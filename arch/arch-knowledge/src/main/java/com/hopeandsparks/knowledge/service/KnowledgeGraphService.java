package com.hopeandsparks.knowledge.service;

import com.hopeandsparks.knowledge.entity.Course;
import com.hopeandsparks.knowledge.entity.KnowledgeNode;
import com.hopeandsparks.knowledge.entity.KnowledgeRelation;
import com.hopeandsparks.knowledge.vo.KnowledgeGraphVO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KnowledgeGraphService {

    KnowledgeGraphVO graph(String keyword, int depth);

    List<Course> listActiveCourses();

    Optional<Course> findCourse(String courseId, String keyword);

    Optional<KnowledgeNode> findNode(String nodeIdOrCode);

    List<KnowledgeNode> listActiveNodesByCourse(Long courseId);

    List<KnowledgeNode> listNodesByIds(Collection<Long> nodeIds);

    List<KnowledgeRelation> listRelationsBetween(Collection<Long> nodeIds);

    List<KnowledgeRelation> listRelationsAround(Collection<Long> nodeIds);
}
