package com.hopeandsparks.agent.runtime;

import com.hopeandsparks.agent.orchestration.AgentGraphState;

public interface GraphRuntime {

    AgentGraphState run(AgentGraphState initialState);
}
