package com.hopeandsparks.infra.llm;

/**
 * 通用 LLM 适配接口，用来预留 Coze 之外的模型调用能力。
 *
 * <p>MVP 的智能体长流程直接走 Coze，但摘要、标签提取、审核、embedding 或后续模型切换
 * 可以通过这个接口扩展讯飞星火、OpenAI、DashScope 等供应商，避免业务代码绑定单一 SDK。</p>
 */
public interface LlmGateway {
}
