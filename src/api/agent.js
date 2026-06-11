import { http } from './request.js';
import { getAccessToken } from '../utils/auth.js';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

function buildUrl(path, params) {
  const url = new URL(path, BASE_URL || window.location.origin);
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value));
      }
    });
  }
  return BASE_URL ? url.toString() : `${url.pathname}${url.search}`;
}

/** 获取可用 Agent 列表 */
export const getAgents = () => http.get('/api/v1/agents');

/** 创建 Agent 会话 */
export const createAgentSession = (data) => http.post('/api/v1/agent-sessions', data);

/** 获取会话消息分页列表 */
export const getAgentMessages = (sessionId, params) =>
  http.get(`/api/v1/agent-sessions/${sessionId}/messages`, params);

/** 发送消息并触发 Agent 编排 */
export const sendAgentMessage = (sessionId, data) =>
  http.post(`/api/v1/agent-sessions/${sessionId}/messages`, data);

/**
 * 按 messageId 获取 SSE 流式事件（text/event-stream）
 * @param {string} sessionId
 * @param {{ messageId: string }} params
 * @returns {Promise<Response>} 原生 Response，供 ReadableStream 解析
 */
export const streamAgentSession = (sessionId, params) => {
  const headers = { Accept: 'text/event-stream' };
  const token = getAccessToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  return fetch(buildUrl(`/api/v1/agent-sessions/${sessionId}/stream`, params), {
    method: 'GET',
    headers,
  });
};

/**
 * 订阅 Agent SSE 流，解析 event/data 并回调
 * @param {string} sessionId
 * @param {string} messageId
 * @param {{ onEvent?: (event) => void, onError?: (err) => void, onDone?: () => void }} handlers
 */
export async function subscribeAgentStream(sessionId, messageId, handlers = {}) {
  const response = await streamAgentSession(sessionId, { messageId });
  if (!response.ok) {
    const err = new Error(`SSE 连接失败: HTTP ${response.status}`);
    handlers.onError?.(err);
    throw err;
  }

  const reader = response.body?.getReader();
  if (!reader) {
    const err = new Error('SSE 响应体不可读');
    handlers.onError?.(err);
    throw err;
  }

  const decoder = new TextDecoder();
  let buffer = '';

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const parts = buffer.split('\n\n');
      buffer = parts.pop() || '';

      for (const block of parts) {
        if (!block.trim()) continue;
        let eventType = 'message';
        let dataStr = '';
        for (const line of block.split('\n')) {
          if (line.startsWith('event:')) eventType = line.slice(6).trim();
          if (line.startsWith('data:')) dataStr += line.slice(5).trim();
        }
        let payload = dataStr;
        try {
          payload = dataStr ? JSON.parse(dataStr) : null;
        } catch {
          /* 保留原始字符串 */
        }
        handlers.onEvent?.({ type: eventType, data: payload, raw: dataStr });
        if (eventType === 'done' || eventType === 'error') {
          handlers.onDone?.();
          return;
        }
      }
    }
    handlers.onDone?.();
  } catch (e) {
    handlers.onError?.(e);
    throw e;
  } finally {
    reader.releaseLock();
  }
}

/** 提交 AI 内容争议 */
export const createAiDispute = (data) => http.post('/api/v1/ai-disputes', data);
