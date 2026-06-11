import { http } from './request.js';

export const getUploadToken = (data) => http.post('/api/v1/uploads/token', data);

export const completeUpload = (data) => http.post('/api/v1/uploads/complete', data);
