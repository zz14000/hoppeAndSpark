package com.hopeandsparks.knowledge.service.impl;

import com.hopeandsparks.knowledge.entity.Course;
import com.hopeandsparks.knowledge.entity.KnowledgeNode;
import com.hopeandsparks.knowledge.entity.KnowledgeRelation;
import com.hopeandsparks.knowledge.repository.KnowledgeRepository;
import com.hopeandsparks.knowledge.service.KnowledgeGraphService;
import com.hopeandsparks.knowledge.vo.KnowledgeGraphEdgeVO;
import com.hopeandsparks.knowledge.vo.KnowledgeGraphNodeVO;
import com.hopeandsparks.knowledge.vo.KnowledgeGraphVO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeGraphServiceImpl(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public KnowledgeGraphVO graph(String keyword, int depth) {
        int safeDepth = Math.max(0, Math.min(depth, 3));
        Map<Long, KnowledgeNode> nodes = new LinkedHashMap<>();
        List<KnowledgeNode> seeds = knowledgeRepository.listGraphSeedNodes(blankToNull(keyword));
        seeds.forEach(node -> nodes.put(node.id(), node));

        Set<Long> frontier = new LinkedHashSet<>(nodes.keySet());
        for (int i = 0; i < safeDepth && !frontier.isEmpty(); i++) {
            List<KnowledgeRelation> relations = knowledgeRepository.listRelationsAround(frontier);
            Set<Long> nextIds = new LinkedHashSet<>();
            for (KnowledgeRelation relation : relations) {
                if (!nodes.containsKey(relation.sourceNodeId())) {
                    nextIds.add(relation.sourceNodeId());
                }
                if (!nodes.containsKey(relation.targetNodeId())) {
                    nextIds.add(relation.targetNodeId());
                }
            }
            knowledgeRepository.listNodesByIds(nextIds).forEach(node -> nodes.put(node.id(), node));
            frontier = nextIds;
        }

        List<KnowledgeRelation> relations = knowledgeRepository.listRelationsBetween(nodes.keySet());
        return new KnowledgeGraphVO(
                nodes.values().stream().map(this::toNodeVO).toList(),
                relations.stream().map(this::toEdgeVO).toList()
        );
    }

    @Override
    public List<Course> listActiveCourses() {
        return knowledgeRepository.listActiveCourses();
    }

    @Override
    public Optional<Course> findCourse(String courseId, String keyword) {
        Long parsedCourseId = parseId(courseId);
        if (parsedCourseId != null) {
            return knowledgeRepository.findCourseById(parsedCourseId);
        }
        String safeKeyword = blankToNull(keyword);
        if (safeKeyword != null) {
            return knowledgeRepository.findCourseByKeyword(safeKeyword);
        }
        return knowledgeRepository.listActiveCourses().stream().findFirst();
    }

    @Override
    public Optional<KnowledgeNode> findNode(String nodeIdOrCode) {
        Long parsedNodeId = parseId(nodeIdOrCode);
        if (parsedNodeId != null) {
            return knowledgeRepository.findNodeById(parsedNodeId);
        }
        return knowledgeRepository.findNodeByCode(nodeIdOrCode);
    }

    @Override
    public List<KnowledgeNode> listActiveNodesByCourse(Long courseId) {
        return knowledgeRepository.listActiveNodesByCourse(courseId);
    }

    @Override
    public List<KnowledgeNode> listNodesByIds(Collection<Long> nodeIds) {
        return knowledgeRepository.listNodesByIds(nodeIds);
    }

    @Override
    public List<KnowledgeRelation> listRelationsBetween(Collection<Long> nodeIds) {
        return knowledgeRepository.listRelationsBetween(nodeIds);
    }

    @Override
    public List<KnowledgeRelation> listRelationsAround(Collection<Long> nodeIds) {
        return knowledgeRepository.listRelationsAround(nodeIds);
    }

    private KnowledgeGraphNodeVO toNodeVO(KnowledgeNode node) {
        return new KnowledgeGraphNodeVO(
                String.valueOf(node.id()),
                node.nodeName(),
                node.nodeDesc(),
                node.nodeCode(),
                String.valueOf(node.courseId()),
                node.parentId() == null ? null : String.valueOf(node.parentId()),
                node.difficultyLevel()
        );
    }

    private KnowledgeGraphEdgeVO toEdgeVO(KnowledgeRelation relation) {
        return new KnowledgeGraphEdgeVO(
                String.valueOf(relation.id()),
                String.valueOf(relation.sourceNodeId()),
                String.valueOf(relation.targetNodeId()),
                relation.relationType()
        );
    }

    private Long parseId(String value) {
        String safeValue = blankToNull(value);
        if (safeValue == null) {
            return null;
        }
        String digits = safeValue.matches("\\d+") ? safeValue : safeValue.replaceFirst("^.*_(\\d+)$", "$1");
        if (!digits.matches("\\d+")) {
            return null;
        }
        return Long.parseLong(digits);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
