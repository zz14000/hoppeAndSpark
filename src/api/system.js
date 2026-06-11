import { http, USE_MOCK } from './request.js';
import { MOCK_NOTIFICATIONS, MOCK_CALENDAR_EVENTS, delay } from '../mock/index.js';

function normalizeNotification(item) {
  if (!item) return item;
  return {
    ...item,
    unread: item.unread ?? !item.read ?? true,
    time: item.time || item.createdAt || '',
    content: item.content || item.body || '',
  };
}

function extractCalendarEvents(data) {
  if (Array.isArray(data)) return data;
  if (data?.events) return data.events;
  if (data?.list) return data.list;
  return [];
}

function normalizeCalendarEvent(event) {
  if (!event) return event;
  const start = event.startTime || event.startAt;
  const date = event.date || (start ? String(start).slice(0, 10) : '');
  const time = event.time || (start ? String(start).slice(11, 16) : '');
  return {
    ...event,
    date,
    time,
    desc: event.desc || event.remark || event.description || '',
    status: event.status || '待完成',
    type: event.type || 'blue',
  };
}

export const getNotifications = async (params = {}) => {
  if (USE_MOCK) {
    await delay(300);
    return { code: 200, message: 'success', data: MOCK_NOTIFICATIONS };
  }
  const res = await http.get('/api/v1/notifications', {
    page: params.page ?? 1,
    pageSize: params.pageSize ?? 50,
    type: params.type,
  });
  if (res.code === 200 && res.data?.list) {
    return { ...res, data: res.data.list.map(normalizeNotification) };
  }
  if (res.code === 200 && Array.isArray(res.data)) {
    return { ...res, data: res.data.map(normalizeNotification) };
  }
  return res;
};

export const markNotificationRead = async (id) => {
  if (USE_MOCK) {
    await delay(200);
    return { code: 200, message: '操作成功' };
  }
  if (id === 'all') {
    return http.put('/api/v1/notifications/read-all');
  }
  return http.put(`/api/v1/notifications/${id}/read`);
};

export const deleteNotification = (notificationId) =>
  http.delete(`/api/v1/notifications/${notificationId}`);

export const executeNotificationAction = (notificationId, data) =>
  http.post(`/api/v1/notifications/${notificationId}/actions`, data);

export const getCalendarEvents = async (month) => {
  if (USE_MOCK) {
    await delay(400);
    return { code: 200, message: 'success', data: MOCK_CALENDAR_EVENTS };
  }
  const res = await http.get('/api/v1/calendar/events', { month });
  if (res.code === 200) {
    return { ...res, data: extractCalendarEvents(res.data).map(normalizeCalendarEvent) };
  }
  return res;
};

export const createCalendarEvent = (data) => http.post('/api/v1/calendar/events', data);

export const updateCalendarEvent = (eventId, data) =>
  http.put(`/api/v1/calendar/events/${eventId}`, data);

export const deleteCalendarEvent = (eventId) =>
  http.delete(`/api/v1/calendar/events/${eventId}`);

export const exploreNebula = async (query, options = {}) => {
  if (USE_MOCK) {
    await delay(1500);
    return {
      code: 200,
      message: 'success',
      data: {
        summary: `关于"${query}"，核心本质在于...（此处为 AI 生成摘要）`,
        resources: [
          { id: 1, type: 'document', title: `${query} 源码级原理解析` },
          { id: 2, type: 'video', title: `动画图解：${query} 核心痛点` },
          { id: 3, type: 'quiz', title: `Coach 压力面试：${query}` },
        ],
        relatedNodes: ['关联知识点1', '关联知识点2', '关联知识点3'],
      },
    };
  }
  const res = await http.post('/api/v1/explore', { keyword: query, ...options });
  if (res.code === 200 && res.data) {
    const data = res.data;
    return {
      ...res,
      data: {
        summary: data.summary || data.answer || data.content || '',
        resources: data.resources || data.recommendedResources || [],
        relatedNodes: data.relatedNodes || data.nodes || [],
        exploreId: data.exploreId || data.id,
        ...data,
      },
    };
  }
  return res;
};

export const getExploreDetail = (exploreId) => http.get(`/api/v1/explore/${exploreId}`);

export const generateMindmap = (exploreId, data) =>
  http.post(`/api/v1/explore/${exploreId}/mindmap`, data);

export const getKnowledgeGraph = (params) => http.get('/api/v1/knowledge-graph', params);
