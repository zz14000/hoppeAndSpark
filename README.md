# Hope & Sparks

Hope & Sparks 前端项目，基于 Vue 3 + Vite，对接后端 REST API，覆盖学习资源、Agent 对话、知识库治理等能力。

## 快速开始

```bash
npm install
npm run dev
```

默认开发地址：`http://localhost:5173`

## 环境变量

复制 `.env.example` 为 `.env.development` 并按需修改：

| 变量 | 说明 |
|------|------|
| `VITE_API_PROXY_TARGET` | 后端地址，默认 `http://localhost:8080` |
| `VITE_USE_MOCK` | `true` 时使用本地 mock |
| `VITE_SKIP_AUTH` | `true` 时跳过登录校验 |

## API 层

统一请求封装在 `src/api/request.js`（Bearer Token、401 自动刷新）。

```js
import { login, getKbDocuments, subscribeAgentStream } from './src/api/index.js'
```

### 接口文档与覆盖率

| 文档 | 说明 |
|------|------|
| [docs/API_COVERAGE.md](docs/API_COVERAGE.md) | 前端已实现接口汇总表（含路径、模块文件） |
| [docs/API_COVERAGE.json](docs/API_COVERAGE.json) | 机器可读覆盖率数据 |
| `Hope and Sparks API.openapi.json` | 主业务 OpenAPI |
| `apifox-kb-agent-openapi.json` | 知识库 + Agent 补充 OpenAPI |

重新生成覆盖率报告：

```bash
npm run api:coverage
```

### 模块说明

| 文件 | 职责 |
|------|------|
| `src/api/auth.js` | 注册、登录、密码找回 |
| `src/api/agent.js` | Agent 会话、消息、SSE 流式、`ai-disputes` |
| `src/api/kb.js` | 知识库文档、入库任务、候选治理、仪表盘、评估、Agent Run 运维 |
| `src/api/manage.js` | 管理端（用户、审核、资源等） |
| `src/api/upload.js` | 上传凭证与完成登记 |

## Agent SSE 流式示例

```js
import { sendAgentMessage, subscribeAgentStream } from '@/api'

const sendRes = await sendAgentMessage(sessionId, { content: '你好', stream: true })
const messageId = sendRes.data?.messageId

await subscribeAgentStream(sessionId, messageId, {
  onEvent: ({ type, data }) => console.log(type, data),
  onError: (err) => console.error(err),
  onDone: () => console.log('stream done'),
})
```

## 构建

```bash
npm run build
npm run preview
```

## 相关路由

- `/login` — 登录
- `/forgot-password` — 找回密码
- `/reset-password` — 重置密码（邮件 token）
- `/app/dashboard` — 工作台（需登录）
# Hope & Sparks 前端 API 接口实现汇总

> 依据主规范 + KB/Agent 补充规范自动生成，最后更新：2026-06-11
>
> - `Hope and Sparks API.openapi.json`
> - `apifox-kb-agent-openapi.json`

## 覆盖率概览

| 指标 | 数值 |
|------|------|
| OpenAPI 接口总数 | 144 |
| 已实现 | 144 |
| 未实现 | 0 |
| 覆盖率 | **100.0%** |

重新生成：

```bash
npm run api:coverage
```

## 前端模块索引

| 模块文件 | 接口数 | 说明 |
|----------|--------|------|
| `src/api/agent.js` | 6 | 智能体会话与争议举报 |
| `src/api/article.js` | 11 | 社区文章与评论 |
| `src/api/auth.js` | 6 | 用户注册、登录、Token、密码找回 |
| `src/api/chat.js` | 7 | 私信、群聊、好友申请 |
| `src/api/exercise.js` | 9 | 练习与测试 |
| `src/api/kb.js` | 31 | 知识库文档、入库治理、评估与 Agent Ops |
| `src/api/learning.js` | 8 | 学习计划、拓扑、技能树 |
| `src/api/manage.js` | 16 | 管理端后台（用户、审核、资源等） |
| `src/api/onboarding.js` | 4 | Spark 画像引导与重建 |
| `src/api/resource.js` | 15 | 学习资源、视频、文档 |
| `src/api/settings.js` | 3 | 用户设置与缓存 |
| `src/api/system.js` | 13 | Nebula 探索、日历、通知 |
| `src/api/upload.js` | 2 | 文件上传 |
| `src/api/user.js` | 13 | 用户资料、收藏、关注、设备与安全 |

## 调用方式

```js
import { login, getArticleList, manageLogin } from '@/api'
// 或
import { forgotPassword } from '@/api/auth.js'
```

统一从 `src/api/index.js` 导出。请求层见 `src/api/request.js`，开发代理见 `vite.config.js`。

## 完整接口清单

### Agent

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | POST | `/api/v1/agent-sessions` | 创建 Agent 会话 | `src/api/agent.js` |
| ✅ 已实现 | GET | `/api/v1/agent-sessions/{sessionId}/messages` | 获取会话消息分页列表 | `src/api/agent.js` |
| ✅ 已实现 | POST | `/api/v1/agent-sessions/{sessionId}/messages` | 发送消息并触发 Agent 编排 | `src/api/agent.js` |
| ✅ 已实现 | GET | `/api/v1/agent-sessions/{sessionId}/stream` | 按 messageId 获取 SSE 流式事件 | `src/api/agent.js` |
| ✅ 已实现 | GET | `/api/v1/agents` | 获取可用 Agent 列表 | `src/api/agent.js` |
| ✅ 已实现 | POST | `/api/v1/ai-disputes` | 提交 AI 内容争议 | `src/api/agent.js` |

### 社区与文章

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/articles` | 获取文章列表 | `src/api/article.js` |
| ✅ 已实现 | POST | `/api/v1/articles` | 发布文章 | `src/api/article.js` |
| ✅ 已实现 | GET | `/api/v1/articles/{articleId}` | 获取文章详情 | `src/api/article.js` |
| ✅ 已实现 | DELETE | `/api/v1/articles/{articleId}/collect` | 取消收藏文章 | `src/api/article.js` |
| ✅ 已实现 | POST | `/api/v1/articles/{articleId}/collect` | 收藏文章 | `src/api/article.js` |
| ✅ 已实现 | GET | `/api/v1/articles/{articleId}/comments` | 获取评论 | `src/api/article.js` |
| ✅ 已实现 | POST | `/api/v1/articles/{articleId}/comments` | 发布评论 | `src/api/article.js` |
| ✅ 已实现 | DELETE | `/api/v1/articles/{articleId}/like` | 取消点赞文章 | `src/api/article.js` |
| ✅ 已实现 | POST | `/api/v1/articles/{articleId}/like` | 点赞文章 | `src/api/article.js` |
| ✅ 已实现 | POST | `/api/v1/articles/drafts` | 保存草稿 | `src/api/article.js` |
| ✅ 已实现 | POST | `/api/v1/articles/polish` | Horizon 文章润色 | `src/api/article.js` |
| ✅ 已实现 | DELETE | `/api/v1/users/{userId}/follow` | 取消关注用户 | `src/api/user.js` |
| ✅ 已实现 | POST | `/api/v1/users/{userId}/follow` | 关注用户 | `src/api/user.js` |

### 用户与认证

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | POST | `/api/v1/auth/login` | 登录 | `src/api/auth.js` |
| ✅ 已实现 | POST | `/api/v1/auth/logout` | 退出登录 | `src/api/auth.js` |
| ✅ 已实现 | POST | `/api/v1/auth/password/reset-confirm` | 重置密码 | `src/api/auth.js` |
| ✅ 已实现 | POST | `/api/v1/auth/password/reset-request` | 找回密码 | `src/api/auth.js` |
| ✅ 已实现 | POST | `/api/v1/auth/refresh` | 刷新 token | `src/api/auth.js` |
| ✅ 已实现 | POST | `/api/v1/auth/register` | 注册 | `src/api/auth.js` |

### 学习计划日历与拓扑

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/calendar/events` | 获取日历事件 | `src/api/system.js` |
| ✅ 已实现 | POST | `/api/v1/calendar/events` | 新建日程 | `src/api/system.js` |
| ✅ 已实现 | DELETE | `/api/v1/calendar/events/{eventId}` | 删除日程 | `src/api/system.js` |
| ✅ 已实现 | PUT | `/api/v1/calendar/events/{eventId}` | 更新日程 | `src/api/system.js` |
| ✅ 已实现 | PUT | `/api/v1/learning-plans/{planId}/adjust` | 调整学习计划 | `src/api/learning.js` |
| ✅ 已实现 | GET | `/api/v1/learning-plans/{planId}/topology` | 获取学习拓扑 | `src/api/learning.js` |
| ✅ 已实现 | GET | `/api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resource-network` | 获取知识点资源网络图 | `src/api/learning.js` |
| ✅ 已实现 | GET | `/api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resources` | 获取知识点资源网络图列表视图 | `src/api/learning.js` |
| ✅ 已实现 | GET | `/api/v1/learning-plans/current` | 获取学习计划 | `src/api/learning.js` |
| ✅ 已实现 | POST | `/api/v1/learning-plans/generate` | Strict 生成学习计划 | `src/api/learning.js` |

### 消息私信与群聊

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/chats` | 获取会话列表 | `src/api/chat.js` |
| ✅ 已实现 | GET | `/api/v1/chats/{chatId}/messages` | 获取聊天记录 | `src/api/chat.js` |
| ✅ 已实现 | POST | `/api/v1/chats/{chatId}/messages` | 发送消息 | `src/api/chat.js` |
| ✅ 已实现 | POST | `/api/v1/chats/groups` | 创建群聊 | `src/api/chat.js` |
| ✅ 已实现 | POST | `/api/v1/chats/private` | 创建私聊 | `src/api/chat.js` |
| ✅ 已实现 | POST | `/api/v1/friend-requests` | 发起好友申请 | `src/api/chat.js` |
| ✅ 已实现 | PUT | `/api/v1/friend-requests/{requestId}` | 处理好友申请 | `src/api/chat.js` |

### 学习资源库

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/code-cases/{caseId}` | 获取代码案例详情 | `src/api/resource.js` |
| ✅ 已实现 | GET | `/api/v1/readings/{readingId}` | 获取拓展阅读详情 | `src/api/resource.js` |
| ✅ 已实现 | GET | `/api/v1/resources` | 获取资源列表 | `src/api/resource.js` |
| ✅ 已实现 | GET | `/api/v1/resources/{resourceId}` | 获取资源详情 | `src/api/resource.js` |
| ✅ 已实现 | POST | `/api/v1/resources/{resourceId}/export` | 导出资源 | `src/api/resource.js` |
| ✅ 已实现 | POST | `/api/v1/resources/{resourceId}/feedback` | 对资源进行质量反馈 | `src/api/resource.js` |
| ✅ 已实现 | PUT | `/api/v1/resources/{resourceId}/progress` | 更新资源学习进度 | `src/api/resource.js` |

### 文档阅读

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/documents/{documentId}` | 获取文档详情 | `src/api/resource.js` |
| ✅ 已实现 | POST | `/api/v1/documents/{documentId}/ask` | Sage 伴读提问 | `src/api/resource.js` |
| ✅ 已实现 | GET | `/api/v1/documents/{documentId}/outline` | 获取文档目录 | `src/api/resource.js` |
| ✅ 已实现 | PUT | `/api/v1/documents/{documentId}/reading-progress` | 更新阅读进度 | `src/api/resource.js` |

### 练习与测试

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/exercise-sets` | 获取练习/测试列表 | `src/api/exercise.js` |
| ✅ 已实现 | GET | `/api/v1/exercise-sets/{exerciseSetId}` | 获取练习/测试详情 | `src/api/exercise.js` |
| ✅ 已实现 | GET | `/api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}` | 获取单题答题详情 | `src/api/exercise.js` |
| ✅ 已实现 | PUT | `/api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/answer` | 保存单题答案 | `src/api/exercise.js` |
| ✅ 已实现 | POST | `/api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/code-run` | 提交代码题即时评测 | `src/api/exercise.js` |
| ✅ 已实现 | PUT | `/api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/flag` | 标记或取消标记题目 | `src/api/exercise.js` |
| ✅ 已实现 | POST | `/api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/hint` | 请求 Coach 提示 | `src/api/exercise.js` |
| ✅ 已实现 | POST | `/api/v1/exercise-sets/{exerciseSetId}/submit` | 提交练习/测试 | `src/api/exercise.js` |
| ✅ 已实现 | GET | `/api/v1/exercise-sets/attempts/{attemptId}/report` | 获取练习/测试报告 | `src/api/exercise.js` |

### Nebula探索与资源生成

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | POST | `/api/v1/explore` | 调用 Nebula 全局探索 | `src/api/system.js` |
| ✅ 已实现 | GET | `/api/v1/explore/{exploreId}` | 获取探索详情 | `src/api/system.js` |
| ✅ 已实现 | POST | `/api/v1/explore/{exploreId}/mindmap` | 生成思维导图 | `src/api/system.js` |
| ✅ 已实现 | GET | `/api/v1/knowledge-graph` | 获取知识关联图谱 | `src/api/system.js` |

### Manage管理端

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/manage/agent-prompts` | Prompt 配置列表 | `src/api/manage.js` |
| ✅ 已实现 | PUT | `/api/v1/manage/agent-prompts/{promptId}` | 更新 Prompt 配置 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/ai-disputes` | 争议内容工单列表 | `src/api/manage.js` |
| ✅ 已实现 | PUT | `/api/v1/manage/ai-disputes/{disputeId}` | 处理争议内容 | `src/api/manage.js` |
| ✅ 已实现 | POST | `/api/v1/manage/auth/login` | 管理端登录 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/dashboard/overview` | 数据看板 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/moderation/behavior-alerts` | 用户行为风控预警 | `src/api/manage.js` |
| ✅ 已实现 | PUT | `/api/v1/manage/moderation/behavior-alerts/{alertId}` | 处理行为预警 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/moderation/content` | 内容 AI 审核列表 | `src/api/manage.js` |
| ✅ 已实现 | PUT | `/api/v1/manage/moderation/content/{recordId}` | 处理内容审核结果 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/resources` | 管理历史生成资源 | `src/api/manage.js` |
| ✅ 已实现 | DELETE | `/api/v1/manage/resources/{resourceId}` | 删除历史生成资源 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/storage/overview` | 资源存储监控 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/users` | 用户管理列表 | `src/api/manage.js` |
| ✅ 已实现 | POST | `/api/v1/manage/users/{userId}/actions` | 用户封禁、解封与警告 | `src/api/manage.js` |
| ✅ 已实现 | GET | `/api/v1/manage/users/{userId}/learning-trace` | 查看用户学习轨迹 | `src/api/manage.js` |

### Agent Ops

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/agent-runs` | 分页查询 Agent 运行记录 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/agent-runs/{runId}` | 获取单个 Agent Run | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/agent-runs/{runId}/checkpoints` | 分页查询 Agent Run Checkpoint | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/agent-runs/{runId}/events` | 分页查询 Agent Run 事件 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/agent-runs/{runId}/replay` | 重放 Agent Run | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/agent-runs/{runId}/resume` | 从最近或指定 Checkpoint 恢复 Agent Run | `src/api/kb.js` |

### KB Governance

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/candidates` | 分页查询候选治理记录 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/candidates/{candidateId}` | 获取候选治理详情 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/candidates/{candidateId}/approve` | 人工批准候选内容 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/candidates/{candidateId}/reject` | 驳回候选内容 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/candidates/{candidateId}/replay` | 重放候选治理链路 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/candidates/{candidateId}/rollback` | 回滚已晋升候选内容 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/dashboard/candidates` | 候选治理指标分页 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/dashboard/failures` | 查询失败事件分页 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/dashboard/overview` | 知识库总览指标 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/dashboard/quality` | 知识库质量指标 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/dashboard/stages` | 知识库阶段指标 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/evaluations/runs` | 分页查询评估运行 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/evaluations/runs` | 创建离线评估运行 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/evaluations/runs/{runId}` | 获取单个评估运行 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/ingest-jobs` | 分页查询入库任务 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/ingest-jobs` | 创建 KB 异步入库任务 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/ingest-jobs/{taskId}` | 获取单个入库任务 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/ingest-jobs/{taskId}/retry` | 重试入库任务 | `src/api/kb.js` |

### KB Documents

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/documents` | 分页查询知识库文档 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/documents` | 创建知识库文档 | `src/api/kb.js` |
| ✅ 已实现 | DELETE | `/api/v1/manage/knowledge-base/documents/{documentId}` | 删除知识库文档 | `src/api/kb.js` |
| ✅ 已实现 | PUT | `/api/v1/manage/knowledge-base/documents/{documentId}` | 更新知识库文档 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/documents/{documentId}/chunks` | 获取文档切块分页列表 | `src/api/kb.js` |
| ✅ 已实现 | GET | `/api/v1/manage/knowledge-base/documents/{documentId}/parse-status` | 获取文档解析状态 | `src/api/kb.js` |
| ✅ 已实现 | POST | `/api/v1/manage/knowledge-base/documents/{documentId}/reparse` | 触发文档重新解析 | `src/api/kb.js` |

### 通知

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/notifications` | 获取通知列表 | `src/api/system.js` |
| ✅ 已实现 | DELETE | `/api/v1/notifications/{notificationId}` | 删除通知 | `src/api/system.js` |
| ✅ 已实现 | POST | `/api/v1/notifications/{notificationId}/actions` | 通知动作回调 | `src/api/system.js` |
| ✅ 已实现 | PUT | `/api/v1/notifications/{notificationId}/read` | 标记通知已读 | `src/api/system.js` |
| ✅ 已实现 | PUT | `/api/v1/notifications/read-all` | 全部通知已读 | `src/api/system.js` |

### Spark画像与首次引导

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | POST | `/api/v1/onboarding/answers` | 提交单轮画像回答 | `src/api/onboarding.js` |
| ✅ 已实现 | POST | `/api/v1/onboarding/complete` | 生成 Spark 画像 | `src/api/onboarding.js` |
| ✅ 已实现 | GET | `/api/v1/onboarding/questions` | 获取画像引导问题 | `src/api/onboarding.js` |
| ✅ 已实现 | POST | `/api/v1/spark-profile/rebuild` | 重新构建画像 | `src/api/onboarding.js` |

### 设置

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/settings` | 获取设置 | `src/api/settings.js` |
| ✅ 已实现 | PUT | `/api/v1/settings` | 更新设置 | `src/api/settings.js` |
| ✅ 已实现 | POST | `/api/v1/settings/cache/clear` | 清理缓存 | `src/api/settings.js` |

### 技能树

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/skill-tree` | 获取技能树 | `src/api/learning.js` |
| ✅ 已实现 | POST | `/api/v1/skill-tree/nodes/{nodeId}/light-up` | 点亮技能节点 | `src/api/learning.js` |

### Upload

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | POST | `/api/v1/uploads/complete` | 完成上传并登记文件 | `src/api/upload.js` |
| ✅ 已实现 | POST | `/api/v1/uploads/token` | 创建上传凭证 | `src/api/upload.js` |

### 用户资料与个人中心

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/user/collections` | 获取我的收藏 | `src/api/user.js` |
| ✅ 已实现 | POST | `/api/v1/user/collections` | 收藏或取消收藏 | `src/api/user.js` |
| ✅ 已实现 | DELETE | `/api/v1/user/collections/{collectionId}` | 删除收藏 | `src/api/user.js` |
| ✅ 已实现 | GET | `/api/v1/user/current` | 获取当前用户 | `src/api/user.js` |
| ✅ 已实现 | GET | `/api/v1/user/learning-stats` | 获取学习数据统计 | `src/api/user.js` |
| ✅ 已实现 | PUT | `/api/v1/user/profile` | 更新个人资料 | `src/api/user.js` |
| ✅ 已实现 | GET | `/api/v1/users/{userId}` | 获取指定用户主页 | `src/api/user.js` |

### 设备与安全

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/user/devices` | 获取登录设备 | `src/api/user.js` |
| ✅ 已实现 | DELETE | `/api/v1/user/devices/{deviceId}` | 下线指定设备 | `src/api/user.js` |
| ✅ 已实现 | PUT | `/api/v1/user/email` | 绑定或更换邮箱 | `src/api/user.js` |
| ✅ 已实现 | PUT | `/api/v1/user/password` | 修改密码 | `src/api/user.js` |

### 视频学习

| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |
|------|------|----------|------|----------|
| ✅ 已实现 | GET | `/api/v1/videos/{videoId}` | 获取视频详情 | `src/api/resource.js` |
| ✅ 已实现 | GET | `/api/v1/videos/{videoId}/episodes` | 获取视频选集 | `src/api/resource.js` |
| ✅ 已实现 | GET | `/api/v1/videos/{videoId}/transcripts` | 获取视频字幕与 AI 提示 | `src/api/resource.js` |
| ✅ 已实现 | PUT | `/api/v1/videos/{videoId}/watch-progress` | 上报播放进度 | `src/api/resource.js` |

---

相关文件：

- 主 OpenAPI：`Hope and Sparks API.openapi.json`
- KB/Agent OpenAPI：`apifox-kb-agent-openapi.json`
- JSON 机器可读：`docs/API_COVERAGE.json`
