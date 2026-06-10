package com.hopeandsparks.infra.coze;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Coze 的空实现。
 * 先返回固定提示，避免 W1 依赖 Coze token 或真实外部服务。
 */
@Service
public class MockCozeAgentClient implements CozeAgentClient {

    @Override
    public CozeAgentResponse chat(CozeAgentRequest request) {
        return mockResponse(request, "Agent 接入暂未开启，当前为 mock 对话结果。");
    }

    @Override
    public CozeAgentResponse runWorkflow(CozeAgentRequest request) {
        return mockResponse(request, "Agent Workflow 暂未接入，当前只返回 mock 结果。");
    }

    private CozeAgentResponse mockResponse(CozeAgentRequest request, String output) {
        String agentCode = request == null ? "unknown" : request.agentCode();
        return new CozeAgentResponse(
                agentCode,
                output,
                "mock_conversation_" + UUID.randomUUID().toString().replace("-", ""),
                "mock_message_" + UUID.randomUUID().toString().replace("-", ""),
                true
        );
    }
}
