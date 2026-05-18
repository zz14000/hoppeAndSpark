package com.hopeandsparks.infra.coze;

/**
 * Coze 调用客户端接口，统一封装 Bot 对话和 Workflow 执行。
 *
 * <p>业务模块只传 agentCode、输入参数和上下文，不直接关心 Coze Bot ID、Workflow ID、
 * spaceId 或 HTTP 细节。运行路由配置来自 {@code sys_agent_config}，由 infra 模块管理。</p>
 */
public interface CozeAgentClient {
}
