package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentTaskResult;

public interface AgentOutputValidator {

    void validate(AgentTaskResult result);
}
