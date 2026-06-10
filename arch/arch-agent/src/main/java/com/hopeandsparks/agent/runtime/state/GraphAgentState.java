package com.hopeandsparks.agent.runtime.state;

import com.hopeandsparks.agent.orchestration.AgentGraphState;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class GraphAgentState extends AgentState {

    public static final String BUSINESS_STATE = "businessState";

    public GraphAgentState(Map<String, Object> data) {
        super(data);
    }

    public AgentGraphState businessState() {
        return value(BUSINESS_STATE, (AgentGraphState) null);
    }
}
