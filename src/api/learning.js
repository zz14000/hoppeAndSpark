import { http } from './request.js';

export const getCurrentLearningPlan = () => http.get('/api/v1/learning-plans/current');

export const generateLearningPlan = (data) => http.post('/api/v1/learning-plans/generate', data);

export const adjustLearningPlan = (planId, data) =>
  http.put(`/api/v1/learning-plans/${planId}/adjust`, data);

export const getPlanTopology = (planId) => http.get(`/api/v1/learning-plans/${planId}/topology`);

export const getNodeResourceNetwork = (planId, nodeId) =>
  http.get(`/api/v1/learning-plans/${planId}/topology/nodes/${nodeId}/resource-network`);

export const getNodeResources = (planId, nodeId, params) =>
  http.get(`/api/v1/learning-plans/${planId}/topology/nodes/${nodeId}/resources`, params);

export const getSkillTree = () => http.get('/api/v1/skill-tree');

export const lightUpSkillNode = (nodeId, data) =>
  http.post(`/api/v1/skill-tree/nodes/${nodeId}/light-up`, data);
