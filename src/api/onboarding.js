import { http, USE_MOCK } from './request.js';
import { delay } from '../mock/index.js';

export const getOnboardingQuestions = async () => {
  if (USE_MOCK) {
    await delay(300);
    return { code: 200, message: 'success', data: { questions: [] } };
  }
  return http.get('/api/v1/onboarding/questions');
};

export const submitOnboardingAnswer = (data) => http.post('/api/v1/onboarding/answers', data);

export const completeOnboarding = async () => {
  if (USE_MOCK) {
    await delay(500);
    return { code: 200, message: '画像生成成功', data: {} };
  }
  return http.post('/api/v1/onboarding/complete');
};

export const rebuildSparkProfile = (data) => http.post('/api/v1/spark-profile/rebuild', data);
