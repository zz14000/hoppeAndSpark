import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '../utils/auth.js';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

let refreshPromise = null;

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

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return { code: response.status, message: response.statusText, data: null };
  try {
    return JSON.parse(text);
  } catch {
    throw new Error(`响应解析失败: ${text.slice(0, 200)}`);
  }
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  const url = buildUrl('/api/v1/auth/refresh');
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    clearTokens();
    return false;
  }

  const result = await parseResponse(response);
  if (result.code === 200 && result.data?.accessToken) {
    setTokens({
      accessToken: result.data.accessToken,
      refreshToken: result.data.refreshToken || refreshToken,
    });
    return true;
  }

  clearTokens();
  return false;
}

async function request(method, path, { params, body, headers = {}, auth = true, retry = true } = {}) {
  const requestHeaders = {
    'Content-Type': 'application/json',
    ...headers,
  };

  if (auth) {
    const token = getAccessToken();
    if (token) requestHeaders.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(buildUrl(path, params), {
    method,
    headers: requestHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401 && auth && retry) {
    if (!refreshPromise) {
      refreshPromise = refreshAccessToken().finally(() => {
        refreshPromise = null;
      });
    }
    const refreshed = await refreshPromise;
    if (refreshed) return request(method, path, { params, body, headers, auth, retry: false });
    clearTokens();
    if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
      window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
    }
  }

  const result = await parseResponse(response);

  if (!response.ok && result.code === undefined) {
    throw new Error(result.message || `HTTP ${response.status}`);
  }

  return result;
}

export const http = {
  get: (path, params, options) => request('GET', path, { params, ...options }),
  post: (path, body, options) => request('POST', path, { body, ...options }),
  put: (path, body, options) => request('PUT', path, { body, ...options }),
  delete: (path, options) => request('DELETE', path, options),
};

export { USE_MOCK };
