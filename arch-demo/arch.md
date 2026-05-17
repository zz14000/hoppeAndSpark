# Hope and Sparks 后端架构草案

> 版本：v0.6  
> 状态：对话讨论稿  
> 资料来源：`资源/Hope and Sparks开发文档改.md`、`资源/Hope_and_Sparks_API接口文档.md`、`资源/apifox-openapi.yaml`、`资源/数据库设计-正式版.md`、`资源/hope_sparks.sql`

## 0. 已确认架构决策

1. 后端技术栈采用 Java 17 + Spring Boot 3.x + Maven + MySQL + Redis + Chroma。
2. ORM 优先采用 MyBatis-Plus；复杂 SQL 可补充 MyBatis XML。
3. 智能体相关调用直接使用 Coze，后端保留统一 Agent / LLM 适配层。
4. LLM 能力预留其他模型适配，不把业务代码绑定到单一模型供应商。
5. 文件存储使用 MinIO，业务层统一通过文件存储端口访问。
6. MVP 异步队列使用 RabbitMQ，不使用 Redis Stream 作为主任务队列。
7. MVP 包含社区文章模块，但 IM、商城、周挑战仍可后置。
8. Coze 组织方式：对话型能力使用独立 Bot，长流程与结构化产物使用 Workflow。
9. 流式策略：Agent 对话走 SSE 流式；资源生成、知识库解析、向量化走 RabbitMQ 异步任务 + 状态查询。
10. RabbitMQ 消费者 MVP 阶段放在同一个 Spring Boot 应用内，代码按 worker 边界组织，后续可拆独立 worker 服务。
11. 社区文章先落库为 `pending`，通过 RabbitMQ 异步审核，通过后变为 `published`。
12. 新增 `sys_agent_config` 表，保存 Coze Bot / Workflow 路由配置；`sys_agent_prompt` 只保存提示词与模型参数版本。
13. Coze Token、MinIO 密钥、RabbitMQ 密码、MySQL 密码等敏感配置统一放环境变量或部署配置，不入库。
14. Spark 前台和 Manage 后台 MVP 阶段共用一个 Spring Boot 应用和同一个 MySQL，通过路由、token 类型、RBAC 权限隔离。
15. 代码题 MVP 不引入真实沙箱，先做代码文本提交 + AI 评阅 + 评阅反馈。
16. 下一阶段先继续完善架构文档，再生成 Spring Boot 工程骨架。
17. 数据库迁移工具使用 Flyway，原始建表与架构增补 DDL 分版本管理。
18. 本地开发环境使用 Docker Compose 一键启动 MySQL、Redis、RabbitMQ、MinIO、Chroma。
19. Coze、MinIO、RabbitMQ 在开发初期提供 Mock Adapter，没有真实凭证时也能跑通主流程。
20. 架构文档确认到当前边界后，下一阶段开始生成 Spring Boot 工程骨架。
21. Java 根包名使用 `com.hopeandsparks`。
22. Flyway `V1` 使用讨论后的完整初始化 SQL，即更新后的 `资源/hope_sparks.sql`，不再使用原始未修订版本叠加 V2-V5 补丁。

## 1. 架构目标

Hope and Sparks 面向“个性化学习资源体系 + 多智能体协同学习系统”。后端架构需要优先支撑两个闭环：

1. Spark 前台学习闭环：用户画像 -> 个性化资源生成 -> 学习路径规划 -> 学习执行 -> 答题评测 -> 动态反馈。
2. Manage 后台治理闭环：知识库上传 -> 文档解析 -> 切片向量化 -> RAG 检索 -> 争议工单 -> 知识修正。

架构设计优先级：

1. 比赛 / 演示阶段：核心链路可运行、可联调、可演示。
2. 开发阶段：模块边界清晰，接口、数据库、Agent、异步任务能逐步落地。
3. 扩展阶段：社区、IM、商城、周挑战等能力可以作为二期模块接入。

## 2. 总体架构判断

### 2.1 推荐形态

推荐采用“模块化单体 + 清晰分层 + 异步任务管道”的后端架构，而不是一开始拆微服务。

原因：

1. 当前 `arch-demo` 是 Maven 单工程，比赛场景更重视完整闭环和交付速度。
2. API、数据库表、业务模块已经很多，过早微服务化会增加部署、联调和事务复杂度。
3. Agent 编排、RAG、OSS、异步生成天然适合通过端口适配器与任务队列隔离，将来可以平滑拆服务。

### 2.2 技术栈建议

| 层次 | 建议 |
| --- | --- |
| 后端框架 | Java 17 + Spring Boot 3.x |
| 构建工具 | Maven |
| API 文档 | OpenAPI / Apifox，接口以 `/api/v1` 为前缀 |
| 数据库 | MySQL，使用现有 `hope_sparks.sql` |
| 缓存 | Redis，缓存热点数据、登录态、短期上下文、进度、排行榜 |
| 异步队列 | RabbitMQ，承载资源生成、知识库解析、向量化、审核等异步任务 |
| 向量库 | Chroma，保存知识切片、个人笔记、视频字幕、多模态资产向量 |
| 文件存储 | MinIO，业务层只感知 `sys_oss_file` 和文件存储端口 |
| ORM | MyBatis-Plus 为主，复杂查询补充 MyBatis XML |
| 鉴权 | JWT access token + refresh token，Redis 保存登录态 / 黑名单 |
| 实时通信 | SSE 用于 Agent 流式响应，WebSocket 用于私信、群聊、通知 |
| Agent 平台 | Coze，后端通过适配层封装 Bot / Workflow / Conversation 调用 |
| LLM 适配 | 预留 Spark、OpenAI、DashScope 等模型适配实现 |

## 3. 分层设计

推荐采用偏 DDD 的实用分层：

```text
interfaces    对外入口：Controller、SSE、WebSocket、管理端接口
application   用例编排：注册登录、生成资源、提交测试、解析文档等
domain        领域模型与领域服务：画像、计划、资源、评测、工单、Agent 记忆
infrastructure 技术适配：MySQL、Redis、RabbitMQ、Chroma、MinIO、Coze、LLM、第三方 API
common        通用能力：统一响应、异常、鉴权、分页、审计、枚举、工具类
```

依赖方向：

```text
interfaces -> application -> domain
application -> infrastructure adapter interface
infrastructure -> external systems
```

原则：

1. Controller 不写业务，只做参数接收、鉴权上下文获取、响应组装。
2. Application Service 负责一个完整用例的流程编排和事务边界。
3. Domain Service 负责业务规则，例如学习计划调整、评测反哺画像、资源质量状态流转。
4. Infrastructure 负责 MySQL、Redis、RabbitMQ、Chroma、MinIO、Coze、LLM 等技术细节。

## 4. 后端模块划分

### 4.1 核心模块

| 模块 | 包名建议 | 主要职责 | 主要表 |
| --- | --- | --- | --- |
| 认证与用户 | `auth`、`user` | 注册、登录、token、当前用户、设备安全、用户设置 | `sys_user`、`user_login_session`、`user_settings` |
| Spark 画像 | `profile` | 首次引导、画像生成、画像重建、长期偏好沉淀 | `user_profile`、`agent_memory` |
| Agent 协同 | `agent` | Coze Bot / Workflow 调用、会话映射、消息落库、SSE 流式转发、记忆摘要、Prompt 配置入口 | `agent_chat_session`、`agent_chat_message`、`agent_memory`、`sys_agent_prompt` |
| Nebula 探索 | `explore` | 全局探索、思维导图、知识关联图谱、资源生成任务触发 | `async_generation_task`、`learning_resource` |
| 课程与知识图谱 | `knowledge` | 课程、知识点、节点关系、拓扑查询 | `course`、`knowledge_node`、`knowledge_node_relation` |
| 学习资源 | `resource` | 资源列表、详情、版本、收藏、学习进度、导出、质量反馈 | `learning_resource`、`learning_resource_version`、`user_resource_favorite`、`user_learning_record` |
| 学习计划 | `study` | 当前计划、Strict 生成 / 调整计划、日历事件、任务执行 | `study_plan`、`study_task`、`user_knowledge_progress` |
| 练习与评测 | `practice`、`evaluation` | 练习列表、答题、提交、代码题评测、Coach 报告 | `question_bank`、`practice_set`、`user_question_record`、`evaluation_report` |
| 知识库 / RAG | `kb`、`rag` | 文档上传、解析、切片、向量化、召回、引用、重解析 | `kb_document`、`kb_chunk_record`、`kb_parse_strategy`、`sys_oss_file` |
| 文件 | `file` | 上传凭证、上传完成、文件元数据、访问 URL | `sys_oss_file` |
| 社区文章 | `community` | 文章、评论、点赞、收藏、异步审核 | `blog_post`、`blog_comment`、`blog_like`、`blog_favorite` |
| 管理端 | `manage` | 数据看板、用户管理、知识库管理、工单、资源治理、Prompt 管理 | `sys_admin`、`sys_role`、`feedback_ticket`、`sys_operation_log` |

### 4.2 后置模块

| 模块 | 包名建议 | 说明 |
| --- | --- | --- |
| IM 与通知 | `im`、`notification` | 私信、群聊、通知，WebSocket 可后置 |
| 成就与挑战 | `achievement`、`challenge` | 技能激励、周挑战、方案提交 |
| 商城资产 | `mall` | 商城、虚拟资产，建议比赛阶段不作为主线 |

## 5. 关键业务链路

### 5.1 首次使用与画像生成

```text
注册 / 登录
 -> 获取画像引导问题
 -> 提交画像回答
 -> 调用 Agent / 规则引擎生成 user_profile
 -> 写入长期记忆 agent_memory
 -> 触发 Strict 生成推荐学习计划
 -> 返回 recommendedPlanId
```

关键点：

1. `user_profile` 保存稳定画像字段。
2. `agent_memory` 保存动态偏好、长期目标、薄弱点等可演化信息。
3. 画像生成后应能触发学习计划初始化。

### 5.2 Nebula 资源生成与 Horizon 质检

```text
Spark 发起探索 / 资源生成
 -> 创建 async_generation_task
 -> 投递 RabbitMQ 资源生成任务
 -> 后端 Agent 适配层调用 Coze Nebula Bot / Workflow 生成结构化资源草案
 -> RAG 检索 Chroma / kb_chunk_record 获取依据
 -> 后端 Agent 适配层调用 Coze Horizon Bot / Workflow 做事实核查和质量评分
 -> 写 learning_resource + learning_resource_version + sys_oss_file
 -> 更新任务状态与资源质量状态
```

关键点：

1. 生成过程走 RabbitMQ 异步任务，前端通过进度查询或 SSE 获取状态。
2. Horizon 核查结果应该写入资源质量字段，失败时生成可追踪工单或失败原因。
3. 资源版本表是后续修正、回滚和审核的基础。
4. Coze 的 Bot ID、Workflow ID、空间 ID、版本等配置不写死在业务代码中，由配置表或配置中心管理。

### 5.3 学习计划与动态调整

```text
读取画像 + 知识图谱 + 已有进度
 -> Strict 生成 study_plan
 -> 拆分 study_task
 -> Spark 执行学习 / 练习
 -> user_learning_record + user_knowledge_progress 更新
 -> Coach 评测报告反哺
 -> Strict 动态调整后续任务
```

关键点：

1. 学习计划是用户维度的个性化实例，不直接等同课程模板。
2. 知识点进度是技能树、拓扑、推荐和复习提醒的共同依据。
3. 评测结果需要反哺画像和计划，而不是只生成报告。

### 5.4 知识库入库与 RAG

```text
Manage 上传文件
 -> 写 sys_oss_file
 -> 创建 kb_document
 -> RabbitMQ: kb.document.parse
 -> 解析文档并切片，写 kb_chunk_record
 -> RabbitMQ: kb.chunk.embed
 -> 写入 Chroma
 -> 回写 chroma_point_id 与文档状态
```

关键点：

1. MySQL 保存可追溯元数据，Chroma 保存向量，MinIO 保存原始文件和生成文件。
2. 删除 / 重解析时必须同步处理 MySQL 逻辑删除与 Chroma 向量删除。
3. RAG 返回结果必须带来源文档、切片摘要和知识点元数据。

### 5.5 争议内容复核

```text
Spark 举报 AI 内容
 -> 创建 feedback_ticket
 -> Manage 复核
 -> 若确认 AI 错误：修正知识库 / 资源版本 / Prompt
 -> 记录 sys_operation_log
 -> 必要时触发文档重解析或资源重生成
```

关键点：

1. 争议对象可以是资源、Agent 消息、评论、知识切片或系统行为。
2. 工单是 AI 内容治理闭环的核心，不只是客服反馈。
3. 修正动作需要可审计。

### 5.6 社区文章发布与审核

```text
Spark 发布文章 / 评论
 -> 写 blog_post / blog_comment，状态为 pending
 -> 投递 RabbitMQ: community.moderation
 -> 调用审核适配层，可使用 Coze Workflow 或 LLM moderation
 -> 通过：状态改为 published
 -> 风险：状态改为 risk / blocked，并生成审核记录或工单
```

关键点：

1. MVP 不做复杂内容风控，但要保留状态流转，避免后续重构。
2. 文章列表默认只展示 `published`。
3. 作者本人可以看到自己的 `pending`、`risk`、`blocked` 状态。
4. 高风险内容可进入 `feedback_ticket`，统一纳入 Manage 复核。

## 6. 数据存储边界

### 6.1 MySQL

保存结构化事实数据：

1. 用户、画像、设置、登录会话。
2. 课程、知识点、资源、计划、任务、进度。
3. 题库、答题记录、评估报告。
4. Agent 会话、消息、长期记忆、Graph checkpoint。
5. 知识库文档、切片、文件元数据。
6. 管理端权限、工单、操作日志。

### 6.2 Redis

保存短期、高频、协同状态：

1. 登录态、黑名单、热点缓存。
2. Agent 短期上下文、流式输出缓冲。
3. 学习时长、打卡、排行榜。
4. 资源生成进度、知识库解析进度。
5. 分布式锁、热点对象缓存与短 TTL 状态。

### 6.3 RabbitMQ

保存异步任务消息：

1. `resource.generate`：Nebula 资源生成任务。
2. `resource.audit`：Horizon 资源质检任务。
3. `kb.document.parse`：知识库文档解析任务。
4. `kb.chunk.embed`：知识切片向量化任务。
5. `ticket.review.notify`：争议工单通知与后续处理。
6. `community.moderation`：社区文章 / 评论审核任务。

建议每类关键任务配置死信队列、重试次数和幂等键，幂等键可复用 `async_generation_task.idempotent_key` 或业务目标 ID。

MVP 阶段消费者与 API 在同一个 Spring Boot 应用内运行：

```text
interfaces/controller
application/usecase
infrastructure/rabbitmq/publisher
infrastructure/rabbitmq/consumer
worker/resource
worker/kb
worker/community
```

后续如生成任务压力变大，可将 `worker/*` 与 RabbitMQ consumer 拆成独立进程，API 应用不需要改业务接口。

### 6.4 Chroma

保存向量检索数据：

1. `edu_ground_truth`：官方权威知识库。
2. `spark_personal_notes`：用户个人笔记 / 错题本。
3. `video_transcripts`：视频字幕。
4. `multimodal_assets`：图文 / 多模态资源。

### 6.5 MinIO

保存文件对象：

1. 知识库原始文档、课件、教材、视频、字幕。
2. Nebula 生成的文档、思维导图、音频、视频等资源文件。
3. 社区文章图片、头像、附件。
4. 导出文件和临时文件。

MySQL 的 `sys_oss_file` 保存文件元数据、bucket、object key、hash、MIME 类型、大小等信息，业务接口不直接暴露 MinIO 内部路径。

## 7. 数据库增补与状态枚举

当前 `hope_sparks.sql` 已覆盖大多数核心表，但结合 Coze、异步审核、代码题 AI 评阅，需要少量增补或字段语义扩展。

### 7.1 新增 `sys_agent_config`

用途：保存 Coze Bot / Workflow 路由配置，不保存 Token 等敏感凭证。

建议 DDL：

```sql
CREATE TABLE `sys_agent_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agent_code` varchar(50) NOT NULL COMMENT 'nebula/horizon/strict/ava/sage/coach/oldmoney',
  `agent_name` varchar(50) NOT NULL COMMENT '智能体名称',
  `coze_space_id` varchar(100) DEFAULT NULL COMMENT 'Coze 空间ID',
  `coze_bot_id` varchar(100) DEFAULT NULL COMMENT 'Coze Bot ID',
  `coze_workflow_id` varchar(100) DEFAULT NULL COMMENT 'Coze Workflow ID',
  `default_model_provider` varchar(50) DEFAULT NULL COMMENT '备用模型供应商',
  `stream_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持流式',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  `version` varchar(50) DEFAULT 'v1',
  `extra_config` json DEFAULT NULL COMMENT '超时、温度、参数映射等扩展配置',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_code` (`agent_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体运行配置表';
```

### 7.2 Agent 会话映射增补

现有 `agent_chat_session` 和 `agent_chat_message` 能保存本地会话与消息，但缺少 Coze 外部 ID。建议增补字段：

| 表 | 字段 | 说明 |
| --- | --- | --- |
| `agent_chat_session` | `external_conversation_id` | Coze conversation / thread ID |
| `agent_chat_session` | `external_section_id` | Coze section ID，可选 |
| `agent_chat_session` | `agent_config_id` | 关联 `sys_agent_config.id` |
| `agent_chat_message` | `external_message_id` | Coze message ID |
| `agent_chat_message` | `raw_response` | JSON，保存 Coze 原始响应摘要，便于排查 |

### 7.3 异步任务增补

现有 `async_generation_task` 可复用为资源生成、计划生成、评测报告等任务表。建议增补：

| 字段 | 说明 |
| --- | --- |
| `task_type` | `resource_generate`、`resource_audit`、`plan_generate`、`kb_parse`、`kb_embed`、`community_moderation` |
| `external_run_id` | Coze workflow run ID 或第三方任务 ID |
| `retry_count` | 当前重试次数 |
| `max_retry` | 最大重试次数 |
| `started_at` / `finished_at` | 任务开始和结束时间 |

### 7.4 社区审核状态

现有 `blog_post.post_status` 只有 `1发布 / 0下架`，不够表达审核状态。建议扩展为：

| 值 | API 枚举 | 说明 |
| --- | --- | --- |
| `0` | `draft` | 草稿 |
| `1` | `published` | 已发布 |
| `2` | `pending` | 待审核 |
| `3` | `risk` | 风险待复核 |
| `4` | `blocked` | 审核拦截 |
| `5` | `offline` | 管理员下架 |

`blog_comment` 建议新增 `comment_status tinyint NOT NULL DEFAULT 2`，枚举同上。文章和评论列表默认只查询 `published`。

### 7.5 代码题 AI 评阅

代码题仍使用 `question_bank.question_type = code`。MVP 不执行真实代码，只保存文本答案并调用 Coach / LLM 做 AI 评阅：

1. `user_question_record.user_answer` 保存代码文本。
2. `user_question_record.judge_mode = ai`。
3. `user_question_record.feedback_text` 保存评阅反馈。
4. `user_question_record.score` 保存 AI 评分。
5. 后续真实沙箱可新增 `code_submission`、`code_test_case`、`code_run_result`。

## 8. 安全、配置与权限边界

### 8.1 敏感配置管理

不入库的配置：

1. Coze API Token。
2. MinIO access key / secret key。
3. RabbitMQ 用户名和密码。
4. MySQL、Redis、Chroma 连接凭证。
5. JWT 签名密钥。

这些配置放在环境变量、部署平台 Secret 或本地 `.env`，`application.yml` 只保留占位符。

### 8.2 前台与后台隔离

MVP 共用一个 Spring Boot 应用：

1. Spark 前台接口：`/api/v1/**`。
2. Manage 后台接口：`/api/v1/manage/**`。
3. 前台 token 解析 `sys_user`，后台 token 解析 `sys_admin`。
4. 后台权限基于 `sys_admin`、`sys_role`、`sys_permission` 做 RBAC。
5. 管理端关键操作写 `sys_operation_log`。

### 8.3 鉴权建议

1. Access Token 短有效期，Refresh Token 长有效期。
2. Redis 保存 token/session 映射和强制下线状态。
3. 用户封禁、管理员禁用时立即删除对应 Redis 登录态。
4. `X-Request-Id` 贯穿日志、RabbitMQ 消息、Coze 调用和错误响应。

## 9. 本地开发与迁移策略

### 9.1 Flyway 迁移

使用 Flyway 管理数据库版本：

```text
src/main/resources/db/migration
├─ V1__init_hope_sparks_schema.sql
└─ V2__future_changes.sql
```

约定：

1. `V1` 基于讨论后更新的 `资源/hope_sparks.sql` 整理为初始化脚本。
2. `V1` 已包含 `sys_agent_config`、Coze 会话映射字段、RabbitMQ 任务追踪字段、社区审核状态字段。
3. `V2+` 仅用于后续新需求，不再把已讨论确认的结构拆成多段补丁。

### 9.2 Docker Compose 本地环境

本地开发使用 Docker Compose 启动依赖：

| 服务 | 默认端口 | 说明 |
| --- | ---: | --- |
| MySQL | `3306` | 主业务数据库 |
| Redis | `6379` | 登录态、缓存、短期上下文 |
| RabbitMQ | `5672` / `15672` | 任务队列 / 管理面板 |
| MinIO | `9000` / `9001` | 文件对象存储 / 控制台 |
| Chroma | `8000` | 向量数据库 |

Spring Boot 通过 `application-local.yml` 连接本地容器，敏感配置从环境变量读取。

### 9.3 Mock Adapter

外部依赖都通过端口接口访问，并提供 mock 实现：

| 能力 | 真实实现 | Mock 实现 |
| --- | --- | --- |
| Agent | `CozeAgentAdapter` | `MockAgentAdapter` |
| LLM | `Spark/OpenAI/DashScope Adapter` | `MockLlmAdapter` |
| 文件存储 | `MinioFileStorageAdapter` | `LocalMemoryFileStorageAdapter` 或 `LocalDiskFileStorageAdapter` |
| 队列 | `RabbitMqTaskPublisher` | `InMemoryTaskPublisher` |
| 向量检索 | `ChromaVectorAdapter` | `MockVectorSearchAdapter` |

激活方式：

```yaml
hope:
  adapter-mode: mock
```

或按能力拆分：

```yaml
hope:
  agent:
    mode: mock
  queue:
    mode: rabbitmq
  file:
    mode: minio
```

Mock 的目标不是模拟全部外部行为，而是保证核心用例能跑通：

1. Agent 对话返回可预测文本。
2. 资源生成返回固定结构化结果。
3. 知识库解析返回固定切片。
4. 社区审核返回 pass / risk 可配置结果。
5. 文件上传返回本地可访问的假 URL 或本地路径。

## 10. Agent 与模型适配设计

### 10.1 Coze 调用边界

后端不在 Controller 中直接调用 Coze，而是通过统一端口封装：

```text
application use case
 -> domain agent service
 -> AgentGateway
 -> CozeAgentAdapter
 -> Coze API
```

建议接口：

```text
AgentGateway
├─ createConversation(agentCode, userId, context)
├─ sendMessage(sessionId, message, options)
├─ streamMessage(sessionId, message, options)
├─ runWorkflow(workflowCode, input, options)
└─ getRunStatus(runId)
```

后端需要维护本地会话和 Coze 会话的映射关系：

| 本地概念 | Coze 概念 | 落库建议 |
| --- | --- | --- |
| `agent_chat_session.id` | conversation_id / thread_id | `agent_chat_session` 扩展或元数据字段 |
| `agent_chat_message.id` | message_id | `agent_chat_message` 扩展或元数据字段 |
| `agentCode` | bot_id / workflow_id | `sys_agent_prompt` 或新增配置表 |
| `async_generation_task.id` | workflow run id | `async_generation_task` 扩展或任务元数据 |

### 10.2 Coze Bot / Workflow 映射

| Agent | Coze 形态 | 后端调用场景 | 交互方式 |
| --- | --- | --- | --- |
| Ava | Bot | 情绪激励、学习提醒、陪伴式对话 | SSE 流式 |
| Sage | Bot | 苏格拉底式答疑、伴读提问、效率建议 | SSE 流式 |
| Coach | Bot + Workflow | 练习提示可用 Bot；阶段评测报告建议 Workflow | 对话流式 / 任务异步 |
| Old money | Bot | 行业经验、反方辩论、方案挑战 | SSE 流式 |
| Nebula | Workflow | 探索学习方向、生成结构化资源、生成思维导图 | RabbitMQ 异步 + 状态查询 |
| Horizon | Workflow | 事实核查、资源质检、文章润色 / 审核辅助 | RabbitMQ 异步 / 同步短调用 |
| Strict | Workflow | 生成学习计划、调整学习路径、拆分学习任务 | RabbitMQ 异步 + 状态查询 |

确认新增独立配置表 `sys_agent_config`，不要把 Coze 配置塞进 `sys_agent_prompt`。`sys_agent_prompt` 更适合保存提示词版本，`sys_agent_config` 保存运行时连接和路由配置。

建议字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `agent_code` | `nebula`、`horizon`、`strict`、`ava`、`sage`、`coach`、`oldmoney` |
| `agent_name` | 展示名称 |
| `coze_bot_id` | 对话型 Bot ID，可为空 |
| `coze_workflow_id` | Workflow ID，可为空 |
| `coze_space_id` | Coze 空间 ID |
| `default_model_provider` | 备用模型供应商标识 |
| `stream_enabled` | 是否支持流式 |
| `enabled` | 是否启用 |
| `version` | 配置版本 |
| `extra_config` | JSON，保存超时、温度、参数映射等扩展配置 |
| `created_at` / `updated_at` | 时间字段 |

### 10.3 流式与异步边界

| 场景 | 推荐方式 | 原因 |
| --- | --- | --- |
| Agent 普通对话 | SSE 流式 | 体验优先，用户需要即时反馈 |
| Sage 伴读提问 | SSE 流式 | 和阅读页面强交互 |
| Coach 单题提示 | SSE 流式或短同步 | 返回内容短，可以实时给提示 |
| Nebula 资源生成 | RabbitMQ 异步任务 | 产物复杂，可能耗时长 |
| Horizon 资源质检 | RabbitMQ 异步任务 | 可重试、可追踪、可审计 |
| Strict 计划生成 | RabbitMQ 异步任务 | 需要结合画像、资源、日历，适合状态跟踪 |
| 知识库解析 / 向量化 | RabbitMQ 异步任务 | 文件处理与 embedding 耗时长 |
| 社区文章审核 | RabbitMQ 异步任务 | 不阻塞发布动作，保留审核状态 |

异步任务统一写入 `async_generation_task` 或业务状态表，前端通过状态查询接口获取进度。需要即时进度的页面可额外通过 SSE 订阅任务事件。

### 10.4 LLM 适配层

虽然智能体调用 MVP 直接走 Coze，但仍保留模型适配层，服务以下场景：

1. 简单文本生成、摘要、标签提取可以不必创建 Coze Workflow。
2. 后续需要切换或混用讯飞星火、OpenAI、DashScope 等模型。
3. RAG、审核、摘要、embedding 等基础能力可以独立演进。

建议接口：

```text
LlmGateway
├─ chat(request)
├─ streamChat(request)
├─ embed(texts, options)
├─ rerank(query, candidates)
└─ moderate(content)
```

业务代码只依赖 `AgentGateway` 和 `LlmGateway`，不直接依赖 Coze SDK、模型 SDK 或 HTTP 细节。

## 11. API 设计约定

1. 全部接口以 `/api/v1` 为前缀。
2. 返回结构统一为 `code`、`message`、`data`、`requestId`。
3. 分页结构统一为 `page`、`pageSize`、`total`、`list`。
4. API 层使用 camelCase，数据库层使用 snake_case。
5. 主键 ID 对前端统一以字符串返回。
6. Controller 按 API 文档模块分组，内部调用 application 用例服务。
7. DTO / Assembler 层负责字段映射和枚举转换，不让前端直接依赖数据库枚举。

## 12. 推荐项目目录

```text
arch-demo
├─ pom.xml
├─ arch.md
└─ src
   ├─ main
   │  ├─ java
   │  │  └─ com
   │  │     └─ hopeandsparks
   │  │        ├─ HopeAndSparksApplication.java
   │  │        ├─ common
   │  │        │  ├─ config
   │  │        │  ├─ security
   │  │        │  ├─ exception
   │  │        │  └─ response
   │  │        ├─ interfaces
   │  │        │  ├─ auth
   │  │        │  ├─ user
   │  │        │  ├─ agent
   │  │        │  ├─ resource
   │  │        │  ├─ study
   │  │        │  ├─ practice
   │  │        │  ├─ kb
   │  │        │  ├─ community
   │  │        │  └─ manage
   │  │        ├─ application
   │  │        ├─ domain
   │  │        └─ infrastructure
   │  │           ├─ persistence
   │  │           ├─ redis
   │  │           ├─ rabbitmq
   │  │           ├─ chroma
   │  │           ├─ minio
   │  │           ├─ coze
   │  │           ├─ llm
   │  │           └─ mock
   │  └─ resources
   │     ├─ application.yml
   │     ├─ application-local.yml
   │     ├─ application-mock.yml
   │     ├─ mapper
   │     └─ db
   ├─ docker
   │  └─ docker-compose.yml
   └─ test
      └─ java
```

## 13. MVP 优先级

### P0：必须优先打通

1. 登录注册、当前用户、画像引导。
2. Agent 会话、流式响应、基础记忆落库。
3. Nebula 探索、资源列表、资源详情、资源生成进度。
4. 学习计划、学习拓扑、知识点资源网络。
5. 练习提交、评测报告、薄弱点反哺。
6. Manage 知识库文档上传、解析状态、争议工单。
7. 社区文章列表、详情、发布、评论、点赞、收藏。
8. 社区文章 / 评论异步审核状态流转。

### P1：增强体验

1. 视频学习进度、文档阅读进度、Sage 伴读。
2. 技能树点亮、复习提醒、通知。
3. 设备安全、缓存清理、用户设置。

### P2：二期扩展

1. 私信、群聊、WebSocket 完整消息系统。
2. 周挑战、成就、商城。
3. 用户行为风控、内容 AI 审核后台。

## 14. 工程落地顺序

建议按以下顺序推进，尽量让每一步都能单独运行和演示：

1. 工程骨架：Spring Boot、统一响应、异常、配置、MyBatis-Plus、健康检查。
2. 基础设施：MySQL、Redis、RabbitMQ、MinIO、Chroma、Coze 客户端适配。
3. 鉴权与用户：注册登录、用户 / 管理员 token、RBAC 拦截器。
4. Agent 基础链路：`sys_agent_config`、会话、消息、SSE 流式转发。
5. 文件与知识库：MinIO 上传、`sys_oss_file`、`kb_document`、解析任务状态。
6. 资源与学习计划：Nebula / Strict 异步任务、资源落库、计划落库。
7. 练习与评测：题目、答题记录、Coach AI 评阅、报告反哺。
8. 社区文章：文章、评论、点赞、收藏、异步审核。
9. Manage 后台：数据看板、知识库管理、工单复核、Prompt / Agent 配置管理。

## 15. Spring Boot 骨架首轮范围

下一阶段生成工程骨架时，首轮不直接铺满所有业务接口，而是先做“可启动 + 可迁移 + 可替换外部依赖”的基础版本。

首轮建议包含：

1. `pom.xml` 升级为 Spring Boot 3 工程。
2. `HopeAndSparksApplication` 启动类。
3. `application.yml`、`application-local.yml`、`application-mock.yml`。
4. 统一响应 `ApiResponse`、分页响应、全局异常处理。
5. Spring Security 基础配置和 JWT 工具占位。
6. MyBatis-Plus、Flyway、MySQL 连接配置。
7. Redis、RabbitMQ、MinIO、Chroma、Coze 的配置属性类。
8. `AgentGateway`、`LlmGateway`、`FileStorageGateway`、`TaskPublisher`、`VectorSearchGateway` 端口接口。
9. 真实 Adapter 的空实现 / TODO 框架，以及 Mock Adapter 可运行实现。
10. 健康检查接口：`GET /api/v1/health`。
11. Agent mock 对话接口：`POST /api/v1/agent-sessions/{sessionId}/messages` 可返回固定响应。
12. Docker Compose 文件。
13. Flyway `V1` 迁移脚本骨架，内容基于讨论后的完整 `hope_sparks.sql`。

首轮暂不做：

1. 完整注册登录。
2. 完整 RBAC。
3. 完整资源生成和知识库解析。
4. 真实 Coze / MinIO / Chroma 调用。
5. 真实业务 CRUD 全量生成。

这样第一版可以很快启动，后续每个模块再沿着架构边界逐步填实。

