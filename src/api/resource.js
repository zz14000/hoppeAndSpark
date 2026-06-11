import { http } from './request.js';

export const getResources = (params) => http.get('/api/v1/resources', params);

export const getResourceDetail = (resourceId) => http.get(`/api/v1/resources/${resourceId}`);

export const updateResourceProgress = (resourceId, data) =>
  http.put(`/api/v1/resources/${resourceId}/progress`, data);

export const exportResource = (resourceId, data) =>
  http.post(`/api/v1/resources/${resourceId}/export`, data);

export const submitResourceFeedback = (resourceId, data) =>
  http.post(`/api/v1/resources/${resourceId}/feedback`, data);

export const getReading = (readingId) => http.get(`/api/v1/readings/${readingId}`);

export const getCodeCase = (caseId) => http.get(`/api/v1/code-cases/${caseId}`);

export const getVideo = (videoId) => http.get(`/api/v1/videos/${videoId}`);

export const getVideoEpisodes = (videoId) => http.get(`/api/v1/videos/${videoId}/episodes`);

export const updateVideoProgress = (videoId, data) =>
  http.put(`/api/v1/videos/${videoId}/watch-progress`, data);

export const getVideoTranscripts = (videoId) => http.get(`/api/v1/videos/${videoId}/transcripts`);

export const getDocument = (documentId) => http.get(`/api/v1/documents/${documentId}`);

export const getDocumentOutline = (documentId) =>
  http.get(`/api/v1/documents/${documentId}/outline`);

export const updateDocumentProgress = (documentId, data) =>
  http.put(`/api/v1/documents/${documentId}/reading-progress`, data);

export const askDocument = (documentId, data) =>
  http.post(`/api/v1/documents/${documentId}/ask`, data);
