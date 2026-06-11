import { http, USE_MOCK } from './request.js';
import { MOCK_USER, delay } from '../mock/index.js';

export const getCurrentUser = async () => {
  if (USE_MOCK) {
    await delay(300);
    return { code: 200, message: 'success', data: MOCK_USER };
  }
  return http.get('/api/v1/user/current');
};

export const getUserById = async (userId) => {
  if (USE_MOCK) {
    await delay(400);
    return { code: 200, message: 'success', data: { ...MOCK_USER, id: userId } };
  }
  return http.get(`/api/v1/users/${userId}`);
};

export const updateProfile = async (profileData) => {
  if (USE_MOCK) {
    await delay(600);
    return { code: 200, message: '更新成功', data: { ...MOCK_USER, ...profileData } };
  }
  return http.put('/api/v1/user/profile', profileData);
};

export const getLearningStats = () =>
  USE_MOCK
    ? delay(300).then(() => ({ code: 200, message: 'success', data: {} }))
    : http.get('/api/v1/user/learning-stats');

export const getCollections = (params) =>
  USE_MOCK
    ? delay(300).then(() => ({ code: 200, message: 'success', data: { total: 0, list: [] } }))
    : http.get('/api/v1/user/collections', params);

/** 收藏或取消收藏 */
export const toggleCollection = (data) => http.post('/api/v1/user/collections', data);

export const removeCollection = (collectionId) =>
  http.delete(`/api/v1/user/collections/${collectionId}`);

export const followUser = (userId) => http.post(`/api/v1/users/${userId}/follow`);

export const unfollowUser = (userId) => http.delete(`/api/v1/users/${userId}/follow`);

export const changePassword = (data) => http.put('/api/v1/user/password', data);

export const changeEmail = (data) => http.put('/api/v1/user/email', data);

export const getDevices = () => http.get('/api/v1/user/devices');

export const removeDevice = (deviceId) => http.delete(`/api/v1/user/devices/${deviceId}`);
