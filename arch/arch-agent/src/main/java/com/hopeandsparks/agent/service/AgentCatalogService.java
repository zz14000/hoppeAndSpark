package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.vo.AgentInfoVO;

import java.util.List;

public interface AgentCatalogService {

    List<AgentInfoVO> listAgents();

    AgentInfoVO getAgent(String agentId);

    boolean isWorkflowAgent(String agentId);
}
