package com.hopeandsparks.knowledge.service.impl;

import com.hopeandsparks.knowledge.service.KnowledgeGraphService;
import com.hopeandsparks.knowledge.vo.KnowledgeGraphVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    @Override
    public KnowledgeGraphVO graph(String keyword, int depth) {
        String root = keyword == null || keyword.isBlank() ? "root" : keyword;
        return new KnowledgeGraphVO(
                List.of(
                        new KnowledgeGraphVO.Node("n1", root, "topic"),
                        new KnowledgeGraphVO.Node("n2", "related concept", "knowledge")
                ),
                List.of(new KnowledgeGraphVO.Edge("n1", "n2", "related")),
                true
        );
    }
}
