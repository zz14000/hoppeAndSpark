import { http } from './request.js';

export const getSettings = () => http.get('/api/v1/settings');

export const updateSettings = (data) => http.put('/api/v1/settings', data);

export const clearCache = (data) => http.post('/api/v1/settings/cache/clear', data);
