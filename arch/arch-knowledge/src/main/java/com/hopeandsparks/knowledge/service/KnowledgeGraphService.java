package com.hopeandsparks.knowledge.service;

import com.hopeandsparks.knowledge.vo.KnowledgeGraphVO;

public interface KnowledgeGraphService {

    KnowledgeGraphVO graph(String keyword, int depth);
}
