# Hope and Sparks 后端架构草案

> 版本：v0.12  
> 状态：对话讨论稿  
> 资料来源：`资源/Hope and Sparks开发文档改.md`、`资源/Hope_and_Sparks_API接口文档.md`、`资源/apifox-openapi.yaml`、`资源/数据库设计-正式版.md`、`资源/hope_sparks.sql`

## 0. 已确认架构决策

1. 后端技术栈采用 Java 17 + Spring Boot 3.x + Maven + MySQL + Redis + Chroma。
2. ORM 优先采用 MyBatis-Plus；复杂 SQL 可补充 MyBatis XML。
3. 智能体相关调用直接使用 Coze，后端保留统一 Agent / LLM 适配层。
4. LLM 能力预留其他模型适配，不把业务代码绑定到单一模型供应商。
5. 文件存储使用 MinIO，业务层统一通过文件存储端口访问。
6. MVP 异步队列使用 Redis Stream，数据库设计直接沿用老版本 `queue:*` 命名。
7. MVP 包含社区文章模块，但 IM、商城、周挑战仍可后置。
8. Coze 组织方式：对话型能力使用独立 Bot，长流程与结构化产物使用 Workflow。
9. 流式策略：Agent 对话走 SSE 流式；资源生成、知识库解析、向量化走 Redis Stream 异步任务 + 状态查询。
10. Redis Stream 消费者 MVP 阶段放在同一个 Spring Boot 应用内，代码按 worker 边界组织，后续可拆独立 worker 服务。
11. 社区文章先落库为 `pending`，通过 Redis Stream 异步审核，通过后变为 `published`。
12. 新增 `sys_agent_config` 表并归入 `arch-infra`，保存 Coze Bot / Workflow 路由配置；`sys_agent_prompt` 只保存提示词与模型参数版本。
13. Coze Token、MinIO 密钥、Redis 密码、MySQL 密码等敏感配置统一放环境变量或部署配置，不入库。
14. Spark 前台和 Manage 后台 MVP 阶段共用一个 Spring Boot 应用和同一个 MySQL，通过路由、token 类型、RBAC 权限隔离。
15. 代码题 MVP 不引入真实沙箱，先做代码文本提交 + AI 评阅 + 评阅反馈。
16. 下一阶段先继续完善架构文档，再生成 Spring Boot 工程骨架。
17. 数据库迁移工具使用 Flyway，原始建表与架构增补 DDL 分版本管理。
18. 本地开发环境使用 Docker Compose 一键启动 MySQL、Redis、MinIO、Chroma。
19. Coze、MinIO、Redis Stream 在开发初期提供 Mock Adapter，没有真实凭证时也能跑通主流程。
20. 架构文档确认到当前边界后，下一阶段开始生成 Spring Boot 工程骨架。
21. Java 根包名使用 `com.hopeandsparks`。
22. Flyway `V1` 使用讨论后的完整初始化 SQL，即更新后的 `资源/hope_sparks.sql`，不再使用原始未修订版本叠加 V2-V5 补丁。
23. 后续工程以根目录下新建的 `arch` Maven 工程为准，不再以 `arch-demo` 作为目标工程。
24. `arch` 采用父 POM + 多 Maven 子模块结构，顶层只保留父 `pom.xml` 和各子模块目录。
25. 代码包结构改为熟悉的 `controller`、`service`、`mapper` 风格；每个业务模块内部按 `controller/service/mapper/entity/dto/vo` 组织。
26. `auth` 和 `user` 放在同一个 Maven 子模块 `arch-auth`，避免登录态、用户资料、设备安全、用户设置分散。
27. 文件能力不单独拆 `arch-file`，统一放入 `arch-infra`，由 MinIO 客户端、文件元数据服务和上传签名能力承载。
28. `arch-manage` 采用“后台聚合入口 + 后台自身权限表 + 只读统计查询”策略，不重新接管各业务模块的核心状态流转。
29. `sys_agent_config` 归 `arch-infra`，作为 Coze Bot / Workflow 路由与运行配置；`arch-agent` 只负责会话、消息、记忆和 Prompt 业务。
30. Nebula / Strict 等生成出来的学习资源统一由 `arch-resource` 落库；`arch-explore` 只负责探索入口、生成任务触发和探索过程记录。
31. `arch-boot` 允许放少量全局 Controller，例如健康检查、应用信息、文件上传入口；业务 Controller 仍归各业务子模块。
32. 用户收藏业务不放在 `arch-auth`；资源收藏归 `arch-resource`，文章收藏归 `arch-community`，用户中心只做展示聚合。
33. Redis Stream 基础能力集中在 `arch-infra`，具体 consumer 分散在业务模块内，谁消费任务谁负责业务状态流转。
34. `/api/v1/manage/knowledge-base/**` 由 `arch-manage` 承接后台入口；列表和统计可做只读查询，上传、删除、重解析等动作调用 `arch-kb` Service。
35. `async_generation_task` 不继续归 `arch-explore`，抽成 `arch-task`，作为通用异步任务状态能力；Redis Stream 客户端仍归 `arch-infra`，具体 consumer 仍归业务模块。
36. 后台 RBAC MVP 做到管理员、角色、菜单权限、关键按钮 / API 权限和操作日志；暂不做组织架构、数据范围权限、字段级权限和审批流。
37. 子模块依赖采用单向分层：`arch-common` 最底层，`arch-infra` / `arch-task` 依赖 `arch-common`，业务模块按需依赖 `arch-common` / `arch-infra` / `arch-task`，`arch-manage` 聚合调用业务 Service / Facade，`arch-boot` 依赖所有需要装配的模块；MVP 不拆 `xxx-api` 子模块。

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

推荐采用“多 Maven 子模块的模块化单体 + controller/service/mapper 分层 + 异步任务管道”的后端架构，而不是一开始拆微服务。

原因：

1. 当前已新建 `arch` Maven 工程，适合直接调整为父 POM 聚合工程。
2. API、数据库表、业务模块已经很多，用 Maven 子模块拆边界，比在单工程里堆包更容易维护。
3. 比赛阶段仍保持一个 Spring Boot 应用启动，避免微服务部署、联调和分布式事务复杂度。
4. Agent 编排、RAG、OSS、异步生成天然适合拆成独立模块，将来如果压力变大，可把 worker 或某个模块平滑拆成独立服务。

### 2.2 技术栈建议

| 层次 | 建议 |
| --- | --- |
| 后端框架 | Java 17 + Spring Boot 3.x |
| 构建工具 | Maven |
| API 文档 | OpenAPI / Apifox，接口以 `/api/v1` 为前缀 |
| 数据库 | MySQL，使用现有 `hope_sparks.sql` |
| 缓存 | Redis，缓存热点数据、登录态、短期上下文、进度、排行榜 |
| 异步队列 | Redis Stream，承载资源生成、知识库解析、向量化、审核等异步任务 |
| 向量库 | Chroma，保存知识切片、个人笔记、视频字幕、多模态资产向量 |
| 文件存储 | MinIO，业务层只感知 `sys_oss_file` 和文件存储端口 |
| ORM | MyBatis-Plus 为主，复杂查询补充 MyBatis XML |
| 鉴权 | JWT access token + refresh token，Redis 保存登录态 / 黑名单 |
| 实时通信 | SSE 用于 Agent 流式响应，WebSocket 用于私信、群聊、通知 |
| Agent 平台 | Coze，后端通过适配层封装 Bot / Workflow / Conversation 调用 |
| LLM 适配 | 预留 Spark、OpenAI、DashScope 等模型适配实现 |

## 3. 分层设计

采用更贴近日常开发习惯的 `controller / service / mapper` 分层。分层不是按顶层大包拆，而是在每个 Maven 业务子模块内部重复这套结构。

```text
controller  对外入口：REST API、SSE、WebSocket、管理端接口
service     业务编排：注册登录、生成资源、提交练习、解析文档、调用 Coze / Redis Stream 等
mapper      数据访问：MyBatis-Plus Mapper 与复杂 SQL XML
entity      数据库实体：与表结构保持 snake_case 到 camelCase 映射
dto         请求入参、内部命令对象
vo          响应视图对象、分页结果
enums       模块内枚举
config      模块内配置
```

依赖方向：

```text
controller -> service -> mapper -> MySQL
service -> common / infra client
service -> Redis Stream / MinIO / Chroma / Coze adapter
```

原则：

1. Controller 不写业务，只做参数接收、鉴权上下文获取、参数校验和响应组装。
2. Service 负责一个完整用例的流程编排、事务边界、状态流转和外部能力调用。
3. Mapper 只负责数据访问，复杂查询放在 MyBatis XML，避免 SQL 散落在 Service。
4. Entity 不直接返回给前端；Controller 返回 VO，入参使用 DTO。
5. 跨模块复用能力放到 `arch-common`、`arch-infra` 或明确的通用业务模块如 `arch-task`，业务模块之间避免随意互相调用。

## 4. 后端模块划分

### 4.1 核心模块

| 业务模块 | Maven 子模块 | Java 包名 | 主要职责 | 主要表 |
| --- | --- | --- | --- | --- |
| 启动模块 | `arch-boot` | `com.hopeandsparks.boot` | Spring Boot 启动类、全局配置装配、模块扫描入口、少量全局 Controller | 无独立业务表 |
| 公共模块 | `arch-common` | `com.hopeandsparks.common` | 统一响应、异常、分页、枚举、工具类、基础安全上下文 | 无独立业务表 |
| 基础设施模块 | `arch-infra` | `com.hopeandsparks.infra` | Redis Stream、MinIO 文件能力、Chroma、Coze、LLM、通用配置客户端 | `sys_oss_file`、`sys_agent_config` |
| 异步任务模块 | `arch-task` | `com.hopeandsparks.task` | 通用异步任务创建、状态流转、重试、失败原因、幂等键、任务进度查询 | `async_generation_task` |
| 认证与用户 | `arch-auth` | `com.hopeandsparks.auth` | 注册、登录、token、当前用户、用户资料、设备安全、用户设置 | `sys_user`、`user_login_session`、`user_settings` |
| Spark 画像 | `arch-profile` | `com.hopeandsparks.profile` | 首次引导、画像生成、画像重建、长期偏好沉淀 | `user_profile`、`agent_memory` |
| Agent 协同 | `arch-agent` | `com.hopeandsparks.agent` | 会话映射、消息落库、SSE 流式转发、记忆摘要、Prompt 配置入口；调用 `arch-infra` 完成 Coze 运行 | `agent_chat_session`、`agent_chat_message`、`agent_memory`、`sys_agent_prompt` |
| Nebula 探索 | `arch-explore` | `com.hopeandsparks.explore` | 全局探索、思维导图、知识关联图谱、资源生成任务触发；不直接落学习资源表和异步任务表 | 可按需要新增 `explore_*` 过程表 |
| 课程与知识图谱 | `arch-knowledge` | `com.hopeandsparks.knowledge` | 课程、知识点、节点关系、拓扑查询 | `course`、`knowledge_node`、`knowledge_node_relation` |
| 学习资源 | `arch-resource` | `com.hopeandsparks.resource` | 资源生成结果落库、资源列表、详情、版本、收藏、学习进度、导出、质量反馈 | `learning_resource`、`learning_resource_version`、`user_resource_favorite`、`user_learning_record` |
| 学习计划 | `arch-study` | `com.hopeandsparks.study` | 当前计划、Strict 生成 / 调整计划、日历事件、任务执行 | `study_plan`、`study_task`、`user_knowledge_progress` |
| 练习与评测 | `arch-practice` | `com.hopeandsparks.practice` | 练习列表、答题、提交、代码题评测、Coach 报告 | `question_bank`、`practice_set`、`user_question_record`、`evaluation_report` |
| 知识库 / RAG | `arch-kb` | `com.hopeandsparks.kb` | 文档上传、解析、切片、向量化、召回、引用、重解析；文件只保存 `file_id` 引用 | `kb_document`、`kb_chunk_record`、`kb_parse_strategy` |
| 社区文章 | `arch-community` | `com.hopeandsparks.community` | 文章、评论、点赞、收藏、异步审核 | `blog_post`、`blog_comment`、`blog_like`、`blog_favorite` |
| 管理端 | `arch-manage` | `com.hopeandsparks.manage` | 后台聚合入口、后台账号权限、操作日志、只读统计看板 | `sys_admin`、`sys_role`、`sys_permission`、`sys_admin_role`、`sys_role_permission`、`sys_operation_log` |

### 4.2 后置模块

| 模块 | Maven 子模块 | 说明 |
| --- | --- | --- |
| IM 与通知 | `arch-im`、`arch-notification` | 私信、群聊、通知，WebSocket 可后置 |
| 成就与挑战 | `arch-achievement`、`arch-challenge` | 技能激励、周挑战、方案提交 |
| 商城资产 | `arch-mall` | 商城、虚拟资产，建议比赛阶段不作为主线 |

### 4.3 模块归属补充

#### 4.3.1 `arch-auth` 同时承载 auth 和 user

`auth` 和 `user` 不再拆成两个 Maven 子模块。原因是登录注册、当前用户、用户设置、设备安全、token 管理都围绕同一个用户身份模型展开，拆开后会造成用户实体、登录态和安全上下文来回依赖。

`arch-auth` 包含：

1. 前台用户注册、登录、刷新 token、退出登录。
2. 当前用户信息、公开用户信息、用户资料修改。
3. 用户设置、设备管理、修改密码、修改邮箱。
4. 前台用户登录态、黑名单、设备安全相关 Redis 读写。

后台管理员账号不放在 `arch-auth`，归 `arch-manage` 管理。

用户收藏业务不放在 `arch-auth`。资源收藏归 `arch-resource`，文章收藏归 `arch-community`。如果前端需要“我的收藏”页面，MVP 可以由前端分别调用资源收藏和文章收藏接口后聚合；后续如需要统一用户中心聚合接口，再考虑放入 `arch-profile` 或单独增加用户中心聚合能力。

#### 4.3.2 `arch-infra` 承载文件能力

文件能力不单独拆 `arch-file`。MVP 阶段文件能力主要是基础设施能力，和 MinIO、签名 URL、对象元数据、临时文件、导出文件强相关，因此放入 `arch-infra` 更直接。

`arch-infra` 的文件职责包括：

1. MinIO 客户端封装。
2. 上传 token / 预签名 URL 生成。
3. 上传完成后的 `sys_oss_file` 元数据写入。
4. 文件访问 URL、临时 URL、导出文件路径生成。
5. 文件 hash、MIME 类型、大小、bucket、object key 等基础元数据管理。

业务模块只保存业务对象和 `file_id` 关联，不直接依赖 MinIO SDK。例如 `arch-kb` 保存 `kb_document.source_file_id`，`arch-resource` 保存资源版本关联文件，真正文件读写通过 `arch-infra` 的 Service 完成。

`sys_agent_config` 也归 `arch-infra`。它保存 Coze Bot ID、Workflow ID、空间 ID、流式开关和额外运行参数，属于外部智能体平台的运行路由配置。`arch-agent` 读取或引用该配置来完成会话、消息、Prompt 和记忆业务，但不拥有这张表的核心维护职责。

#### 4.3.3 生成资源落库职责

生成出来的学习资源统一由 `arch-resource` 落库。

推荐链路：

```text
arch-explore
 -> 调用 arch-task 创建 async_generation_task
 -> 调用 arch-infra 投递 Redis Stream 生成任务
 -> 触发 Nebula / Strict 等生成流程
 -> 生成完成后调用 arch-resource Service
 -> arch-resource 写 learning_resource + learning_resource_version
 -> arch-infra 写 sys_oss_file 或生成文件 URL
```

边界说明：

1. `arch-explore` 负责探索入口、思维导图、知识关联图谱和资源生成任务触发。
2. `arch-resource` 负责学习资源生命周期，包括新建、版本、上下架、质检状态、收藏、学习进度和导出。
3. `arch-task` 负责 `async_generation_task` 的创建、状态、进度、重试和失败原因。
4. `arch-infra` 负责文件对象、MinIO、Coze 运行配置、Redis Stream 发布等基础能力。
5. 这样资源表和异步任务表都不会被 `arch-explore`、`arch-study`、`arch-manage` 多处直接写入，后续状态流转更容易维护。

#### 4.3.4 `arch-task` 定位

`arch-task` 是应用层的通用异步任务状态模块，不是基础设施模块。它只关心“一件异步业务任务现在是什么状态”，不关心 Redis Stream 的底层读写，也不直接处理具体业务落库。

`arch-task` 包含：

1. `async_generation_task` 的 Mapper / Service。
2. 任务创建、幂等键检查、状态流转、进度更新。
3. 重试次数、最大重试次数、失败原因、外部 run id 记录。
4. 面向前台和后台的任务状态查询接口能力。

推荐边界：

```text
arch-infra
 -> Redis Stream 基础客户端、XADD、XREADGROUP、ACK、DLQ 工具

arch-task
 -> async_generation_task 表和任务状态服务

arch-explore / arch-study / arch-kb / arch-community
 -> 创建任务时调用 arch-task
 -> 投递消息时调用 arch-infra
 -> 消费完成后调用 arch-task 更新状态
 -> 具体业务数据仍写回本模块或目标业务模块
```

不建议把 `async_generation_task` 放在 `arch-explore`，因为知识库解析、社区审核、学习计划生成、练习评测报告都会复用任务状态能力；如果放在 `arch-explore`，其他模块会产生反向依赖。

不建议把 `async_generation_task` 放进 `arch-infra`，因为它是应用业务任务状态，不是 Redis、MinIO、Coze 这类外部基础设施适配。

#### 4.3.5 `arch-manage` 定位

`arch-manage` 采用“后台聚合入口 + 后台自身权限表 + 只读统计查询”策略。

后台聚合入口：

1. 承接 `/api/v1/manage/**` 下的后台接口。
2. 按后台页面组织 Controller 和 VO。
3. 对用户、知识库、资源、社区、Agent 配置等后台操作做统一入口编排。
4. 具体业务动作调用对应模块 Service，不在 `arch-manage` 重写业务状态流转。

后台自身权限表：

1. `sys_admin`：后台管理员账号。
2. `sys_role`：后台角色。
3. `sys_permission`：菜单、按钮、接口权限，可用 `type = menu / button / api` 区分。
4. `sys_admin_role`、`sys_role_permission`：管理员、角色、权限关系。
5. `sys_operation_log`：后台关键操作日志。

后台 RBAC 的 MVP 范围：

1. 管理员登录、退出和禁用。
2. 角色管理，支持给管理员分配角色。
3. 菜单权限，控制后台能看到哪些页面，例如知识库管理、资源管理、社区审核、Agent 配置、用户管理、数据统计、系统设置。
4. 关键按钮 / API 权限，控制上传、删除、重解析、审核通过、驳回、下架、启停 Agent 配置、禁用用户等高风险动作。
5. 后端接口必须校验权限码，不能只依赖前端隐藏按钮。
6. 操作日志记录所有高风险写操作。

MVP 暂不做：

1. 部门组织架构。
2. 数据范围权限。
3. 字段级权限。
4. 多级审批流。
5. 复杂审计报表。

只读统计查询：

1. 后台首页总览、用户数、活跃数、资源数、待审核数量。
2. 知识库解析成功 / 失败数量、Redis Stream 任务状态统计。
3. 社区待审核文章 / 评论数量、风险内容数量。
4. Agent 调用次数、失败次数、Coze Workflow 运行概览。
5. MinIO 存储占用、资源类型分布等运营看板。

`arch-manage` 可以为后台看板和跨表列表保留专用只读 Mapper，例如 `ManageStatisticsMapper`、`ManageDashboardMapper`。这些 Mapper 原则上只做查询，不直接更新业务状态。

不放进 `arch-manage` 的核心规则：

1. 用户封禁、解封的核心状态流转，由 `arch-auth` 提供 Service。
2. 知识库重解析、解析状态流转，由 `arch-kb` 提供 Service。
3. 资源下架、资源质检状态流转，由 `arch-resource` 提供 Service。
4. 社区文章 / 评论审核状态流转，由 `arch-community` 提供 Service。
5. Agent 会话、Prompt 业务由 `arch-agent` 提供 Service；Coze 路由配置由 `arch-infra` 提供 Service。

这样 `arch-manage` 负责“后台怎么进入、谁能操作、后台怎么看全局数据”，具体业务怎么变化仍由对应业务模块负责。

`/api/v1/manage/knowledge-base/**` 是后台知识库管理入口，放在 `arch-manage` 的 Controller 中：

1. 文档列表、解析状态列表、失败原因列表、切片数量统计等后台展示接口，可以使用 `arch-manage` 的只读 Mapper 查询。
2. 上传知识库文档、删除文档、触发重解析、启停文档等动作，必须调用 `arch-kb` Service。
3. 文件上传和 `sys_oss_file` 写入仍通过 `arch-infra` 文件能力完成。
4. `arch-manage` 只负责后台入口、权限校验、操作日志和页面 VO 组装，不直接改 `kb_document` 的核心状态。

### 4.4 子模块依赖方向

MVP 采用“单向分层依赖 + 少量明确的业务 Facade 调用”，不拆 `arch-resource-api`、`arch-kb-api` 这类额外 API 子模块。这样 POM 更简单，模块边界仍然清楚。

推荐依赖方向：

```text
arch-common
 -> 不依赖任何项目内模块

arch-infra
 -> arch-common
 -> 不依赖任何业务模块

arch-task
 -> arch-common
 -> 不依赖任何业务模块
 -> MVP 不依赖 arch-infra

业务模块，例如 arch-auth / arch-agent / arch-kb / arch-resource / arch-community
 -> arch-common
 -> arch-infra，按需
 -> arch-task，按需
 -> 少量调用其他业务模块的 Service / Facade

arch-manage
 -> arch-common
 -> arch-infra，按需
 -> arch-task，按需
 -> 需要被后台聚合的业务模块

arch-boot
 -> arch-common
 -> arch-infra
 -> arch-task
 -> 所有需要装配进同一个 Spring Boot 应用的业务模块
```

依赖图：

```text
                arch-boot
                   |
    ---------------------------------
    |       |       |       |       |
 arch-manage   arch-agent  arch-kb  arch-resource ...
    |              |        |        |
    |              |        |        |
    ---------> business Service / Facade
                   |
            ----------------
            |              |
        arch-task      arch-infra
            \            /
             \          /
              arch-common
```

落地规则：

1. `arch-common` 是最底层，只放统一响应、异常、分页、安全上下文、工具类、通用枚举，不依赖任何项目内模块。
2. `arch-infra` 只放 Redis Stream、MinIO、Coze、Chroma、LLM、`sys_agent_config`、`sys_oss_file` 等基础设施能力，不依赖业务模块。
3. `arch-task` 只管理 `async_generation_task` 和任务状态，不依赖具体业务模块，也不主动投递 Redis Stream。
4. 业务模块不能依赖 `arch-boot`，也不要依赖 `arch-manage`。
5. 业务模块之间可以少量调用，但只能调用对方公开的 `Service` 或 `Facade`，不能跨模块调用 Mapper。
6. `arch-manage` 可以依赖业务模块，因为它是后台聚合入口；但只能调用业务 Service / Facade，不直接写业务表、不接管业务状态流转。
7. `arch-boot` 只负责启动、配置装配、全局 Controller 和模块扫描，可以依赖所有模块；其他模块不能反向依赖 `arch-boot`。
8. 如果出现双向依赖，优先把公共 DTO / 枚举下沉到 `arch-common`，或把跨模块用例收敛到拥有该业务的模块 Facade；MVP 不额外拆 `xxx-api` 子模块。

POM 依赖建议：

| 模块 | 建议依赖 |
| --- | --- |
| `arch-common` | 无项目内依赖 |
| `arch-infra` | `arch-common` |
| `arch-task` | `arch-common` |
| `arch-auth` | `arch-common`、按需 `arch-infra` |
| `arch-profile` | `arch-common`、按需 `arch-infra` / `arch-task` |
| `arch-agent` | `arch-common`、`arch-infra`、按需 `arch-task` |
| `arch-explore` | `arch-common`、`arch-infra`、`arch-task`、按需 `arch-resource` |
| `arch-knowledge` | `arch-common` |
| `arch-kb` | `arch-common`、`arch-infra`、`arch-task` |
| `arch-resource` | `arch-common`、`arch-infra`、`arch-task` |
| `arch-study` | `arch-common`、`arch-infra`、`arch-task`、按需 `arch-resource` / `arch-knowledge` |
| `arch-practice` | `arch-common`、`arch-infra`、`arch-task`、按需 `arch-study` |
| `arch-community` | `arch-common`、`arch-infra`、`arch-task` |
| `arch-manage` | `arch-common`、`arch-infra`、`arch-task`、后台需要聚合的业务模块 |
| `arch-boot` | 所有需要启动装配的模块 |

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
 -> 调用 arch-task 创建 async_generation_task
 -> 调用 arch-infra 投递 Redis Stream 资源生成任务
 -> 后端 Agent 适配层调用 Coze Nebula Bot / Workflow 生成结构化资源草案
 -> RAG 检索 Chroma / kb_chunk_record 获取依据
 -> 后端 Agent 适配层调用 Coze Horizon Bot / Workflow 做事实核查和质量评分
 -> arch-resource 写 learning_resource + learning_resource_version
 -> arch-infra 写 sys_oss_file 或生成文件 URL
 -> 更新任务状态与资源质量状态
```

关键点：

1. 生成过程走 Redis Stream 异步任务，前端通过进度查询或 SSE 获取状态。
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
 -> 调用 arch-infra 文件能力写 sys_oss_file
 -> 创建 kb_document
 -> Redis Stream: queue:kb:parse
 -> 解析文档并切片，写 kb_chunk_record
 -> Redis Stream: queue:kb:embed
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
 -> 投递 Redis Stream: queue:community:moderation
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

### 6.3 Redis Stream

保存异步任务消息，队列命名直接采用根目录老版本数据库设计中的 Redis Stream 规则：

1. `queue:kb:parse`：知识库文档解析任务。
2. `queue:kb:embed`：知识切片向量化任务。
3. 资源生成、学习计划、社区审核等后续任务按同一旧版命名规范扩展为 `queue:{domain}:{action}`。

幂等键优先复用 `arch-task` 管理的 `async_generation_task.idempotent_key`，也可结合业务目标 ID；重试次数落在 `async_generation_task.retry_count` 或对应业务状态字段。

MVP 阶段消费者与 API 在同一个 Spring Boot 应用内运行：

```text
arch-boot
arch-infra/redis-stream-client
arch-task/service/AsyncTaskService
arch-kb/consumer/KbParseConsumer
arch-resource/consumer/ResourceGenerateConsumer
arch-community/consumer/CommunityModerationConsumer
```

Redis Stream 采用“基础能力集中，业务 consumer 分散”的策略：

1. `arch-infra` 只提供 Stream 客户端、消息序列化、`XADD`、`XREADGROUP` 封装、ACK、重试、死信工具。
2. `arch-infra` 不写文档解析、资源生成、社区审核等业务逻辑。
3. `arch-kb`、`arch-resource`、`arch-community` 等模块各自放 consumer，消费自己的队列，并调用 `arch-task` 更新任务状态。
4. 谁消费任务，谁负责幂等、事务、状态更新和错误处理。
5. 具体业务结果仍调用本模块或目标业务模块 Service 落库，例如资源生成完成后调用 `arch-resource`。

后续如生成任务压力变大，可将某个业务模块的 consumer 拆成独立 worker 进程，API 应用不需要改业务接口。

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

用途：保存 Coze Bot / Workflow 路由配置，不保存 Token 等敏感凭证。该表归 `arch-infra` 管理，`arch-agent` 只读取或引用配置，不直接维护配置表。

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

`async_generation_task` 归 `arch-task` 管理，可复用为资源生成、计划生成、知识库解析、社区审核、评测报告等任务表。建议增补：

| 字段 | 说明 |
| --- | --- |
| `task_type` | `resource_generate`、`resource_audit`、`plan_generate`、`kb_parse`、`kb_embed`、`community_moderation` |
| `external_run_id` | Coze workflow run ID 或第三方任务 ID |
| `retry_count` | 当前重试次数 |
| `max_retry` | 最大重试次数 |
| `started_at` / `finished_at` | 任务开始和结束时间 |

### 7.4 后台 RBAC 与操作日志

后台 RBAC 表归 `arch-manage` 管理。MVP 建议包含：

| 表 | 说明 |
| --- | --- |
| `sys_admin` | 后台管理员账号 |
| `sys_role` | 后台角色 |
| `sys_permission` | 菜单、按钮、API 权限，使用 `type` 字段区分 |
| `sys_admin_role` | 管理员与角色关系 |
| `sys_role_permission` | 角色与权限关系 |
| `sys_operation_log` | 后台关键操作日志 |

权限码建议按后台动作命名：

| 场景 | 示例权限码 |
| --- | --- |
| 知识库上传 | `manage:kb:upload` |
| 知识库重解析 | `manage:kb:reparse` |
| 社区审核通过 | `manage:community:approve` |
| 社区内容驳回 | `manage:community:reject` |
| Agent 配置修改 | `manage:agent-config:update` |
| 用户禁用 / 解封 | `manage:user:disable`、`manage:user:enable` |

`sys_operation_log` 至少记录操作人、操作模块、操作类型、目标对象 ID、请求路径、请求参数摘要、操作结果、失败原因、IP、User-Agent、操作时间。

### 7.5 社区审核状态

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

### 7.6 代码题 AI 评阅

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
3. Redis 密码或云 Redis 凭证。
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

### 8.3 后台 RBAC MVP 边界

后台 RBAC 做到“能登录、能分角色、能控制菜单、能控制关键按钮 / API、能追溯关键操作”即可。

MVP 必做：

1. 管理员账号、角色、权限关系。
2. 后台菜单树接口，根据管理员权限返回可见菜单。
3. 关键按钮权限码，例如上传、删除、重解析、审核、下架、启停配置、禁用用户。
4. 后端接口权限校验，避免只做前端展示控制。
5. 高风险写操作日志。

MVP 不做组织架构、数据范围权限、字段级权限、多级审批流和复杂审计报表。

### 8.4 鉴权建议

1. Access Token 短有效期，Refresh Token 长有效期。
2. Redis 保存 token/session 映射和强制下线状态。
3. 用户封禁、管理员禁用时立即删除对应 Redis 登录态。
4. `X-Request-Id` 贯穿日志、Redis Stream 消息、Coze 调用和错误响应。

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
2. `V1` 已包含 `sys_agent_config`、Coze 会话映射字段、Redis Stream 任务追踪字段、后台 RBAC 表、操作日志表、社区审核状态字段。
3. `V2+` 仅用于后续新需求，不再把已讨论确认的结构拆成多段补丁。

### 9.2 Docker Compose 本地环境

本地开发使用 Docker Compose 启动依赖：

| 服务 | 默认端口 | 说明 |
| --- | ---: | --- |
| MySQL | `3306` | 主业务数据库 |
| Redis | `6379` | 登录态、缓存、短期上下文 |
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
| 队列 | `RedisStreamTaskPublisher` | `InMemoryTaskPublisher` |
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
    mode: redis-stream
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

后端不在 Controller 中直接调用 Coze，而是在对应模块的 Service 中通过 `arch-infra` 提供的统一客户端封装：

```text
arch-agent/controller
 -> arch-agent/service
 -> arch-infra/coze/CozeAgentClient
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
| `agentCode` | bot_id / workflow_id | `sys_agent_config` |
| `async_generation_task.id` | workflow run id | `arch-task` 管理的 `async_generation_task.external_run_id` |

### 10.2 Coze Bot / Workflow 映射

| Agent | Coze 形态 | 后端调用场景 | 交互方式 |
| --- | --- | --- | --- |
| Ava | Bot | 情绪激励、学习提醒、陪伴式对话 | SSE 流式 |
| Sage | Bot | 苏格拉底式答疑、伴读提问、效率建议 | SSE 流式 |
| Coach | Bot + Workflow | 练习提示可用 Bot；阶段评测报告建议 Workflow | 对话流式 / 任务异步 |
| Old money | Bot | 行业经验、反方辩论、方案挑战 | SSE 流式 |
| Nebula | Workflow | 探索学习方向、生成结构化资源、生成思维导图 | Redis Stream 异步 + 状态查询 |
| Horizon | Workflow | 事实核查、资源质检、文章润色 / 审核辅助 | Redis Stream 异步 / 同步短调用 |
| Strict | Workflow | 生成学习计划、调整学习路径、拆分学习任务 | Redis Stream 异步 + 状态查询 |

确认新增独立配置表 `sys_agent_config`，归 `arch-infra` 管理，不要把 Coze 配置塞进 `sys_agent_prompt`。`sys_agent_prompt` 更适合保存提示词版本，`sys_agent_config` 保存运行时连接和路由配置。

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
| Nebula 资源生成 | Redis Stream 异步任务 | 产物复杂，可能耗时长 |
| Horizon 资源质检 | Redis Stream 异步任务 | 可重试、可追踪、可审计 |
| Strict 计划生成 | Redis Stream 异步任务 | 需要结合画像、资源、日历，适合状态跟踪 |
| 知识库解析 / 向量化 | Redis Stream 异步任务 | 文件处理与 embedding 耗时长 |
| 社区文章审核 | Redis Stream 异步任务 | 不阻塞发布动作，保留审核状态 |

异步任务统一通过 `arch-task` 写入 `async_generation_task` 或在少数场景复用业务状态表，前端通过状态查询接口获取进度。需要即时进度的页面可额外通过 SSE 订阅任务事件。

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
6. Controller 按 Maven 业务子模块分组，内部调用本模块 Service。
7. DTO / VO 层负责入参与响应字段，不让前端直接依赖数据库实体和数据库枚举。

## 12. 推荐项目目录

```text
arch
├─ pom.xml
├─ arch-boot
│  └─ src/main/java/com/hopeandsparks/boot
│     ├─ HopeAndSparksApplication.java
│     └─ controller
│        ├─ HealthController.java
│        ├─ AppInfoController.java
│        └─ UploadController.java
├─ arch-common
│  └─ src/main/java/com/hopeandsparks/common
│     ├─ config
│     ├─ exception
│     ├─ response
│     ├─ security
│     ├─ enums
│     └─ utils
├─ arch-infra
│  └─ src/main/java/com/hopeandsparks/infra
│     ├─ redis
│     ├─ file
│     ├─ minio
│     ├─ chroma
│     ├─ coze
│     └─ llm
├─ arch-task
├─ arch-auth
├─ arch-profile
├─ arch-agent
├─ arch-explore
├─ arch-knowledge
├─ arch-resource
├─ arch-study
├─ arch-practice
├─ arch-kb
├─ arch-community
└─ arch-manage
```

每个业务子模块内部统一采用以下结构：

```text
arch-{module}
└─ src/main
   ├─ java/com/hopeandsparks/{module}
   │  ├─ controller
   │  ├─ service
   │  │  └─ impl
   │  ├─ mapper
   │  ├─ consumer
   │  ├─ entity
   │  ├─ dto
   │  ├─ vo
   │  ├─ enums
   │  └─ config
   └─ resources
      └─ mapper
```

顶层 `arch` 只作为父 POM 聚合工程，不放业务源码。业务源码全部进入对应子模块。

`arch-boot` 可以放少量全局 Controller：

1. `HealthController`：应用健康检查，不归任何业务模块。
2. `AppInfoController`：版本、构建信息、运行环境等应用级信息。
3. `UploadController`：统一上传入口，内部只调用 `arch-infra` 文件能力，不承载业务文件归属规则。

除上述全局入口外，业务 Controller 不放在 `arch-boot`，必须放入对应业务子模块。

## 13. MVP 优先级

### P0：必须优先打通

1. 登录注册、当前用户、画像引导。
2. Agent 会话、流式响应、基础记忆落库。
3. Nebula 探索、资源列表、资源详情、资源生成进度。
4. 学习计划、学习拓扑、知识点资源网络。
5. 练习提交、评测报告、薄弱点反哺。
6. Manage 管理员登录、菜单权限、关键按钮 / API 权限、操作日志。
7. Manage 知识库文档上传、解析状态、争议工单。
8. 社区文章列表、详情、发布、评论、点赞、收藏。
9. 社区文章 / 评论异步审核状态流转。

### P1：增强体验

1. 视频学习进度、文档阅读进度、Sage 伴读。
2. 技能树点亮、复习提醒、通知。
3. 设备安全、缓存清理、用户设置。

### P2：二期扩展

1. 私信、群聊、WebSocket 完整消息系统。
2. 周挑战、成就、商城。
3. 用户行为风控、内容 AI 审核后台。

## 14. 工程落地顺序

在 `arch.md` 讨论完成前，只维护架构文档，不生成或改造工程代码。文档确认后再按以下顺序推进：

1. 父 POM 聚合工程：确认 `arch` 顶层只保留父 `pom.xml` 和子模块目录。
2. 按 4.4 的依赖方向配置各子模块 POM，先保证无循环依赖、可聚合编译。
3. 基础模块：生成 `arch-common`、`arch-infra`、`arch-task`、`arch-boot`。
4. 第一批业务模块：生成 `arch-auth`、`arch-agent`、`arch-kb`、`arch-resource`、`arch-community`、`arch-manage`。
5. 第二批业务模块：生成 `arch-profile`、`arch-explore`、`arch-knowledge`、`arch-study`、`arch-practice`。
6. 基础设施接入：MySQL、Redis Stream、MinIO、Chroma、Coze 客户端适配。
7. 鉴权与用户：前台用户注册登录、用户 token、后台管理员 token、RBAC 拦截器。
8. Agent 基础链路：`arch-infra` 管理 `sys_agent_config`，`arch-agent` 管理会话、消息、Prompt、SSE 流式转发。
9. 文件与知识库：`arch-infra` 提供 MinIO 上传与 `sys_oss_file`，`arch-task` 负责解析任务状态，`arch-kb` 负责 `kb_document` 和解析业务状态。
10. 资源、学习计划、练习、社区和 Manage 后台按模块逐步填实；生成资源统一通过 `arch-resource` 落库。

## 15. Spring Boot 骨架首轮范围

下一阶段生成工程骨架时，首轮不直接铺满所有业务接口，而是先做“父 POM 可聚合 + 应用可启动 + 依赖可替换”的基础版本。

首轮建议包含：

1. `arch/pom.xml` 改为父 POM，packaging 为 `pom`，统一管理 Spring Boot、MyBatis-Plus、MinIO 等版本。
2. 所有子模块 POM 按 4.4 配置依赖方向：`arch-common` 最底层，`arch-boot` 依赖所有需要装配的模块，业务模块不反向依赖 `arch-boot` / `arch-manage`。
3. `arch-common`：统一响应 `ApiResponse`、分页响应、全局异常、基础枚举、工具类。
4. `arch-infra`：Redis Stream、MinIO 文件能力、Chroma、Coze、LLM 的配置属性类与客户端接口。
5. `arch-task`：`async_generation_task` 实体、Mapper、Service、任务状态枚举和基础查询接口。
6. `arch-boot`：Spring Boot 启动类、`application.yml`、`application-local.yml`、`application-mock.yml`、健康检查、应用信息、上传入口。
7. 业务模块先生成空结构：`controller/service/mapper/consumer/entity/dto/vo`，不急着铺完整 CRUD。
8. MyBatis-Plus、Flyway、MySQL 连接配置。
9. Spring Security 基础配置和 JWT 工具占位。
10. Docker Compose 文件。
11. Flyway `V1` 迁移脚本骨架，内容基于讨论后的完整 `hope_sparks.sql`。

首轮暂不做：

1. 完整注册登录。
2. 完整 RBAC 管理页面和复杂权限配置。
3. 完整资源生成和知识库解析。
4. 真实 Coze / MinIO / Chroma 调用。
5. 真实业务 CRUD 全量生成。

这样第一版可以很快启动，后续每个 Maven 子模块再按 `controller/service/mapper` 结构逐步填实。
