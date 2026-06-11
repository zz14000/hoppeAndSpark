import { http, USE_MOCK } from './request.js';
import { MOCK_ARTICLES, delay } from '../mock/index.js';

function normalizeArticle(item) {
  if (!item) return item;
  const author =
    typeof item.author === 'object'
      ? item.author?.nickname || item.author?.name || '匿名'
      : item.author || item.authorName || '匿名';

  return {
    ...item,
    author,
    excerpt: item.excerpt || item.summary || '',
    time: item.time || item.publishedAt || item.createdAt || '',
    readCount: item.readCount ?? item.viewCount ?? 0,
    aiSummary: item.aiSummary ?? item.summary ?? false,
    tags: item.tags || [],
    likes: item.likes ?? item.likeCount ?? 0,
    comments: item.comments ?? item.commentCount ?? 0,
    collects: item.collects ?? item.collectCount ?? 0,
  };
}

function normalizePageResult(res, mapper) {
  if (res.code !== 200 || !res.data) return res;
  const list = (res.data.list || []).map(mapper);
  return { ...res, data: { ...res.data, list } };
}

export const getArticleList = async (params = {}) => {
  if (USE_MOCK) {
    await delay(500);
    return {
      code: 200,
      message: 'success',
      data: { total: MOCK_ARTICLES.length, list: MOCK_ARTICLES },
    };
  }
  const query = {
    page: params.page ?? 1,
    pageSize: params.pageSize ?? params.limit ?? 10,
    category: params.category,
    keyword: params.keyword,
  };
  const res = await http.get('/api/v1/articles', query);
  return normalizePageResult(res, normalizeArticle);
};

export const getArticleDetail = async (articleId) => {
  if (USE_MOCK) {
    await delay(400);
    const article = MOCK_ARTICLES.find((a) => a.id == articleId) || MOCK_ARTICLES[0];
    return { code: 200, message: 'success', data: normalizeArticle(article) };
  }
  const res = await http.get(`/api/v1/articles/${articleId}`);
  if (res.code === 200 && res.data) {
    return { ...res, data: normalizeArticle(res.data) };
  }
  return res;
};

export const publishArticle = async (articleData) => {
  if (USE_MOCK) {
    await delay(800);
    return { code: 200, message: '发布成功', data: { id: Date.now(), ...articleData } };
  }
  return http.post('/api/v1/articles', articleData);
};

export const saveDraft = (data) => http.post('/api/v1/articles/drafts', data);

export const polishArticle = (data) => http.post('/api/v1/articles/polish', data);

export const likeArticle = (articleId) => http.post(`/api/v1/articles/${articleId}/like`);

export const unlikeArticle = (articleId) => http.delete(`/api/v1/articles/${articleId}/like`);

export const collectArticle = (articleId) => http.post(`/api/v1/articles/${articleId}/collect`);

export const uncollectArticle = (articleId) => http.delete(`/api/v1/articles/${articleId}/collect`);

export const getArticleComments = (articleId, params) =>
  http.get(`/api/v1/articles/${articleId}/comments`, params);

export const postArticleComment = (articleId, data) =>
  http.post(`/api/v1/articles/${articleId}/comments`, data);
