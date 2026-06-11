import { http } from './request.js';
import { setTokens } from '../utils/auth.js';

/** 管理端登录 */
export const manageLogin = async (data) => {
  const res = await http.post('/api/v1/manage/auth/login', data, { auth: false });
  if (res.code === 200 && res.data?.accessToken) {
    setTokens(res.data);
  }
  return res;
};

/** 数据看板 */
export const getManageDashboard = (params) => http.get('/api/v1/manage/dashboard/overview', params);

/** 用户管理列表 */
export const getManageUsers = (params) => http.get('/api/v1/manage/users', params);

/** 用户封禁、解封与警告 */
export const manageUserAction = (userId, data) =>
  http.post(`/api/v1/manage/users/${userId}/actions`, data);

/** 查看用户学习轨迹 */
export const getManageUserLearningTrace = (userId) =>
  http.get(`/api/v1/manage/users/${userId}/learning-trace`);

/** @deprecated 请使用 kb.js 中的 getKbDocuments */
export {
  getKbDocuments as getManageKbDocuments,
  createKbDocument as uploadManageKbDocument,
  updateKbDocument as updateManageKbDocument,
  deleteKbDocument as deleteManageKbDocument,
  getKbParseStatus as getManageKbParseStatus,
} from './kb.js';

/** 争议内容工单列表 */
export const getManageAiDisputes = (params) => http.get('/api/v1/manage/ai-disputes', params);

/** 处理争议内容 */
export const handleManageAiDispute = (disputeId, data) =>
  http.put(`/api/v1/manage/ai-disputes/${disputeId}`, data);

/** 资源存储监控 */
export const getManageStorageOverview = () => http.get('/api/v1/manage/storage/overview');

/** 管理历史生成资源 */
export const getManageResources = (params) => http.get('/api/v1/manage/resources', params);

/** 删除历史生成资源 */
export const deleteManageResource = (resourceId) =>
  http.delete(`/api/v1/manage/resources/${resourceId}`);

/** Prompt 配置列表 */
export const getManageAgentPrompts = () => http.get('/api/v1/manage/agent-prompts');

/** 更新 Prompt 配置 */
export const updateManageAgentPrompt = (promptId, data) =>
  http.put(`/api/v1/manage/agent-prompts/${promptId}`, data);

/** 内容 AI 审核列表 */
export const getManageModerationContent = (params) =>
  http.get('/api/v1/manage/moderation/content', params);

/** 处理内容审核结果 */
export const handleManageModerationContent = (recordId, data) =>
  http.put(`/api/v1/manage/moderation/content/${recordId}`, data);

/** 用户行为风控预警 */
export const getManageBehaviorAlerts = (params) =>
  http.get('/api/v1/manage/moderation/behavior-alerts', params);

/** 处理行为预警 */
export const handleManageBehaviorAlert = (alertId, data) =>
  http.put(`/api/v1/manage/moderation/behavior-alerts/${alertId}`, data);
