package com.hopeandsparks.infra.coze;

/**
 * Coze 调用客户端接口。
 * W1 只保留边界，真实 Coze SDK、Bot ID、Workflow ID 都先不接。
 */
public interface CozeAgentClient {

    CozeAgentResponse chat(CozeAgentRequest request);

    CozeAgentResponse runWorkflow(CozeAgentRequest request);
}
