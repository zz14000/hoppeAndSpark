package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.enums.AgentIntent;
import org.springframework.stereotype.Component;

@Component
public class SparkEntry {

    public AgentIntent route(AgentRunRequest request) {
        String mode = request.agentMode() == null ? "" : request.agentMode().toLowerCase();
        String query = request.userQuery() == null ? "" : request.userQuery().toLowerCase();
        if ("diagram".equals(mode) || query.contains("流程图") || query.contains("mermaid")) {
            return AgentIntent.DIAGRAM;
        }
        if ("steps".equals(mode) || query.contains("步骤") || query.contains("解题")) {
            return AgentIntent.STEPS;
        }
        if ("rag".equals(mode) || query.contains("知识库") || query.contains("检索")) {
            return AgentIntent.RAG;
        }
        if ("graph".equals(mode)) {
            return AgentIntent.GRAPH;
        }
        if ("plan".equals(mode) || query.contains("学习计划")) {
            return AgentIntent.PLAN;
        }
        return AgentIntent.QA;
    }
}
