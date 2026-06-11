import { http } from './request.js';

/** 获取会话列表 */
export const getChats = (params) => http.get('/api/v1/chats', params);

/** 获取聊天记录 */
export const getChatMessages = (chatId, params) =>
  http.get(`/api/v1/chats/${chatId}/messages`, params);

/** 发送消息 */
export const sendChatMessage = (chatId, data) =>
  http.post(`/api/v1/chats/${chatId}/messages`, data);

/** 创建私聊 */
export const createPrivateChat = (data) => http.post('/api/v1/chats/private', data);

/** 创建群聊 */
export const createGroupChat = (data) => http.post('/api/v1/chats/groups', data);

/** 发起好友申请 */
export const createFriendRequest = (data) => http.post('/api/v1/friend-requests', data);

/** 处理好友申请 */
export const handleFriendRequest = (requestId, data) =>
  http.put(`/api/v1/friend-requests/${requestId}`, data);
