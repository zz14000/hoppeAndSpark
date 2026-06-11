import { http } from './request.js';

// —— KB Documents ——

/** 分页查询知识库文档 */
export const getKbDocuments = (params) =>
  http.get('/api/v1/manage/knowledge-base/documents', params);

/** 创建知识库文档 */
export const createKbDocument = (data) =>
  http.post('/api/v1/manage/knowledge-base/documents', data);

/** 更新知识库文档 */
export const updateKbDocument = (documentId, data) =>
  http.put(`/api/v1/manage/knowledge-base/documents/${documentId}`, data);

/** 删除知识库文档 */
export const deleteKbDocument = (documentId) =>
  http.delete(`/api/v1/manage/knowledge-base/documents/${documentId}`);

/** 触发文档重新解析 */
export const reparseKbDocument = (documentId, data) =>
  http.post(`/api/v1/manage/knowledge-base/documents/${documentId}/reparse`, data);

/** 获取文档解析状态 */
export const getKbParseStatus = (documentId) =>
  http.get(`/api/v1/manage/knowledge-base/documents/${documentId}/parse-status`);

/** 获取文档切块分页列表 */
export const getKbDocumentChunks = (documentId, params) =>
  http.get(`/api/v1/manage/knowledge-base/documents/${documentId}/chunks`, params);

// —— KB Ingest Jobs ——

/** 创建 KB 异步入库任务 */
export const createKbIngestJob = (data) =>
  http.post('/api/v1/manage/knowledge-base/ingest-jobs', data);

/** 分页查询入库任务 */
export const getKbIngestJobs = (params) =>
  http.get('/api/v1/manage/knowledge-base/ingest-jobs', params);

/** 获取单个入库任务 */
export const getKbIngestJob = (taskId) =>
  http.get(`/api/v1/manage/knowledge-base/ingest-jobs/${taskId}`);

/** 重试入库任务 */
export const retryKbIngestJob = (taskId, data) =>
  http.post(`/api/v1/manage/knowledge-base/ingest-jobs/${taskId}/retry`, data);

// —— KB Candidates Governance ——

/** 分页查询候选治理记录 */
export const getKbCandidates = (params) =>
  http.get('/api/v1/manage/knowledge-base/candidates', params);

/** 获取候选治理详情 */
export const getKbCandidate = (candidateId) =>
  http.get(`/api/v1/manage/knowledge-base/candidates/${candidateId}`);

/** 人工批准候选内容 */
export const approveKbCandidate = (candidateId, data) =>
  http.post(`/api/v1/manage/knowledge-base/candidates/${candidateId}/approve`, data);

/** 驳回候选内容 */
export const rejectKbCandidate = (candidateId, data) =>
  http.post(`/api/v1/manage/knowledge-base/candidates/${candidateId}/reject`, data);

/** 回滚已晋升候选内容 */
export const rollbackKbCandidate = (candidateId, data) =>
  http.post(`/api/v1/manage/knowledge-base/candidates/${candidateId}/rollback`, data);

/** 重放候选治理链路 */
export const replayKbCandidate = (candidateId, data) =>
  http.post(`/api/v1/manage/knowledge-base/candidates/${candidateId}/replay`, data);

// —— KB Dashboard ——

/** 知识库总览指标 */
export const getKbDashboardOverview = (params) =>
  http.get('/api/v1/manage/knowledge-base/dashboard/overview', params);

/** 知识库阶段指标 */
export const getKbDashboardStages = (params) =>
  http.get('/api/v1/manage/knowledge-base/dashboard/stages', params);

/** 查询失败事件分页 */
export const getKbDashboardFailures = (params) =>
  http.get('/api/v1/manage/knowledge-base/dashboard/failures', params);

/** 候选治理指标分页 */
export const getKbDashboardCandidates = (params) =>
  http.get('/api/v1/manage/knowledge-base/dashboard/candidates', params);

/** 知识库质量指标 */
export const getKbDashboardQuality = (params) =>
  http.get('/api/v1/manage/knowledge-base/dashboard/quality', params);

// —— KB Evaluations ——

/** 创建离线评估运行 */
export const createKbEvaluationRun = (data) =>
  http.post('/api/v1/manage/knowledge-base/evaluations/runs', data);

/** 分页查询评估运行 */
export const getKbEvaluationRuns = (params) =>
  http.get('/api/v1/manage/knowledge-base/evaluations/runs', params);

/** 获取单个评估运行 */
export const getKbEvaluationRun = (runId) =>
  http.get(`/api/v1/manage/knowledge-base/evaluations/runs/${runId}`);

// —— Agent Ops (KB 管理域) ——

/** 分页查询 Agent 运行记录 */
export const getKbAgentRuns = (params) =>
  http.get('/api/v1/manage/knowledge-base/agent-runs', params);

/** 获取单个 Agent Run */
export const getKbAgentRun = (runId) =>
  http.get(`/api/v1/manage/knowledge-base/agent-runs/${runId}`);

/** 分页查询 Agent Run 事件 */
export const getKbAgentRunEvents = (runId, params) =>
  http.get(`/api/v1/manage/knowledge-base/agent-runs/${runId}/events`, params);

/** 分页查询 Agent Run Checkpoint */
export const getKbAgentRunCheckpoints = (runId, params) =>
  http.get(`/api/v1/manage/knowledge-base/agent-runs/${runId}/checkpoints`, params);

/** 从 Checkpoint 恢复 Agent Run */
export const resumeKbAgentRun = (runId, data) =>
  http.post(`/api/v1/manage/knowledge-base/agent-runs/${runId}/resume`, data);

/** 重放 Agent Run */
export const replayKbAgentRun = (runId, data) =>
  http.post(`/api/v1/manage/knowledge-base/agent-runs/${runId}/replay`, data);
