import { http, USE_MOCK } from './request.js';
import { setTokens, clearTokens } from '../utils/auth.js';
import { delay } from '../mock/index.js';

export const register = async (data) => {
  if (USE_MOCK) {
    await delay(500);
    return { code: 200, message: '注册成功', data: { userId: 'u_mock' } };
  }
  return http.post('/api/v1/auth/register', data, { auth: false });
};

export const login = async (data) => {
  if (USE_MOCK) {
    await delay(500);
    const tokens = { accessToken: 'mock_access_token', refreshToken: 'mock_refresh_token', expiresIn: 7200 };
    setTokens(tokens);
    return { code: 200, message: 'success', data: tokens };
  }
  const res = await http.post('/api/v1/auth/login', { clientType: 'web', ...data }, { auth: false });
  if (res.code === 200 && res.data) {
    setTokens(res.data);
  }
  return res;
};

export const refreshToken = async (token) => {
  if (USE_MOCK) {
    await delay(200);
    return { code: 200, message: 'success', data: { accessToken: 'mock_access_token', refreshToken: token } };
  }
  return http.post('/api/v1/auth/refresh', { refreshToken: token }, { auth: false });
};

export const logout = async () => {
  if (USE_MOCK) {
    await delay(200);
    clearTokens();
    return { code: 200, message: '已退出' };
  }
  try {
    await http.post('/api/v1/auth/logout');
  } finally {
    clearTokens();
  }
  return { code: 200, message: '已退出' };
};

/** 找回密码 - POST /api/v1/auth/password/reset-request */
export const requestPasswordReset = (email) =>
  USE_MOCK
    ? delay(300).then(() => ({ code: 200, message: '重置邮件已发送' }))
    : http.post('/api/v1/auth/password/reset-request', { email }, { auth: false });

/** 重置密码 - POST /api/v1/auth/password/reset-confirm */
export const confirmPasswordReset = (data) =>
  USE_MOCK
    ? delay(300).then(() => ({ code: 200, message: '密码已重置' }))
    : http.post('/api/v1/auth/password/reset-confirm', data, { auth: false });

export const forgotPassword = requestPasswordReset;
export const resetPassword = confirmPasswordReset;
