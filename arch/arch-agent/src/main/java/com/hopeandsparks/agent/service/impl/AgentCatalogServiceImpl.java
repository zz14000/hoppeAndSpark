package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.service.AgentCatalogService;
import com.hopeandsparks.agent.vo.AgentInfoVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentCatalogServiceImpl implements AgentCatalogService {

    @Override
    public List<AgentInfoVO> listAgents() {
        return List.of(
                new AgentInfoVO("sage", "Sage", "文本问答与知识解释", List.of("qa", "rag", "explain"), true),
                new AgentInfoVO("coach", "Coach", "解题步骤与错因诊断", List.of("steps", "hint", "diagnose"), true),
                new AgentInfoVO("strict", "Strict", "学习计划与节奏管理", List.of("plan", "checkpoint"), true),
                new AgentInfoVO("nebula", "Nebula", "流程图、思维导图与资源图谱", List.of("diagram", "mindmap", "resource_graph"), true)
        );
    }
}
