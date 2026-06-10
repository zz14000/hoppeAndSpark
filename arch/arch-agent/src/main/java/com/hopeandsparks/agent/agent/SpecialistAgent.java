package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;

import java.util.Map;

public interface SpecialistAgent {

    AgentName name();

    AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context);
}
