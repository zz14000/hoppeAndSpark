import { http } from './request.js';

/** 获取智能体列表 */
export const getAgents = () => http.get('/api/v1/agents');

/** 创建对话会话 */
export const createAgentSession = (data) => http.post('/api/v1/agent-sessions', data);

/** 获取会话历史 */
export const getAgentMessages = (sessionId, params) =>
  http.get(`/api/v1/agent-sessions/${sessionId}/messages`, params);

/** 发送消息给智能体 */
export const sendAgentMessage = (sessionId, data) =>
  http.post(`/api/v1/agent-sessions/${sessionId}/messages`, data);

/** Agent 流式响应 */
export const streamAgentSession = (sessionId, params) =>
  http.get(`/api/v1/agent-sessions/${sessionId}/stream`, params);

/** 举报 AI 内容争议 */
export const createAiDispute = (data) => http.post('/api/v1/ai-disputes', data);
