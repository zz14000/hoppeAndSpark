import { http } from './request.js';

/** 获取练习/测试列表 */
export const getExerciseSets = (params) => http.get('/api/v1/exercise-sets', params);

/** 获取练习/测试详情 */
export const getExerciseSet = (exerciseSetId) => http.get(`/api/v1/exercise-sets/${exerciseSetId}`);

/** 获取单题答题详情 */
export const getQuestion = (exerciseSetId, questionId) =>
  http.get(`/api/v1/exercise-sets/${exerciseSetId}/questions/${questionId}`);

/** 保存单题答案 */
export const submitAnswer = (exerciseSetId, questionId, data) =>
  http.put(`/api/v1/exercise-sets/${exerciseSetId}/questions/${questionId}/answer`, data);

/** 标记或取消标记题目 */
export const flagQuestion = (exerciseSetId, questionId, data) =>
  http.put(`/api/v1/exercise-sets/${exerciseSetId}/questions/${questionId}/flag`, data);

/** 请求 Coach 提示 */
export const getQuestionHint = (exerciseSetId, questionId, data = {}) =>
  http.post(`/api/v1/exercise-sets/${exerciseSetId}/questions/${questionId}/hint`, data);

/** 提交练习/测试 */
export const submitExerciseSet = (exerciseSetId, data) =>
  http.post(`/api/v1/exercise-sets/${exerciseSetId}/submit`, data);

/** 提交代码题即时评测 */
export const runCode = (exerciseSetId, questionId, data) =>
  http.post(`/api/v1/exercise-sets/${exerciseSetId}/questions/${questionId}/code-run`, data);

/** 获取练习/测试报告 */
export const getAttemptReport = (attemptId) =>
  http.get(`/api/v1/exercise-sets/attempts/${attemptId}/report`);
