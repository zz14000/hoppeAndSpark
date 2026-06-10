package com.hopeandsparks.agent.runtime;

import com.hopeandsparks.agent.orchestration.AgentGraphState;

public interface LinearRuntime {

    AgentGraphState run(AgentGraphState initialState);
}
