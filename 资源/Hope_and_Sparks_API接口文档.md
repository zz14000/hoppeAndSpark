# Hope and Sparks API 接口文档

> 版本：v1.0  
> 适用范围：Spark 前台端、Manage 管理端、AI Agent 协同服务  
> 依据：前端原型页面、现有 mock/API 调用、项目说明文档  

## 1. 文档说明

本文档用于约定 Hope and Sparks 前端与后端的 API 交互方式。当前前端原型已经覆盖了登录后首页、画像构建、Agent 对话、资源库、学习拓扑、日历计划、技能树、实战测验、社区、个人中心、消息通知、设置等功能；说明文档还补充了 Manage 管理端、知识库、AI 审核、争议内容复核、Prompt 配置等后台能力。

本文档中的接口路径统一以 `/api/v1` 作为基础路径。

## 2. 全局规范

### 2.1 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization: Bearer <token>` | 是，登录后接口必填 | 用户登录凭证 |
| `Content-Type: application/json` | 是 | JSON 请求体 |
| `X-Client-Type` | 否 | 客户端类型：`web`、`android`、`ios` |
| `X-Request-Id` | 否 | 请求链路追踪 ID |

### 2.2 统一响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "requestId": "req_20260505_xxxx"
}
```

### 2.3 分页结构

```json
{
  "page": 1,
  "pageSize": 10,
  "total": 120,
  "list": []
}
```

### 2.4 常用状态码

| code | 含义 |
| --- | --- |
| `200` | 成功 |
| `400` | 请求参数错误 |
| `401` | 未登录或 token 失效 |
| `403` | 无权限 |
| `404` | 资源不存在 |
| `409` | 当前状态冲突 |
| `422` | 内容审核或业务校验未通过 |
| `429` | 请求过于频繁 |
| `500` | 服务端异常 |

### 2.5 数据库落库与字段命名约定

本接口文档已结合根目录 `数据库设计-正式版.md` 与 `hope_sparks.sql` 对齐。接口层统一使用前端友好的 camelCase 字段名，数据库层使用 snake_case 字段名；若接口字段说明与 SQL 表结构冲突，以 `hope_sparks.sql` 为准。响应中默认不暴露 `is_deleted`、内部审核字段、向量库内部 ID、密码散列、管理员操作来源等敏感或实现细节字段。

| 约定项 | API 层 | 数据库层 |
| --- | --- | --- |
| 主键 ID | 统一按字符串返回，如 `"10001"`；语义编码另放在 `nodeCode`、`knowledgeKey` 等字段 | MySQL 主键多为 `bigint`，业务编码字段如 `knowledge_node.node_code` 可作为前端稳定标识 |
| 路径参数兼容 | 正式实现优先传数据库主键字符串；原型中的 `node_bst`、`exercise_set_xxx` 等语义 ID 可作为兼容输入 | 节点类参数先按 `id` 查询，非数字时按 `node_code` 查询；其他语义 ID 需在种子数据或映射层转换为真实主键 |
| 时间字段 | `createdAt`、`updatedAt`、`startedAt`、`finishedAt` | `created_at`、`updated_at`、`started_at`、`finished_at` |
| 软删除 | 列表和详情默认只返回未删除数据 | `is_deleted = 0` |
| 用户身份 | 通过 `Authorization` 解析当前用户 | `sys_user.id`，关联到各业务表 `user_id` |
| 文件访问 | 返回可访问 `url`、`mimeType`、`size` | `sys_oss_file` 保存对象存储元信息 |
| 向量检索 | API 返回知识片段摘要、引用来源 | MySQL `kb_chunk_record` 保存切片元数据，Chroma 保存向量 |

核心模块与数据库表对应关系如下：

| API 模块 | 主要表 | 说明 |
| --- | --- | --- |
| 用户与认证 | `sys_user`、`user_profile`、`user_settings`、`user_login_session` | 用户账号、画像、设置、登录会话 |
| 课程/拓扑/资源 | `course`、`knowledge_node`、`knowledge_node_relation`、`learning_resource`、`learning_resource_version`、`sys_oss_file`、`user_resource_favorite` | 学习拓扑节点、节点关系、资源网络图、资源版本与收藏 |
| 学习计划/日历 | `study_plan`、`study_task`、`user_knowledge_progress`、`user_learning_record` | 学习计划、每日任务、知识点进度、学习记录 |
| 练习与测试 | `practice_set`、`practice_set_question`、`question_bank`、`user_question_record`、`evaluation_report` | 练习卷、测试卷、题库、作答记录、评估报告 |
| Agent 智能体 | `agent_chat_session`、`agent_chat_message`、`agent_graph_thread`、`agent_graph_checkpoint`、`agent_memory`、`agent_memory_summary`、`async_generation_task`、`sys_agent_prompt` | 对话、LangGraph 线程/检查点、长期记忆、异步生成任务、Prompt 配置 |
| 知识库/RAG | `kb_document`、`kb_chunk_record`、`kb_parse_strategy`、`sys_oss_file`，以及 Chroma 集合 | 文档解析、切片、召回、引用与向量存储 |
| 社区与文章 | `blog_post`、`blog_comment`、`blog_like`、`blog_favorite`、`blog_view_log` | 文章、评论、点赞、收藏、浏览记录 |
| 消息/私信/群聊 | `im_conversation`、`im_message`、`im_message_read`、`user_friend`、`sys_chat_group`、`sys_group_member` | 会话、消息、已读、好友、群组成员 |
| 管理端/权限/工单 | `sys_admin`、`sys_role`、`sys_admin_menu`、`sys_admin_resource`、`sys_admin_resource_category`、`sys_admin_role`、`sys_role_admin_menu`、`sys_role_admin_resource`、`feedback_ticket`、`sys_operation_log` | 管理员、角色权限、争议工单、操作日志 |
| 成就/商城/挑战 | `sys_achievement_badge`、`user_achievement`、`mall_item`、`user_asset`、`weekly_challenge`、`challenge_submission` | 徽章、资产、商城物品、周挑战与提交记录 |

常用字段映射：

| API 字段 | 数据库字段 | 备注 |
| --- | --- | --- |
| `userId` | `sys_user.id` | 当前登录用户或目标用户 |
| `username` / `account` | `sys_user.username` | 账号名 |
| `adminId` | `sys_admin.id` | 管理端管理员 |
| `adminAccount` | `sys_admin.username` | 管理端登录账号 |
| `nickname` | `sys_user.nickname` | 昵称 |
| `sessionToken` | `user_login_session.session_token` | 登录会话令牌标识，用于刷新 access token 或下线会话 |
| `avatar` | `sys_user.avatar_url` | 头像地址 |
| `profile.learningDomain` | `user_profile.major_domain` | 画像中的学习方向 |
| `profile.stage` | `user_profile.grade_level` | 学段/年级 |
| `knowledgeBaseLevel` | `user_profile.knowledge_base_level` | 基础水平 |
| `selfDiscipline` | `user_profile.self_discipline` | 自律程度 |
| `courseId` | `course.id` | 课程 ID |
| `nodeId` | `knowledge_node.id`，兼容 `knowledge_node.node_code` | 知识节点主键；原型语义 ID 按 `node_code` 解析 |
| `nodeCode` / `knowledgeKey` | `knowledge_node.node_code` | 前端可用作稳定知识点编码 |
| `resourceId` | `learning_resource.id` | 学习资源主键 |
| `resourceType` / `type` | `learning_resource.resource_type` | 详见 22.10 枚举映射 |
| `planId` | `study_plan.id` | 学习计划主键 |
| `taskId` | `study_task.id` | 学习任务主键 |
| `exerciseSetId` / `practiceSetId` | `practice_set.id` | 练习/测试集合主键 |
| `questionId` | `question_bank.id` | 题库题目主键 |
| `answerId` / `recordId` | `user_question_record.id` | 用户作答记录 |
| `conversationId` | `im_conversation.id` | 私信/群聊会话 |
| `messageId` | `im_message.id` 或 `agent_chat_message.id` | 根据业务上下文区分 |

## 3. 用户与认证

### 3.1 注册

`POST /api/v1/auth/register`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `username` | 是 | string | 登录账号，对应 sys_user.username |
| Body | `password` | 是 | string | 密码，服务端加密后写入 sys_user.password_hash |
| Body | `email` | 否 | string | 邮箱，对应 sys_user.email |
| Body | `nickname` | 否 | string | 昵称，对应 sys_user.nickname |

主要数据表：sys_user


请求体：

```json
{
  "username": "spark_1001",
  "email": "spark@example.com",
  "password": "12345678",
  "nickname": "一粒黑子"
}
```

响应：

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": "u_1001"
  }
}
```

### 3.2 登录

`POST /api/v1/auth/login`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `account` | 是 | string | 邮箱或用户名，对应 sys_user.email/sys_user.username |
| Body | `password` | 是 | string | 密码，服务端加密后写入 sys_user.password_hash |
| Body | `clientType` | 否 | string | 客户端类型：web/android/ios |
| Body | `deviceId` | 否 | string | 设备标识，对应 user_login_session.device_id |

主要数据表：sys_user、user_login_session；登录响应中的 onboarded 可通过是否存在 user_profile 判断


请求体：

```json
{
  "account": "spark@example.com",
  "password": "12345678"
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "jwt_access_token",
    "sessionToken": "session_token",
    "expiresIn": 7200,
    "user": {
      "id": "u_1001",
      "nickname": "一粒黑子",
      "avatar": "https://cdn.example.com/avatar.png",
      "onboarded": true
    }
  }
}
```

### 3.3 刷新 token

`POST /api/v1/auth/refresh`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `sessionToken` | 是 | string | 登录会话令牌，对应 user_login_session.session_token |

主要数据表：sys_user、user_login_session


请求体：

```json
{
  "sessionToken": "session_token"
}
```

### 3.4 退出登录

`POST /api/v1/auth/logout`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：user_login_session


### 3.5 找回密码

`POST /api/v1/auth/password/reset-request`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `email` | 是 | string | 邮箱，对应 sys_user.email |

主要数据表：sys_user


请求体：

```json
{
  "email": "spark@example.com"
}
```

### 3.6 重置密码

`POST /api/v1/auth/password/reset-confirm`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `resetToken` | 是 | string | 重置或校验凭证 |
| Body | `newPassword` | 是 | string | 新密码，服务端加密后写入 sys_user.password_hash |

主要数据表：sys_user


请求体：

```json
{
  "resetToken": "reset_token",
  "newPassword": "new_password"
}
```

## 4. Spark 画像与首次引导

### 4.1 获取画像引导问题

`GET /api/v1/onboarding/questions`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：user_profile、agent_memory


响应：

```json
{
  "code": 200,
  "data": [
    {
      "id": "grade_stage",
      "question": "你当前的年级或阶段是什么？",
      "type": "single_choice",
      "options": ["高中生", "大学生", "职场新人", "资深开发者"]
    },
    {
      "id": "learning_domain",
      "question": "接下来主要想专注的学习领域是什么？",
      "type": "single_choice",
      "options": ["前端开发", "后端架构", "人工智能", "数据结构与算法"]
    }
  ]
}
```

### 4.2 提交单轮画像回答

`POST /api/v1/onboarding/answers`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `questionId` | 是 | string | 题目 ID，对应 question_bank.id |
| Body | `answer` | 否 | object | 用户答案，按题型可为字符串、数组或对象 |
| Body | `answerText` | 否 | string | 文本答案 |
| Body | `selectedOptions` | 否 | array<string> | 选择题选项数组 |

主要数据表：user_profile、agent_memory


请求体：

```json
{
  "sessionId": "ob_1001",
  "questionId": "learning_domain",
  "answer": "数据结构与算法"
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "sessionId": "ob_1001",
    "nextQuestion": {
      "id": "knowledge_base",
      "question": "你目前具备怎样的基础知识？",
      "options": ["零基础小白", "了解一些概念", "写过简单 Demo", "有实战经验"]
    },
    "finished": false
  }
}
```

### 4.3 生成 Spark 画像

`POST /api/v1/onboarding/complete`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：user_profile、agent_memory


请求体：

```json
{
  "sessionId": "ob_1001"
}
```

响应：

```json
{
  "code": 200,
  "message": "画像生成成功",
  "data": {
    "profileId": "sp_1001",
    "summary": "大学生，目标为数据结构与算法进阶，基础中等，需要适度提醒。",
    "learningDomain": "数据结构与算法",
    "stage": "大学生",
    "disciplineLevel": "medium",
    "recommendedPlanId": "plan_1001"
  }
}
```

### 4.4 重新构建画像

`POST /api/v1/spark-profile/rebuild`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `reason` | 是 | string | 操作原因 |
| Body | `answers` | 否 | array<object> | 批量作答内容 |

主要数据表：user_profile、agent_memory


请求体：

```json
{
  "reason": "推荐内容不准确",
  "keepHistory": true
}
```

## 5. 用户资料与个人中心

### 5.1 获取当前用户

`GET /api/v1/user/current`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


响应：

```json
{
  "code": 200,
  "data": {
    "id": "u_1001",
    "nickname": "一粒黑子",
    "avatar": "https://cdn.example.com/avatar.png",
    "bio": "永远保持对技术的好奇心。",
    "location": "上海",
    "company": "Spark 开源社区",
    "github": "github.com/heizi",
    "stats": {
      "followers": 2400,
      "likes": 12000,
      "articles": 15
    },
    "skills": [
      {
        "name": "前端工程化",
        "level": 4
      }
    ],
    "progress": 68
  }
}
```

### 5.2 获取指定用户主页

`GET /api/v1/users/{userId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `userId` | 是 | string | 用户 ID，对应 sys_user.id |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


### 5.3 更新个人资料

`PUT /api/v1/user/profile`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `nickname` | 否 | string | 昵称，对应 sys_user.nickname |
| Body | `avatar` | 否 | string | 头像地址，对应 sys_user.avatar_url |
| Body | `bio` | 否 | string | 个人简介，对应 user_profile.bio |
| Body | `learningDomain` | 否 | string | 学习方向，对应 user_profile.major_domain |
| Body | `gradeLevel` | 否 | string | 学段/年级，对应 user_profile.grade_level |
| Body | `knowledgeBaseLevel` | 否 | string | 基础水平，对应 user_profile.knowledge_base_level |
| Body | `selfDiscipline` | 否 | string | 自律程度，对应 user_profile.self_discipline |
| Body | `interests` | 否 | array<string> | 兴趣标签 |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


请求体：

```json
{
  "nickname": "一粒黑子",
  "bio": "永远保持对技术的好奇心。",
  "stage": "大学生",
  "location": "上海",
  "github": "github.com/heizi"
}
```

### 5.4 获取学习数据统计

`GET /api/v1/user/learning-stats`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


响应：

```json
{
  "code": 200,
  "data": {
    "totalStudyHours": 1248,
    "streakDays": 42,
    "resourceAdoptionRate": 78,
    "generatedResourceCount": 152,
    "communityRankPercent": 85,
    "plans": [
      {
        "id": "plan_1001",
        "title": "100 天算法刷题冲刺计划",
        "currentStage": "动态规划 Day 45",
        "finishedCount": 120,
        "totalCount": 300,
        "progress": 45
      }
    ]
  }
}
```

### 5.5 获取我的收藏

`GET /api/v1/user/collections?type=all&page=1&pageSize=10`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `type` | 否 | string | 类型筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


### 5.6 收藏或取消收藏

`POST /api/v1/user/collections`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `targetType` | 是 | string | 目标类型 |
| Body | `targetId` | 是 | string | 目标 ID |
| Body | `action` | 否 | string | 动作类型 |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


请求体：

```json
{
  "targetType": "resource",
  "targetId": "res_1001"
}
```

`DELETE /api/v1/user/collections/{collectionId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `collectionId` | 是 | string | 收藏记录 ID，对应收藏/资源收藏表主键 |

主要数据表：sys_user、user_profile、user_settings、user_resource_favorite、blog_favorite


## 6. Agent 智能体

### 6.1 获取智能体列表

`GET /api/v1/agents`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：agent_chat_session、agent_chat_message、agent_graph_thread、agent_memory、async_generation_task


响应：

```json
{
  "code": 200,
  "data": [
    {
      "id": "ava",
      "name": "Ava",
      "role": "潜意识唤醒与激励",
      "tags": ["情绪干预", "学习动力"],
      "icon": "ava.png",
      "welcomeMessage": "Spark 你好，我们一鼓作气拿下这个知识点。"
    },
    {
      "id": "sage",
      "name": "Sage",
      "role": "苏格拉底式启发体系",
      "tags": ["层层反问", "启发式引导"]
    }
  ]
}
```

### 6.2 创建对话会话

`POST /api/v1/agent-sessions`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `agentId` | 是 | string | 智能体 ID，如 nebula/coach/sage |
| Body | `title` | 否 | string | 标题 |
| Body | `contextNodeId` | 否 | string | 上下文知识点 ID，对应 knowledge_node.id |
| Body | `contextResourceId` | 否 | string | 上下文资源 ID，对应 learning_resource.id |
| Body | `metadata` | 否 | object | 扩展元数据 |

主要数据表：agent_chat_session、agent_chat_message、agent_graph_thread、agent_memory、async_generation_task


请求体：

```json
{
  "agentId": "sage",
  "source": "agent_chat",
  "context": {
    "resourceId": "res_1001",
    "nodeId": "node_bst"
  }
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "sessionId": "ags_1001",
    "agentId": "sage",
    "title": "BST 递归边界讨论"
  }
}
```

### 6.3 发送消息给智能体

`POST /api/v1/agent-sessions/{sessionId}/messages`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `sessionId` | 是 | string | Agent 会话 ID，对应 agent_chat_session.id |
| Body | `content` | 是 | string | 正文内容 |
| Body | `contentType` | 否 | string | 内容类型 |
| Body | `attachments` | 否 | array<object> | 附件列表，对应 sys_oss_file |
| Body | `stream` | 否 | boolean | 是否使用流式响应 |

主要数据表：agent_chat_session、agent_chat_message、agent_graph_thread、agent_memory、async_generation_task


请求体：

```json
{
  "content": "为什么验证 BST 不能只比较左右子节点？",
  "contentType": "text",
  "attachments": [
    {
      "type": "code",
      "language": "javascript",
      "content": "var isValidBST = function(root) {}"
    }
  ]
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "messageId": "msg_1001",
    "reply": {
      "role": "assistant",
      "content": "如果左子树中某个更深层节点大于根节点，会发生什么？请先从全局上下界约束思考。",
      "agentId": "sage"
    }
  }
}
```

### 6.4 获取会话历史

`GET /api/v1/agent-sessions/{sessionId}/messages?page=1&pageSize=30`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `sessionId` | 是 | string | Agent 会话 ID，对应 agent_chat_session.id |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：agent_chat_session、agent_chat_message、agent_graph_thread、agent_memory、async_generation_task


### 6.5 Agent 流式响应

`GET /api/v1/agent-sessions/{sessionId}/stream?messageId=msg_1001`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `sessionId` | 是 | string | Agent 会话 ID，对应 agent_chat_session.id |
| Query | `messageId` | 是 | string | 消息 ID，对应 agent_chat_message.id 或 im_message.id |

主要数据表：agent_chat_session、agent_chat_message、agent_graph_thread、agent_memory、async_generation_task


协议：Server-Sent Events。

事件示例：

```text
event: chunk
data: {"content":"如果我们从边界条件开始看，"}

event: done
data: {"messageId":"msg_1002"}
```

### 6.6 举报 AI 内容争议

`POST /api/v1/ai-disputes`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `targetType` | 是 | string | 目标类型 |
| Body | `targetId` | 是 | string | 目标 ID |
| Body | `issueType` | 是 | string | 问题类型，见 feedback_ticket.issue_type |
| Body | `description` | 否 | string | 问题描述 |
| Body | `evidence` | 否 | array<object> | 证据材料 |

主要数据表：agent_chat_session、agent_chat_message、agent_graph_thread、agent_memory、async_generation_task


请求体：

```json
{
  "targetType": "agent_message",
  "targetId": "msg_1002",
  "reason": "事实错误",
  "description": "这里关于 BST 的定义不准确。"
}
```

## 7. Nebula 探索与资源生成

### 7.1 调用 Nebula 全局探索

`POST /api/v1/explore`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `keyword` | 是 | string | 搜索关键词 |
| Body | `domain` | 否 | string | 学习/知识领域 |
| Body | `depth` | 否 | integer | depth |
| Body | `goals` | 否 | array<string> | 学习目标列表 |
| Body | `preferredResourceTypes` | 否 | array<string> | 偏好的资源类型 |

主要数据表：knowledge_node、knowledge_node_relation、learning_resource、async_generation_task、kb_chunk_record/Chroma


请求体：

```json
{
  "query": "如何解决微服务分布式事务？",
  "mode": "deep",
  "preferredTypes": ["document", "video", "exercise_set", "mindmap"]
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "exploreId": "exp_1001",
    "summary": "分布式事务的核心是跨服务状态一致性与性能之间的取舍。",
    "resources": [
      {
        "id": "res_1001",
        "type": "document",
        "title": "Seata 源码级原理解析"
      }
    ],
    "relatedNodes": ["CAP 定理", "BASE 理论", "RocketMQ 事务消息"]
  }
}
```

### 7.2 获取探索详情

`GET /api/v1/explore/{exploreId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exploreId` | 是 | string | 探索任务 ID，对应异步生成或探索记录 |

主要数据表：knowledge_node、knowledge_node_relation、learning_resource、async_generation_task、kb_chunk_record/Chroma


### 7.3 生成思维导图

`POST /api/v1/explore/{exploreId}/mindmap`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exploreId` | 是 | string | 探索任务 ID，对应异步生成或探索记录 |
| Body | `style` | 否 | string | 生成样式 |
| Body | `depth` | 否 | integer | depth |
| Body | `includeResources` | 否 | boolean | 是否包含资源节点 |

主要数据表：knowledge_node、knowledge_node_relation、learning_resource、async_generation_task、kb_chunk_record/Chroma


响应：

```json
{
  "code": 200,
  "data": {
    "mindmapId": "mm_1001",
    "nodes": [
      {
        "id": "n_cap",
        "label": "CAP 定理",
        "parentId": null
      }
    ],
    "edges": []
  }
}
```

### 7.4 获取知识关联图谱

`GET /api/v1/knowledge-graph?keyword=分布式事务&depth=2`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `keyword` | 否 | string | 搜索关键词 |
| Query | `depth` | 否 | integer | 图谱展开深度 |

主要数据表：knowledge_node、knowledge_node_relation、learning_resource、async_generation_task、kb_chunk_record/Chroma


## 8. 学习资源库

### 8.1 获取资源列表

`GET /api/v1/resources?type=all&keyword=BST&page=1&pageSize=12`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `type` | 否 | string | 类型筛选，具体枚举见接口文档数据字典 |
| Query | `keyword` | 否 | string | 搜索关键词 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


查询参数：

| 参数 | 说明 |
| --- | --- |
| `type` | `all`、`video`、`document`、`exercise_set`、`reading`、`code_case`、`case` |
| `keyword` | 搜索关键词 |
| `verified` | 是否 Horizon 认证 |
| `planId` | 所属学习计划 |

响应：

```json
{
  "code": 200,
  "data": {
    "page": 1,
    "pageSize": 12,
    "total": 3,
    "list": [
      {
        "id": "res_1001",
        "type": "video",
        "title": "BST 概念入门",
        "description": "结合动画演示理解查找、插入、删除。",
        "duration": 750,
        "progress": 65,
        "verifiedBy": "horizon",
        "tags": ["BST", "数据结构"]
      }
    ]
  }
}
```

### 8.2 获取资源详情

`GET /api/v1/resources/{resourceId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `resourceId` | 是 | string | 学习资源 ID，对应 learning_resource.id |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


通用资源详情接口。资源网络图节点跳转时可以先请求该接口，由后端返回 `detailRoute` 和 `detailApi`，前端再进入视频、文档、练习题、拓展阅读或代码案例详情页。

响应：

```json
{
  "code": 200,
  "data": {
    "id": "res_video_bst_intro",
    "type": "video",
    "title": "BST 概念入门",
    "summary": "通过动画理解二叉搜索树的有序性。",
    "knowledgeNodeId": "node_bst",
    "planId": "plan_1001",
    "status": "completed",
    "progress": 100,
    "verifiedBy": "horizon",
    "detailRoute": "/app/video?resourceId=res_video_bst_intro",
    "detailApi": "/api/v1/videos/res_video_bst_intro",
    "actions": {
      "continueLearning": true,
      "collect": true,
      "export": false,
      "feedback": true
    }
  }
}
```

### 8.3 获取拓展阅读详情

`GET /api/v1/readings/{readingId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `readingId` | 是 | string | 拓展阅读资源 ID，对应 learning_resource.id |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


响应：

```json
{
  "code": 200,
  "data": {
    "id": "reading_btree_reason",
    "title": "为什么数据库使用 B 树？",
    "content": "Markdown 正文",
    "estimatedMinutes": 15,
    "source": "nebula",
    "verifiedBy": "horizon",
    "relatedKnowledgeNodes": ["node_bst", "node_btree"]
  }
}
```

### 8.4 获取代码案例详情

`GET /api/v1/code-cases/{caseId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `caseId` | 是 | string | 代码案例资源 ID，对应 learning_resource.id |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


响应：

```json
{
  "code": 200,
  "data": {
    "id": "code_bst_java",
    "title": "BST 完整 Java 实现",
    "language": "java",
    "description": "包含插入、删除、查找和中序遍历的完整实现。",
    "files": [
      {
        "fileName": "BinarySearchTree.java",
        "content": "public class BinarySearchTree {}"
      }
    ],
    "unitTests": [
      {
        "fileName": "BinarySearchTreeTest.java",
        "content": "class BinarySearchTreeTest {}"
      }
    ]
  }
}
```

### 8.5 更新资源学习进度

`PUT /api/v1/resources/{resourceId}/progress`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `resourceId` | 是 | string | 学习资源 ID，对应 learning_resource.id |
| Body | `progressPercent` | 是 | integer | 学习进度百分比 |
| Body | `positionSeconds` | 否 | integer | 播放位置，单位秒 |
| Body | `currentSection` | 否 | string | 当前章节/段落 |
| Body | `completed` | 否 | boolean | 是否完成 |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


请求体：

```json
{
  "progress": 65,
  "lastPosition": 261,
  "status": "learning"
}
```

### 8.6 导出资源

`POST /api/v1/resources/{resourceId}/export`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `resourceId` | 是 | string | 学习资源 ID，对应 learning_resource.id |
| Body | `format` | 是 | string | 导出格式 |
| Body | `includeNotes` | 否 | boolean | 是否包含笔记 |
| Body | `includeMindmap` | 否 | boolean | 是否包含思维导图 |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


请求体：

```json
{
  "format": "pdf"
}
```

### 8.7 对资源进行质量反馈

`POST /api/v1/resources/{resourceId}/feedback`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `resourceId` | 是 | string | 学习资源 ID，对应 learning_resource.id |
| Body | `issueType` | 是 | string | 问题类型，见 feedback_ticket.issue_type |
| Body | `rating` | 否 | integer | 评分 |
| Body | `content` | 是 | string | 正文内容 |
| Body | `evidence` | 否 | array<object> | 证据材料 |

主要数据表：learning_resource、learning_resource_version、sys_oss_file、user_learning_record、feedback_ticket


请求体：

```json
{
  "rating": 4,
  "issueType": "outdated",
  "content": "第 2 节示例代码版本较旧。"
}
```

## 9. 视频学习

### 9.1 获取视频详情

`GET /api/v1/videos/{videoId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `videoId` | 是 | string | 视频资源 ID，对应 learning_resource.id |

主要数据表：learning_resource、sys_oss_file、user_learning_record


响应：

```json
{
  "code": 200,
  "data": {
    "id": "vid_1001",
    "title": "BST 二叉搜索树概念入门",
    "duration": 750,
    "playUrl": "https://cdn.example.com/videos/bst.mp4",
    "coverUrl": "https://cdn.example.com/covers/bst.png",
    "viewCount": 8200,
    "likeCount": 1200,
    "chapter": {
      "chapterNo": 6,
      "lessonNo": 1
    }
  }
}
```

### 9.2 获取视频选集

`GET /api/v1/videos/{videoId}/episodes`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `videoId` | 是 | string | 视频资源 ID，对应 learning_resource.id |

主要数据表：learning_resource、sys_oss_file、user_learning_record


### 9.3 上报播放进度

`PUT /api/v1/videos/{videoId}/watch-progress`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `videoId` | 是 | string | 视频资源 ID，对应 learning_resource.id |
| Body | `positionSeconds` | 是 | integer | 播放位置，单位秒 |
| Body | `durationSeconds` | 否 | integer | 耗时或资源总时长，单位秒 |
| Body | `progressPercent` | 是 | integer | 学习进度百分比 |
| Body | `completed` | 否 | boolean | 是否完成 |

主要数据表：learning_resource、sys_oss_file、user_learning_record


请求体：

```json
{
  "position": 261,
  "duration": 750,
  "completed": false
}
```

### 9.4 获取视频字幕与 AI 提示

`GET /api/v1/videos/{videoId}/transcripts`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `videoId` | 是 | string | 视频资源 ID，对应 learning_resource.id |

主要数据表：learning_resource、sys_oss_file、user_learning_record


## 10. 文档阅读

### 10.1 获取文档详情

`GET /api/v1/documents/{documentId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |

主要数据表：learning_resource、kb_document、kb_chunk_record、user_learning_record


### 10.2 获取文档目录

`GET /api/v1/documents/{documentId}/outline`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |

主要数据表：learning_resource、kb_document、kb_chunk_record、user_learning_record


### 10.3 更新阅读进度

`PUT /api/v1/documents/{documentId}/reading-progress`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |
| Body | `page` | 否 | integer | 页码 |
| Body | `scrollPercent` | 否 | integer | 阅读滚动百分比 |
| Body | `progressPercent` | 是 | integer | 学习进度百分比 |
| Body | `completed` | 否 | boolean | 是否完成 |

主要数据表：learning_resource、kb_document、kb_chunk_record、user_learning_record


请求体：

```json
{
  "progress": 35,
  "currentSectionId": "sec_1"
}
```

### 10.4 Sage 伴读提问

`POST /api/v1/documents/{documentId}/ask`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |
| Body | `content` | 是 | string | 正文内容 |
| Body | `selectedText` | 否 | string | 用户选中的文档片段 |
| Body | `page` | 否 | integer | 页码 |
| Body | `agentId` | 否 | string | 智能体 ID，如 nebula/coach/sage |

主要数据表：learning_resource、kb_document、kb_chunk_record、user_learning_record


请求体：

```json
{
  "question": "为什么多智能体通信要使用异步事件流？",
  "selectedText": "由于每个智能体的推理时间具有不确定性..."
}
```

## 11. 学习计划、日历与拓扑

### 11.1 获取学习计划

`GET /api/v1/learning-plans/current`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


响应：

```json
{
  "code": 200,
  "data": {
    "id": "plan_1001",
    "title": "数据结构与算法进阶深水区",
    "domain": "数据结构与算法",
    "estimatedHours": 45,
    "progress": 50,
    "createdBy": "strict"
  }
}
```

### 11.2 Strict 生成或调整学习计划

`POST /api/v1/learning-plans/generate`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `courseId` | 是 | string | 课程 ID，对应 course.id |
| Body | `goal` | 是 | string | 学习目标 |
| Body | `deadline` | 否 | string | 截止日期 |
| Body | `difficulty` | 否 | string | 难度 |
| Body | `dailyMinutes` | 否 | integer | 每日学习分钟数 |
| Body | `preferredTimeSlots` | 否 | array<string> | 偏好的学习时间段 |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


请求体：

```json
{
  "domain": "数据结构与算法",
  "goal": "准备求职面试",
  "dailyMinutes": 90,
  "deadline": "2026-06-30",
  "difficulty": "adaptive"
}
```

`PUT /api/v1/learning-plans/{planId}/adjust`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `planId` | 是 | string | 学习计划 ID，对应 study_plan.id |
| Body | `reason` | 是 | string | 操作原因 |
| Body | `strategy` | 是 | string | 计划调整策略 |
| Body | `taskIds` | 否 | array<string> | 学习任务 ID 列表，对应 study_task.id |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


请求体：

```json
{
  "reason": "进度超前",
  "strategy": "remove_redundant_review"
}
```

### 11.3 获取学习拓扑

`GET /api/v1/learning-plans/{planId}/topology`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `planId` | 是 | string | 学习计划 ID，对应 study_plan.id |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


数据来源：以 `study_plan` 校验当前用户计划归属，读取 `study_task.node_id` 形成用户计划内节点范围；节点详情来自 `knowledge_node`，节点连线来自 `knowledge_node_relation`，节点学习状态与进度来自 `user_knowledge_progress`。返回给前端的 `knowledgeKey` 对应 `knowledge_node.node_code`。

响应：

```json
{
  "code": 200,
  "data": {
    "planId": "plan_1001",
    "nodes": [
      {
        "id": "node_basic",
        "name": "基础数据结构",
        "desc": "数组、链表、栈与队列",
        "knowledgeKey": "basic_data_structure",
        "status": "completed",
        "progress": 100,
        "prerequisites": [],
        "resourceNetworkUrl": "/app/resource-network?planId=plan_1001&nodeId=node_basic",
        "resourceNetworkApi": "/api/v1/learning-plans/plan_1001/topology/nodes/node_basic/resource-network",
        "clickAction": {
          "type": "navigate",
          "target": "knowledge_resource_network",
          "params": {
            "planId": "plan_1001",
            "nodeId": "node_basic"
          }
        }
      },
      {
        "id": "node_tree",
        "name": "树结构进阶",
        "knowledgeKey": "tree_advanced",
        "status": "in_progress",
        "progress": 60,
        "prerequisites": ["node_basic"],
        "resourceNetworkUrl": "/app/resource-network?planId=plan_1001&nodeId=node_tree",
        "resourceNetworkApi": "/api/v1/learning-plans/plan_1001/topology/nodes/node_tree/resource-network",
        "clickAction": {
          "type": "navigate",
          "target": "knowledge_resource_network",
          "params": {
            "planId": "plan_1001",
            "nodeId": "node_tree"
          }
        }
      }
    ],
    "edges": [
      {
        "from": "node_basic",
        "to": "node_tree"
      }
    ]
  }
}
```

说明：学习拓扑图中的每个 `nodes[]` 节点都必须支持点击跳转。前端点击节点后，使用 `nodeId` 请求该知识点对应的学习资源网络图。

### 11.4 获取知识点资源网络图

`GET /api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resource-network`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `planId` | 是 | string | 学习计划 ID，对应 study_plan.id |
| Path | `nodeId` | 是 | string | 知识点节点 ID，对应 knowledge_node.id，兼容 knowledge_node.node_code |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


数据来源：中心节点与相关知识点来自 `knowledge_node`、`knowledge_node_relation`；视频、文档、思维导图、代码案例、课件等资源来自 `learning_resource` 与 `learning_resource_version`；资源文件地址来自 `sys_oss_file`；练习/测试入口优先关联 `practice_set`、`practice_set_question`、`question_bank`，并可在资源网络图中以 `target.type=exercise_set` 返回。前端展示使用 API 枚举，落库枚举按 22.10 转换。

用途：从学习计划拓扑图的某个知识点进入该知识点的资源网络图。资源网络图以当前知识点为中心，按视频动画、课程文档、练习题、拓展阅读、代码案例、相关知识点等类型展开。点击网络图中的资源节点时，根据 `target.type` 跳转到对应资源详情页；点击相关知识点节点时，继续跳转到该知识点的资源网络图。

响应：

```json
{
  "code": 200,
  "data": {
    "planId": "plan_1001",
    "nodeId": "node_bst",
    "knowledge": {
      "id": "node_bst",
      "name": "二叉搜索树",
      "englishName": "Binary Search Tree",
      "chapter": "第 6 章",
      "status": "in_progress",
      "progress": 65,
      "description": "左子树所有节点小于根节点，右子树所有节点大于根节点的二叉树。"
    },
    "stats": {
      "learnedResourceCount": 4,
      "totalResourceCount": 14,
      "studyDurationSeconds": 9300,
      "exerciseAccuracy": 82
    },
    "categories": [
      {
        "id": "cat_video",
        "type": "video",
        "name": "视频动画",
        "count": 5
      },
      {
        "id": "cat_document",
        "type": "document",
        "name": "课程文档",
        "count": 3
      },
      {
        "id": "cat_exercise",
        "type": "exercise_set",
        "name": "练习题",
        "count": 12
      },
      {
        "id": "cat_reading",
        "type": "reading",
        "name": "拓展阅读",
        "count": 4
      },
      {
        "id": "cat_code",
        "type": "code_case",
        "name": "代码案例",
        "count": 2
      },
      {
        "id": "cat_related",
        "type": "knowledge",
        "name": "相关知识点",
        "count": 4
      }
    ],
    "resourceNodes": [
      {
        "id": "res_video_bst_intro",
        "categoryId": "cat_video",
        "type": "video",
        "title": "BST 概念入门",
        "summary": "通过动画理解二叉搜索树的有序性。",
        "durationSeconds": 720,
        "status": "completed",
        "target": {
          "type": "video_detail",
          "resourceId": "res_video_bst_intro",
          "url": "/app/video?resourceId=res_video_bst_intro"
        }
      },
      {
        "id": "res_doc_bst_guide",
        "categoryId": "cat_document",
        "type": "document",
        "title": "BST 完全指南",
        "summary": "BST 定义、复杂度、插入删除流程。",
        "target": {
          "type": "document_detail",
          "resourceId": "res_doc_bst_guide",
          "url": "/app/document?resourceId=res_doc_bst_guide"
        }
      },
      {
        "id": "exercise_set_bst_after_class",
        "categoryId": "cat_exercise",
        "type": "exercise_set",
        "title": "BST 课后练习",
        "questionCount": 10,
        "questionTypes": ["single_choice", "multiple_choice", "fill_blank", "short_answer", "code"],
        "target": {
          "type": "exercise_detail",
          "exerciseSetId": "exercise_set_bst_after_class",
          "url": "/app/exercises/exercise_set_bst_after_class"
        }
      },
      {
        "id": "reading_btree_reason",
        "categoryId": "cat_reading",
        "type": "reading",
        "title": "为什么数据库使用 B 树？",
        "target": {
          "type": "reading_detail",
          "resourceId": "reading_btree_reason",
          "url": "/app/reading?resourceId=reading_btree_reason"
        }
      },
      {
        "id": "code_bst_java",
        "categoryId": "cat_code",
        "type": "code_case",
        "title": "BST 完整 Java 实现",
        "target": {
          "type": "code_case_detail",
          "resourceId": "code_bst_java",
          "url": "/app/code-case?resourceId=code_bst_java"
        }
      }
    ],
    "relatedKnowledgeNodes": [
      {
        "id": "node_tree",
        "name": "二叉树",
        "relation": "prerequisite",
        "target": {
          "type": "knowledge_resource_network",
          "planId": "plan_1001",
          "nodeId": "node_tree",
          "url": "/app/resource-network?planId=plan_1001&nodeId=node_tree"
        }
      },
      {
        "id": "node_balanced_tree",
        "name": "平衡树 AVL",
        "relation": "next",
        "target": {
          "type": "knowledge_resource_network",
          "planId": "plan_1001",
          "nodeId": "node_balanced_tree",
          "url": "/app/resource-network?planId=plan_1001&nodeId=node_balanced_tree"
        }
      }
    ],
    "edges": [
      {
        "from": "node_bst",
        "to": "cat_video",
        "type": "category"
      },
      {
        "from": "cat_video",
        "to": "res_video_bst_intro",
        "type": "resource"
      },
      {
        "from": "node_bst",
        "to": "node_balanced_tree",
        "type": "related_knowledge"
      }
    ],
    "aiSuggestion": [
      "先看《BST 概念入门》建立直觉",
      "阅读《BST 完全指南》补全理论",
      "完成《BST 课后练习》检验掌握度"
    ]
  }
}
```

### 11.5 获取知识点资源网络图列表视图

`GET /api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resources?type=all&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `planId` | 是 | string | 学习计划 ID，对应 study_plan.id |
| Path | `nodeId` | 是 | string | 知识点节点 ID，对应 knowledge_node.id，兼容 knowledge_node.node_code |
| Query | `type` | 否 | string | 类型筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


用途：资源网络图提供“网络视图/列表视图”切换。列表视图复用该接口，便于移动端或无画布场景展示。

查询参数：

| 参数 | 说明 |
| --- | --- |
| `type` | `all`、`video`、`document`、`exercise_set`、`reading`、`code_case` |
| `status` | `not_started`、`learning`、`completed` |
| `keyword` | 资源标题关键词 |

### 11.6 获取日历事件

`GET /api/v1/calendar/events?month=2026-05&view=week`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `month` | 否 | string | 月份，格式 YYYY-MM |
| Query | `view` | 否 | string | 日历视图类型：month/week/day |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


响应：

```json
{
  "code": 200,
  "data": [
    {
      "id": "e_1",
      "date": "2026-05-18",
      "time": "09:00",
      "title": "算法挑战",
      "desc": "排序与搜索专题突破",
      "type": "blue",
      "status": "pending",
      "source": "strict"
    }
  ]
}
```

### 11.7 新建日程

`POST /api/v1/calendar/events`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `title` | 是 | string | 标题 |
| Body | `startTime` | 是 | string | 开始时间 |
| Body | `endTime` | 是 | string | 结束时间 |
| Body | `type` | 是 | string | type |
| Body | `planId` | 否 | string | 学习计划 ID，对应 study_plan.id |
| Body | `taskId` | 否 | string | 学习任务 ID，对应 study_task.id |
| Body | `resourceId` | 否 | string | 学习资源 ID，对应 learning_resource.id |
| Body | `remark` | 否 | string | 备注 |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


请求体：

```json
{
  "title": "系统架构复习",
  "desc": "分布式高并发方案设计",
  "startTime": "2026-05-20T14:00:00+08:00",
  "endTime": "2026-05-20T16:00:00+08:00",
  "category": "system_architecture"
}
```

### 11.8 更新日程

`PUT /api/v1/calendar/events/{eventId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `eventId` | 是 | string | 日历事件/学习任务 ID，对应 study_task.id |
| Body | `title` | 否 | string | 标题 |
| Body | `startTime` | 否 | string | 开始时间 |
| Body | `endTime` | 否 | string | 结束时间 |
| Body | `type` | 否 | string | type |
| Body | `status` | 否 | string | status |
| Body | `remark` | 否 | string | 备注 |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


### 11.9 删除日程

`DELETE /api/v1/calendar/events/{eventId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `eventId` | 是 | string | 日历事件/学习任务 ID，对应 study_task.id |

主要数据表：study_plan、study_task、knowledge_node、knowledge_node_relation、user_knowledge_progress


## 12. 技能树

### 12.1 获取技能树

`GET /api/v1/skill-tree`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：knowledge_node、knowledge_node_relation、user_knowledge_progress、user_achievement


响应：

```json
{
  "code": 200,
  "data": {
    "totalProgress": 68,
    "nodes": [
      {
        "id": "skill_frontend",
        "name": "前端工程化",
        "level": 4,
        "status": "completed"
      },
      {
        "id": "skill_cloud_native",
        "name": "云原生部署",
        "level": 0,
        "status": "locked"
      }
    ]
  }
}
```

### 12.2 点亮技能节点

`POST /api/v1/skill-tree/nodes/{nodeId}/light-up`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `nodeId` | 是 | string | 知识点节点 ID，对应 knowledge_node.id，兼容 knowledge_node.node_code |
| Body | `evidenceType` | 否 | string | 点亮依据类型 |
| Body | `evidenceId` | 否 | string | 点亮依据 ID |
| Body | `score` | 否 | number | 得分 |
| Body | `remark` | 否 | string | 备注 |

主要数据表：knowledge_node、knowledge_node_relation、user_knowledge_progress、user_achievement


请求体：

```json
{
  "evidenceType": "exercise_passed",
  "evidenceId": "attempt_1001"
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "nodeId": "skill_cloud_native",
    "level": 1,
    "totalProgress": 73,
    "unlockedBadges": []
  }
}
```

## 13. 练习与测试

说明：练习与测试不只包含算法代码题，还包含单选题、多选题、填空题、简答题、代码题等多种题型。资源网络图里的“练习题”节点应跳转到练习套题详情页；点击填空题、简答题、代码题等需要完整作答空间的题目时，可进入独立答题界面。

### 13.1 获取练习/测试列表

`GET /api/v1/exercise-sets?planId=plan_1001&nodeId=node_bst&status=available&type=practice`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `planId` | 否 | string | 学习计划 ID，对应 study_plan.id |
| Query | `nodeId` | 否 | string | 知识点节点 ID，对应 knowledge_node.id，兼容 knowledge_node.node_code |
| Query | `status` | 否 | string | 状态筛选，具体枚举见接口文档数据字典 |
| Query | `type` | 否 | string | 类型筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


数据来源：练习/测试集合来自 `practice_set`，题目关联来自 `practice_set_question`，题目内容来自 `question_bank`，当前用户作答状态来自 `user_question_record`。当从学习计划进入时，`planId` 用 `study_plan` 校验归属，并可结合 `study_task.node_id`、`study_task.resource_id` 过滤当前计划中的练习任务。

查询参数：

| 参数 | 说明 |
| --- | --- |
| `planId` | 学习计划 ID |
| `nodeId` | 知识点节点 ID |
| `type` | `practice` 课后练习、`test` 阶段测试、`challenge` 实战挑战、`code_lab` 代码训练 |
| `status` | `available`、`in_progress`、`submitted`、`graded` |

响应：

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "exercise_set_bst_after_class",
        "title": "二叉搜索树章节课后练习",
        "type": "practice",
        "knowledgeNodeId": "node_bst",
        "questionCount": 10,
        "timeLimitSeconds": 3600,
        "answeredCount": 4,
        "flaggedCount": 2,
        "objectiveScore": 11,
        "totalScore": 35,
        "questionTypeStats": {
          "single_choice": 3,
          "multiple_choice": 1,
          "fill_blank": 3,
          "short_answer": 2,
          "code": 1
        }
      }
    ]
  }
}
```

兼容说明：原 `/api/v1/quizzes` 可作为 `type=challenge` 或 `type=code_lab` 的别名保留，但新实现建议统一使用 `exercise-sets`。

### 13.2 获取练习/测试详情

`GET /api/v1/exercise-sets/{exerciseSetId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


数据来源：详情页以 `practice_set.id` 为主键，返回的 `questions[]` 来自 `practice_set_question` 与 `question_bank`；`answeredCount`、`flaggedCount`、`answerStatus` 等用户维度字段来自 `user_question_record`。接口必须同时支持算法题和普通练习题，题型枚举按 22.10.3 映射。

响应：

```json
{
  "code": 200,
  "data": {
    "id": "exercise_set_bst_after_class",
    "title": "二叉搜索树章节课后练习",
    "type": "practice",
    "knowledgeNodeId": "node_bst",
    "chapter": "第 6 章",
    "timeLimitSeconds": 3600,
    "remainingSeconds": 2538,
    "questionCount": 10,
    "answeredCount": 4,
    "flaggedCount": 2,
    "questions": [
      {
        "id": "q_1",
        "number": 1,
        "type": "single_choice",
        "score": 2,
        "stem": "在二叉搜索树中，对于任意节点 N，下列说法正确的是？",
        "options": [
          {
            "key": "A",
            "content": "左子树所有节点值 < N，右子树所有节点值 > N"
          },
          {
            "key": "B",
            "content": "左孩子节点值 < N，右孩子节点值 > N"
          }
        ],
        "answerStatus": "answered",
        "flagged": false,
        "requiresDedicatedAnswerPage": false
      },
      {
        "id": "q_3",
        "number": 3,
        "type": "fill_blank",
        "score": 2,
        "stem": "BST 的中序遍历得到的序列是 ___ 的。",
        "blanks": [
          {
            "blankId": "b1",
            "label": "空 1"
          }
        ],
        "answerStatus": "answered",
        "flagged": true,
        "requiresDedicatedAnswerPage": true,
        "answerPageUrl": "/app/exercises/exercise_set_bst_after_class/questions/q_3/answer"
      },
      {
        "id": "q_5",
        "number": 5,
        "type": "short_answer",
        "score": 5,
        "stem": "请描述在 BST 中删除一个有左右两个子树的节点的完整流程。",
        "richText": true,
        "allowImageUpload": true,
        "answerStatus": "answered",
        "requiresDedicatedAnswerPage": true
      },
      {
        "id": "q_8",
        "number": 8,
        "type": "code",
        "score": 10,
        "stem": "实现 BST 的 insert(value) 方法。",
        "language": "javascript",
        "starterCode": "class BST { insert(value) {} }",
        "requiresDedicatedAnswerPage": true
      }
    ]
  }
}
```

### 13.3 获取单题答题详情

`GET /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |
| Path | `questionId` | 是 | string | 题目 ID，对应 question_bank.id |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


用途：进入“答题界面”时请求。适用于填空题、简答题、代码题等需要较大作答区域、富文本编辑、图片上传或代码编辑器的题型。

响应：

```json
{
  "code": 200,
  "data": {
    "exerciseSetId": "exercise_set_bst_after_class",
    "question": {
      "id": "q_5",
      "number": 5,
      "type": "short_answer",
      "score": 5,
      "stem": "请描述在 BST 中删除一个有左右两个子树的节点的完整流程。",
      "answerConfig": {
        "richText": true,
        "allowImageUpload": true,
        "maxImages": 4,
        "maxImageSizeMb": 5,
        "autoSaveIntervalSeconds": 30
      },
      "savedAnswer": {
        "answerId": "ans_q5",
        "content": "<p>删除节点 N 时，先找到中序后继...</p>",
        "attachments": [
          {
            "id": "file_1001",
            "type": "image",
            "url": "https://cdn.example.com/bst-delete.png"
          }
        ],
        "savedAt": "2026-05-05T10:24:00+08:00"
      }
    },
    "timer": {
      "remainingSeconds": 2538
    }
  }
}
```

### 13.4 保存单题答案

`PUT /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/answer`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |
| Path | `questionId` | 是 | string | 题目 ID，对应 question_bank.id |
| Body | `answer` | 是 | mixed | 用户答案，按题型可为字符串、数组或对象 |
| Body | `durationSeconds` | 否 | integer | 耗时或资源总时长，单位秒 |
| Body | `flagged` | 否 | boolean | 是否标记题目 |
| Body | `attachments` | 否 | array<object> | 附件列表，对应 sys_oss_file |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


请求体按题型区分：

```json
{
  "type": "single_choice",
  "selectedOptionKeys": ["A"],
  "flagged": false
}
```

```json
{
  "type": "fill_blank",
  "blanks": [
    {
      "blankId": "b1",
      "content": "递增有序"
    },
    {
      "blankId": "b2",
      "content": "O(n)"
    }
  ],
  "flagged": true
}
```

```json
{
  "type": "short_answer",
  "content": "<p>删除节点 N 时，先找到中序后继...</p>",
  "contentFormat": "html",
  "attachments": ["file_1001"],
  "flagged": false
}
```

```json
{
  "type": "code",
  "language": "javascript",
  "code": "class BST { insert(value) {} }",
  "flagged": false
}
```

响应：

```json
{
  "code": 200,
  "message": "答案已保存",
  "data": {
    "answerId": "ans_q5",
    "answerStatus": "answered",
    "savedAt": "2026-05-05T10:25:00+08:00",
    "answeredCount": 5
  }
}
```

### 13.5 标记或取消标记题目

`PUT /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/flag`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |
| Path | `questionId` | 是 | string | 题目 ID，对应 question_bank.id |
| Body | `flagged` | 是 | boolean | 是否标记题目 |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


请求体：

```json
{
  "flagged": true
}
```

### 13.6 请求 Coach 提示

`POST /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/hint`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |
| Path | `questionId` | 是 | string | 题目 ID，对应 question_bank.id |
| Body | `hintLevel` | 否 | integer | 提示等级 |
| Body | `currentAnswer` | 否 | mixed | 当前答案草稿 |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


请求体：

```json
{
  "currentCode": "var isValidBST = function(root) {}",
  "currentAnswer": "删除节点时使用中序后继替换。",
  "question": "我卡在删除节点的解释了"
}
```

### 13.7 提交练习/测试

`POST /api/v1/exercise-sets/{exerciseSetId}/submit`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |
| Body | `durationSeconds` | 否 | integer | 耗时或资源总时长，单位秒 |
| Body | `answers` | 否 | array<object> | 批量作答内容 |
| Body | `forceSubmit` | 否 | boolean | 是否强制提交 |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


数据落库：提交时写入或更新 `user_question_record`；客观题可即时计算 `is_correct` 与 `score`，主观题/代码题可先记录作答内容与评测状态，再由异步评测更新结果。整套练习完成后可生成 `evaluation_report`，用于报告页、错题复盘和学习计划动态调整。

请求体：

```json
{
  "forceSubmit": false
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "attemptId": "attempt_1001",
    "submitted": true,
    "usedSeconds": 1062,
    "answeredCount": 4,
    "unansweredCount": 6,
    "flaggedCount": 2,
    "objectiveScore": 11,
    "objectiveTotalScore": 17,
    "subjectiveStatus": "pending_review",
    "message": "系统已收到答卷，主观题等待批阅。"
  }
}
```

如果存在未作答题目且 `forceSubmit=false`，返回：

```json
{
  "code": 409,
  "message": "还有题目未作答，请确认是否继续提交。",
  "data": {
    "answeredCount": 4,
    "unansweredQuestionNumbers": [4, 6, 7, 8, 9, 10],
    "flaggedCount": 2
  }
}
```

### 13.8 提交代码题即时评测

`POST /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/code-run`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `exerciseSetId` | 是 | string | 练习/测试集合 ID，对应 practice_set.id |
| Path | `questionId` | 是 | string | 题目 ID，对应 question_bank.id |
| Body | `language` | 是 | string | 代码语言 |
| Body | `code` | 是 | string | 验证码或代码内容，按接口上下文区分 |
| Body | `input` | 否 | string | 自定义输入 |
| Body | `testcaseIds` | 否 | array<string> | 测试用例 ID 列表 |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


说明：代码题支持在整套练习提交前单独运行测试用例。此接口也可用于原“算法题沙盘”的即时评测。

请求体：

```json
{
  "language": "javascript",
  "code": "class BST { insert(value) {} }"
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "passed": true,
    "runtimeMs": 64,
    "memoryMb": 42,
    "testCases": [
      {
        "name": "insert [50,30,70]",
        "expected": "[30,50,70]",
        "actual": "[30,50,70]",
        "passed": true
      }
    ],
    "coachReview": "代码通过测试用例，可以继续补充边界情况。"
  }
}
```

### 13.9 获取练习/测试报告

`GET /api/v1/exercise-sets/attempts/{attemptId}/report`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `attemptId` | 是 | string | 作答记录/评估报告 ID，对应 user_question_record.id 或 evaluation_report.id |

主要数据表：practice_set、practice_set_question、question_bank、user_question_record、evaluation_report


## 14. 社区与文章

### 14.1 获取文章列表

`GET /api/v1/articles?page=1&pageSize=10&category=recommend&keyword=Vue3`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |
| Query | `category` | 否 | string | category |
| Query | `keyword` | 否 | string | 搜索关键词 |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


查询参数：

| 参数 | 说明 |
| --- | --- |
| `category` | `recommend`、`latest`、`qa`、`following` |
| `tag` | 标签 |
| `authorId` | 作者 ID |

### 14.2 获取文章详情

`GET /api/v1/articles/{articleId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


响应：

```json
{
  "code": 200,
  "data": {
    "id": "art_1001",
    "title": "手写一个简易版 Vue3 响应式系统",
    "content": "Markdown 正文",
    "author": {
      "id": "u_1001",
      "nickname": "一粒黑子",
      "avatar": "https://cdn.example.com/avatar.png"
    },
    "tags": ["Vue3", "前端架构"],
    "aiSummary": "本文还原 Vue3 Proxy 响应式原理。",
    "readCount": 1200,
    "likeCount": 342,
    "commentCount": 56,
    "collectCount": 128,
    "createdAt": "2026-05-04T10:24:00+08:00"
  }
}
```

### 14.3 发布文章

`POST /api/v1/articles`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `title` | 是 | string | 标题 |
| Body | `content` | 是 | string | 正文内容 |
| Body | `summary` | 否 | string | 摘要 |
| Body | `tags` | 否 | array<string> | 标签列表 |
| Body | `coverFileId` | 否 | string | 封面文件 ID，对应 sys_oss_file.id |
| Body | `visibility` | 否 | string | 可见性 |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


请求体：

```json
{
  "title": "前端状态管理最佳实践",
  "content": "Markdown 内容",
  "category": "frontend",
  "tags": ["前端架构", "Pinia"],
  "coverUrl": "https://cdn.example.com/cover.png",
  "draftId": "draft_1001"
}
```

### 14.4 保存草稿

`POST /api/v1/articles/drafts`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `draftId` | 否 | string | 草稿 ID |
| Body | `title` | 是 | string | 标题 |
| Body | `content` | 是 | string | 正文内容 |
| Body | `summary` | 否 | string | 摘要 |
| Body | `tags` | 否 | array<string> | 标签列表 |
| Body | `coverFileId` | 否 | string | 封面文件 ID，对应 sys_oss_file.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


请求体：

```json
{
  "draftId": "draft_1001",
  "title": "前端状态管理最佳实践",
  "content": "Markdown 内容",
  "tags": ["前端架构"]
}
```

### 14.5 Horizon 文章润色

`POST /api/v1/articles/polish`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `title` | 否 | string | 标题 |
| Body | `content` | 是 | string | 正文内容 |
| Body | `tone` | 否 | string | 润色语气 |
| Body | `agentId` | 否 | string | 智能体 ID，如 nebula/coach/sage |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


请求体：

```json
{
  "title": "前端状态管理最佳实践",
  "content": "Markdown 内容"
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "summary": "本文讨论前端架构中状态管理的最佳实践。",
    "suggestedTags": ["前端架构", "状态管理", "Pinia"],
    "suggestedTitle": "从 Vuex 到 Pinia：前端状态管理实践指南",
    "polishedContent": "润色后的 Markdown 内容",
    "riskLevel": "pass"
  }
}
```

### 14.6 点赞、收藏、关注

`POST /api/v1/articles/{articleId}/like`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


`DELETE /api/v1/articles/{articleId}/like`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


`POST /api/v1/articles/{articleId}/collect`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


`DELETE /api/v1/articles/{articleId}/collect`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


`POST /api/v1/users/{userId}/follow`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `userId` | 是 | string | 用户 ID，对应 sys_user.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


`DELETE /api/v1/users/{userId}/follow`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `userId` | 是 | string | 用户 ID，对应 sys_user.id |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


### 14.7 获取评论

`GET /api/v1/articles/{articleId}/comments?page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


### 14.8 发布评论

`POST /api/v1/articles/{articleId}/comments`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `articleId` | 是 | string | 文章 ID，对应 blog_post.id |
| Body | `content` | 是 | string | 正文内容 |
| Body | `parentId` | 否 | string | 父评论 ID，对应 blog_comment.id |
| Body | `attachments` | 否 | array<object> | 附件列表，对应 sys_oss_file |

主要数据表：blog_post、blog_comment、blog_like、blog_favorite、blog_view_log


请求体：

```json
{
  "content": "写得很清楚，WeakMap 的部分很有帮助。",
  "parentId": null
}
```

## 15. 消息、私信与群聊

### 15.1 获取会话列表

`GET /api/v1/chats?page=1&pageSize=20&keyword=架构`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |
| Query | `keyword` | 否 | string | 搜索关键词 |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


响应：

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "chat_1001",
        "type": "group",
        "name": "千星奇域技术讨论群",
        "avatar": "群",
        "memberCount": 267,
        "lastMsg": "这是最新的原型代码。",
        "lastMsgAt": "2026-05-05T17:26:00+08:00",
        "unreadCount": 3
      }
    ]
  }
}
```

### 15.2 获取聊天记录

`GET /api/v1/chats/{chatId}/messages?before=msg_1009&pageSize=30`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `chatId` | 是 | string | 聊天会话 ID，对应 im_conversation.id |
| Query | `before` | 否 | string | 游标消息 ID，查询该消息之前的记录 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


### 15.3 发送消息

`POST /api/v1/chats/{chatId}/messages`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `chatId` | 是 | string | 聊天会话 ID，对应 im_conversation.id |
| Body | `content` | 是 | string | 正文内容 |
| Body | `messageType` | 否 | string | 消息类型 |
| Body | `attachments` | 否 | array<object> | 附件列表，对应 sys_oss_file |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


请求体：

```json
{
  "content": "这是最新的原型代码，可以嵌套进去测试。",
  "contentType": "text",
  "attachments": []
}
```

### 15.4 创建私聊

`POST /api/v1/chats/private`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `targetUserId` | 是 | string | 目标用户 ID，对应 sys_user.id |
| Body | `initialMessage` | 否 | string | 创建会话时的首条消息 |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


请求体：

```json
{
  "targetUserId": "u_1002"
}
```

### 15.5 创建群聊

`POST /api/v1/chats/groups`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `name` | 是 | string | 名称 |
| Body | `memberIds` | 是 | array<string> | 成员用户 ID 列表 |
| Body | `avatar` | 否 | string | 头像地址，对应 sys_user.avatar_url |
| Body | `announcement` | 否 | string | 群公告 |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


请求体：

```json
{
  "name": "算法冲刺小组",
  "memberIds": ["u_1001", "u_1002"]
}
```

### 15.6 好友申请

`POST /api/v1/friend-requests`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `targetUserId` | 是 | string | 目标用户 ID，对应 sys_user.id |
| Body | `message` | 否 | string | message |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


请求体：

```json
{
  "targetUserId": "u_1002",
  "message": "一起学习算法吗？"
}
```

`PUT /api/v1/friend-requests/{requestId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `requestId` | 是 | string | 好友申请 ID，对应 user_friend.id 或申请记录 |
| Body | `action` | 是 | string | 动作类型 |
| Body | `remark` | 否 | string | 备注 |

主要数据表：im_conversation、im_message、im_message_read、user_friend、sys_chat_group、sys_group_member


请求体：

```json
{
  "action": "accept"
}
```

## 16. 通知

### 16.1 获取通知列表

`GET /api/v1/notifications?type=agent&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `type` | 否 | string | 类型筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：通知推送服务、user_settings、im_message_read


查询参数：

| 参数 | 说明 |
| --- | --- |
| `type` | `agent`、`community`、`system` |
| `unreadOnly` | 只看未读 |

### 16.2 标记通知已读

`PUT /api/v1/notifications/{notificationId}/read`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `notificationId` | 是 | string | 通知 ID |

主要数据表：通知推送服务、user_settings、im_message_read


全部已读：

`PUT /api/v1/notifications/read-all`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：通知推送服务、user_settings、im_message_read


### 16.3 删除通知

`DELETE /api/v1/notifications/{notificationId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `notificationId` | 是 | string | 通知 ID |

主要数据表：通知推送服务、user_settings、im_message_read


### 16.4 通知动作回调

`POST /api/v1/notifications/{notificationId}/actions`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `notificationId` | 是 | string | 通知 ID |
| Body | `action` | 是 | string | 动作类型 |
| Body | `payload` | 否 | object | 动作扩展参数 |

主要数据表：通知推送服务、user_settings、im_message_read


请求体：

```json
{
  "action": "go_exercise"
}
```

## 17. 设置

### 17.1 获取设置

`GET /api/v1/settings`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：user_settings、user_profile


响应：

```json
{
  "code": 200,
  "data": {
    "general": {
      "language": "zh-CN",
      "autoStart": true
    },
    "agent": {
      "sageGuideLevel": 8,
      "coachPressureMode": true,
      "avaInterventionFrequency": "high"
    },
    "theme": {
      "mode": "light",
      "animation": true
    },
    "notification": {
      "agent": true,
      "community": true,
      "system": true
    },
    "privacy": {
      "profileVisible": true,
      "learningStatsVisible": false
    }
  }
}
```

### 17.2 更新设置

`PUT /api/v1/settings`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `theme` | 否 | string | 主题 |
| Body | `language` | 否 | string | 代码语言 |
| Body | `notification` | 否 | object | 通知设置 |
| Body | `privacy` | 否 | object | 隐私设置 |
| Body | `learningPreference` | 否 | object | 学习偏好 |

主要数据表：user_settings、user_profile


请求体：

```json
{
  "agent": {
    "sageGuideLevel": 9,
    "coachPressureMode": true,
    "avaInterventionFrequency": "medium"
  },
  "theme": {
    "mode": "dark",
    "animation": true
  }
}
```

### 17.3 清理缓存

`POST /api/v1/settings/cache/clear`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `cacheTypes` | 否 | array<string> | 需要清理的缓存类型 |

主要数据表：user_settings、user_profile


## 18. 设备与安全

### 18.1 获取登录设备

`GET /api/v1/user/devices`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：user_login_session、sys_user


### 18.2 下线指定设备

`DELETE /api/v1/user/devices/{sessionId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `sessionId` | 是 | string | 登录会话 ID，对应 user_login_session.id |

主要数据表：user_login_session、sys_user


### 18.3 修改密码

`PUT /api/v1/user/password`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `oldPassword` | 是 | string | 旧密码 |
| Body | `newPassword` | 是 | string | 新密码 |

主要数据表：user_login_session、sys_user


请求体：

```json
{
  "oldPassword": "old_password",
  "newPassword": "new_password"
}
```

### 18.4 绑定或更换邮箱

`PUT /api/v1/user/email`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `email` | 是 | string | 邮箱，对应 sys_user.email |
| Body | `code` | 是 | string | 验证码或代码内容，按接口上下文区分 |
| Body | `password` | 否 | string | 密码，服务端加密后写入 sys_user.password_hash |

主要数据表：user_login_session、sys_user


## 19. 文件上传

### 19.1 获取上传凭证

`POST /api/v1/uploads/token`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `fileName` | 是 | string | 文件名 |
| Body | `mimeType` | 是 | string | 文件 MIME 类型 |
| Body | `size` | 是 | integer | 文件大小，单位字节 |
| Body | `bizType` | 是 | string | 文件业务类型 |
| Body | `checksum` | 否 | string | 文件校验值 |

主要数据表：sys_oss_file


请求体：

```json
{
  "fileName": "cover.png",
  "contentType": "image/png",
  "scene": "article_cover"
}
```

响应：

```json
{
  "code": 200,
  "data": {
    "uploadUrl": "https://oss.example.com/upload",
    "fileUrl": "https://cdn.example.com/cover.png",
    "expiresIn": 600
  }
}
```

### 19.2 提交上传完成

`POST /api/v1/uploads/complete`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `fileKey` | 是 | string | 对象存储文件 Key |
| Body | `fileName` | 是 | string | 文件名 |
| Body | `mimeType` | 是 | string | 文件 MIME 类型 |
| Body | `size` | 是 | integer | 文件大小，单位字节 |
| Body | `bizType` | 是 | string | 文件业务类型 |
| Body | `etag` | 否 | string | 对象存储 ETag |

主要数据表：sys_oss_file


请求体：

```json
{
  "fileUrl": "https://cdn.example.com/cover.png",
  "scene": "article_cover"
}
```

## 20. Manage 管理端

### 20.1 管理端登录

`POST /api/v1/manage/auth/login`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `account` | 是 | string | 管理员用户名，对应 sys_admin.username |
| Body | `password` | 是 | string | 密码，服务端加密后写入 sys_admin.password_hash |
| Body | `captcha` | 否 | string | 验证码 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.2 数据看板

`GET /api/v1/manage/dashboard/overview?startDate=2026-05-01&endDate=2026-05-31`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `startDate` | 否 | string | 开始日期 |
| Query | `endDate` | 否 | string | 结束日期 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


响应：

```json
{
  "code": 200,
  "data": {
    "activeSparkCount": 12048,
    "avgStudyHours": 2.6,
    "domainDistribution": [
      {
        "domain": "数据结构与算法",
        "count": 3200,
        "percent": 26.6
      }
    ],
    "weeklyTaskCompletionRate": 78.5,
    "resourceGeneratedCount": 48210
  }
}
```

### 20.3 用户管理列表

`GET /api/v1/manage/users?keyword=黑子&status=active&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `keyword` | 否 | string | 搜索关键词 |
| Query | `status` | 否 | string | 状态筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.4 用户封禁、解封与警告

`POST /api/v1/manage/users/{userId}/actions`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `userId` | 是 | string | 用户 ID，对应 sys_user.id |
| Body | `action` | 是 | string | 动作类型 |
| Body | `reason` | 是 | string | 操作原因 |
| Body | `durationDays` | 否 | integer | 处理持续天数 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


请求体：

```json
{
  "action": "ban",
  "reason": "违规发布敏感内容",
  "durationHours": 168
}
```

`action` 可选：`warn`、`ban`、`unban`、`grant_permission`、`revoke_permission`。

### 20.5 查看用户学习轨迹

`GET /api/v1/manage/users/{userId}/learning-trace`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `userId` | 是 | string | 用户 ID，对应 sys_user.id |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


响应：

```json
{
  "code": 200,
  "data": {
    "userId": "u_1001",
    "skillProgress": [],
    "nodeStayTime": [],
    "coachScores": [],
    "recentEvents": []
  }
}
```

### 20.6 知识库文档列表

`GET /api/v1/manage/knowledge-base/documents?keyword=算法&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `keyword` | 否 | string | 搜索关键词 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.7 上传知识库文档

`POST /api/v1/manage/knowledge-base/documents`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Body | `title` | 是 | string | 标题 |
| Body | `sourceType` | 是 | string | 文档来源类型 |
| Body | `docType` | 是 | string | 文档类型 |
| Body | `fileId` | 否 | string | 文件 ID，对应 sys_oss_file.id |
| Body | `kbDomain` | 是 | string | 知识库领域 |
| Body | `parseStrategyId` | 否 | string | 解析策略 ID，对应 kb_parse_strategy.id |
| Body | `tags` | 否 | array<string> | 标签列表 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


请求体：

```json
{
  "title": "红黑树权威教材",
  "domain": "数据结构与算法",
  "fileUrl": "https://cdn.example.com/rbtree.pdf",
  "source": "teacher_upload"
}
```

### 20.8 更新知识库文档

`PUT /api/v1/manage/knowledge-base/documents/{documentId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |
| Body | `title` | 否 | string | 标题 |
| Body | `kbDomain` | 否 | string | 知识库领域 |
| Body | `parseStrategyId` | 否 | string | 解析策略 ID，对应 kb_parse_strategy.id |
| Body | `status` | 否 | string | status |
| Body | `tags` | 否 | array<string> | 标签列表 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.9 删除知识库文档

`DELETE /api/v1/manage/knowledge-base/documents/{documentId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.10 文档解析入库状态

`GET /api/v1/manage/knowledge-base/documents/{documentId}/parse-status`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `documentId` | 是 | string | 文档 ID，对应 learning_resource.id 或 kb_document.id |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


响应：

```json
{
  "code": 200,
  "data": {
    "status": "parsed",
    "chunkCount": 128,
    "vectorized": true,
    "errorMessage": null
  }
}
```

### 20.11 争议内容工单列表

`GET /api/v1/manage/ai-disputes?status=pending&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `status` | 否 | string | 状态筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.12 处理争议内容

`PUT /api/v1/manage/ai-disputes/{disputeId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `disputeId` | 是 | string | 争议工单 ID，对应 feedback_ticket.id |
| Body | `status` | 是 | string | status |
| Body | `result` | 否 | string | 处理结果 |
| Body | `remark` | 否 | string | 备注 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


请求体：

```json
{
  "status": "resolved",
  "reviewResult": "ai_error",
  "note": "知识库中 BST 定义补充不完整，已修正。",
  "correctedContent": "新的正确解释"
}
```

### 20.13 资源存储监控

`GET /api/v1/manage/storage/overview`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


响应：

```json
{
  "code": 200,
  "data": {
    "totalGb": 5000,
    "usedGb": 1280,
    "resourceCount": 48210,
    "redundantResourceCount": 1320
  }
}
```

### 20.14 管理历史生成资源

`GET /api/v1/manage/resources?type=document&qualityStatus=low&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `type` | 否 | string | 类型筛选，具体枚举见接口文档数据字典 |
| Query | `qualityStatus` | 否 | string | 资源质量状态筛选 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


`DELETE /api/v1/manage/resources/{resourceId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `resourceId` | 是 | string | 学习资源 ID，对应 learning_resource.id |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.15 Prompt 配置列表

`GET /api/v1/manage/agent-prompts`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| 无 | - | - | - | 无参数 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.16 更新 Prompt 配置

`PUT /api/v1/manage/agent-prompts/{promptId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `promptId` | 是 | string | Prompt 配置 ID，对应 sys_agent_prompt.id |
| Body | `promptName` | 否 | string | Prompt 名称 |
| Body | `promptContent` | 是 | string | Prompt 内容 |
| Body | `status` | 否 | string | status |
| Body | `version` | 否 | string | 版本号 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


请求体：

```json
{
  "agentId": "sage",
  "systemPrompt": "你是一位苏格拉底式启发导师...",
  "temperature": 0.7,
  "maxTokens": 2048,
  "enabled": true
}
```

### 20.17 内容 AI 审核列表

`GET /api/v1/manage/moderation/content?status=risk&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `status` | 否 | string | 状态筛选，具体枚举见接口文档数据字典 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.18 处理内容审核结果

`PUT /api/v1/manage/moderation/content/{recordId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `recordId` | 是 | string | 审核记录 ID |
| Body | `status` | 是 | string | status |
| Body | `reason` | 否 | string | 操作原因 |
| Body | `action` | 是 | string | 动作类型 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


请求体：

```json
{
  "action": "block",
  "reason": "包含违规内容"
}
```

### 20.19 用户行为风控预警

`GET /api/v1/manage/moderation/behavior-alerts?level=high&page=1&pageSize=20`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Query | `level` | 否 | string | 风险等级筛选 |
| Query | `page` | 否 | integer | 页码，从 1 开始 |
| Query | `pageSize` | 否 | integer | 每页数量 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


### 20.20 处理行为预警

`PUT /api/v1/manage/moderation/behavior-alerts/{alertId}`

参数设计：

| 位置 | 参数名 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| Path | `alertId` | 是 | string | 行为预警 ID |
| Body | `status` | 是 | string | status |
| Body | `remark` | 否 | string | 备注 |
| Body | `action` | 否 | string | 动作类型 |

主要数据表：sys_admin、sys_role、sys_admin_menu、sys_admin_resource、sys_admin_resource_category、sys_admin_role、sys_role_admin_menu、sys_role_admin_resource、feedback_ticket、kb_document、sys_operation_log、sys_agent_prompt


请求体：

```json
{
  "status": "confirmed",
  "action": "warn",
  "note": "疑似脚本刷分，已警告。"
}
```

## 21. WebSocket

### 21.1 私信与群聊实时通道

连接地址：

`wss://api.example.com/ws/v1/chats?token=<accessToken>`

客户端发送：

```json
{
  "type": "chat.message.send",
  "payload": {
    "chatId": "chat_1001",
    "content": "你好",
    "contentType": "text"
  }
}
```

服务端推送：

```json
{
  "type": "chat.message.received",
  "payload": {
    "chatId": "chat_1001",
    "messageId": "msg_1001",
    "senderId": "u_1002",
    "content": "你好",
    "createdAt": "2026-05-05T17:26:00+08:00"
  }
}
```

### 21.2 通知实时推送

```json
{
  "type": "notification.created",
  "payload": {
    "id": "n_1001",
    "type": "strict",
    "title": "Strict 调度提醒",
    "content": "本周学习进度落后 15%。"
  }
}
```

## 22. 核心数据字典

### 22.1 智能体 agentId

| 值 | 说明 |
| --- | --- |
| `nebula` | 多智能体协同内容生成引擎 |
| `horizon` | 内容质量控制与事实核查智能体 |
| `strict` | 学习计划与调度智能体 |
| `ava` | 激励与情绪干预智能体 |
| `sage` | 苏格拉底式启发智能体 |
| `coach` | 实战测验与评估智能体 |
| `oldmoney` | 行业经验、甲方模拟与辩论智能体 |

### 22.2 资源类型 resource.type

| 值 | 说明 |
| --- | --- |
| `video` | 视频动画 |
| `document` | 图文文档 |
| `exercise_set` | 练习题或测试套题 |
| `question` | 单个题目 |
| `code_case` | 代码案例 |
| `case` | 实操案例与源码，兼容旧值 |
| `reading` | 拓展阅读 |
| `mindmap` | 思维导图 |
| `audio` | 音频 |

### 22.3 练习/测试类型 exerciseSet.type

| 值 | 说明 |
| --- | --- |
| `practice` | 课后练习 |
| `test` | 阶段测试 |
| `challenge` | Coach 实战挑战 |
| `code_lab` | 代码训练 |

### 22.4 题型 question.type

| 值 | 说明 |
| --- | --- |
| `single_choice` | 单选题 |
| `multiple_choice` | 多选题 |
| `fill_blank` | 填空题 |
| `short_answer` | 简答题，支持富文本和图片上传 |
| `code` | 代码题，支持代码编辑器和测试用例运行 |

### 22.5 题目作答状态 answerStatus

| 值 | 说明 |
| --- | --- |
| `not_answered` | 未作答 |
| `answered` | 已作答 |
| `flagged` | 已标记 |
| `auto_saved` | 已自动保存 |
| `submitted` | 已提交 |
| `graded` | 已批阅 |

### 22.6 学习节点状态

| 值 | 说明 |
| --- | --- |
| `locked` | 未解锁 |
| `available` | 可学习 |
| `in_progress` | 学习中 |
| `completed` | 已掌握 |
| `review_required` | 需要复习 |

### 22.7 资源网络跳转目标 target.type

| 值 | 说明 |
| --- | --- |
| `knowledge_resource_network` | 跳转到指定知识点资源网络图 |
| `video_detail` | 跳转到视频学习页 |
| `document_detail` | 跳转到课程文档页 |
| `exercise_detail` | 跳转到练习题/测试详情页 |
| `reading_detail` | 跳转到拓展阅读页 |
| `code_case_detail` | 跳转到代码案例页 |

### 22.8 通知类型

| 值 | 说明 |
| --- | --- |
| `strict` | 计划与调度提醒 |
| `ava` | 激励与情绪提醒 |
| `coach` | 实战测验提醒 |
| `community` | 社区互动 |
| `system` | 系统公告 |

### 22.9 审核状态

| 值 | 说明 |
| --- | --- |
| `pass` | 通过 |
| `risk` | 存在风险，待人工复核 |
| `blocked` | 已拦截 |
| `resolved` | 已处理 |

### 22.10 数据库枚举映射

接口层优先使用贴近前端业务语义的枚举值；落库时按 `hope_sparks.sql` 中的正式字段值转换。后端实现时建议在 DTO/Assembler 层统一处理映射，不要让前端直接依赖数据库枚举。

#### 22.10.1 资源类型

对应数据库字段：`learning_resource.resource_type`

| API 值 | 数据库值 | 说明 |
| --- | --- | --- |
| `video` | `video` | 视频/动画资源 |
| `document` | `doc` | 文档、讲义、PDF、Markdown |
| `mindmap` | `mindmap` | 思维导图 |
| `exercise_set` | `quiz` | 资源网络图中的练习入口；具体题目集合关联 `practice_set` |
| `code_case` | `code` | 代码示例、实验案例 |
| `ppt` | `ppt` | 课件 |
| `knowledge_node` | 不落 `learning_resource` | 资源网络图中的相关知识点，来源于 `knowledge_node` |

#### 22.10.2 练习/测试类型

对应数据库字段：`practice_set.set_type`

| API 值 | 数据库值 | 说明 |
| --- | --- | --- |
| `practice` | `daily` | 普通练习、课后练习 |
| `test` | `exam` | 测试/考试 |
| `challenge` | `mock` | 挑战、模拟测验 |
| `review` | `review` | 复习巩固 |
| `code_lab` | `daily` 或 `mock` | 代码专项不单独建库枚举，通过题型 `code` 与标签区分 |

#### 22.10.3 题型

对应数据库字段：`question_bank.question_type`

| API 值 | 数据库值 | 说明 |
| --- | --- | --- |
| `single_choice` | `single` | 单选题 |
| `multiple_choice` | `multi` | 多选题 |
| `fill_blank` | `fill` | 填空题 |
| `short_answer` | `essay` | 简答/论述题 |
| `code` | `code` | 编程题 |

#### 22.10.4 学习进度与计划状态

知识点进度对应数据库字段：`user_knowledge_progress.progress_status`

| API 值 | 数据库值 | 说明 |
| --- | --- | --- |
| `locked` | `locked` | 未解锁 |
| `available` | `learning` | 已可学习但尚未完成 |
| `in_progress` | `learning` | 学习中 |
| `completed` | `mastered` | 已掌握 |
| `review_required` | `review` | 需要复习 |

学习计划对应数据库字段：`study_plan.plan_status`

| API 值 | 数据库值 | 说明 |
| --- | --- | --- |
| `pending` | `0` | 待执行 |
| `active` | `1` | 执行中 |
| `completed` | `2` | 已完成 |
| `paused` | `3` | 已暂停 |

学习任务对应数据库字段：`study_task.task_type`、`study_task.task_status`

| API 任务类型 | 数据库值 | 说明 |
| --- | --- | --- |
| `learn` | `learn` | 学习任务 |
| `review` | `review` | 复习任务 |
| `practice` | `practice` | 练习任务 |
| `exam` | `exam` | 测试任务 |

| API 任务状态 | 数据库值 | 说明 |
| --- | --- | --- |
| `pending` | `0` | 待办 |
| `in_progress` | `1` | 进行中 |
| `completed` | `2` | 已完成 |
| `overdue` | `3` | 已逾期 |

#### 22.10.5 Agent、知识库与审核工单

| 场景 | 数据库字段 | 可选值 |
| --- | --- | --- |
| Agent 记忆范围 | `agent_memory.memory_scope` | `public`、`user_private`、`agent_private`、`session_short` |
| 异步生成类型 | `async_generation_task.output_type` | `doc`、`video`、`audio`、`mindmap`、`report` |
| 异步任务状态 | `async_generation_task.task_status` | `pending`、`processing`、`success`、`failed` |
| 知识库文档状态 | `kb_document.parse_status` | `pending`、`parsing`、`embedding`、`success`、`failed` |
| 工单目标类型 | `feedback_ticket.target_type` | `resource`、`chat_message`、`comment`、`chunk`、`system` |
| 工单问题类型 | `feedback_ticket.issue_type` | `hallucination`、`inappropriate`、`bug`、`other` |
| 博客点赞目标 | `blog_like.target_type` | `post`、`comment` |

## 23. MVP 接口优先级建议

第一阶段建议优先实现：

| 优先级 | 模块 |
| --- | --- |
| P0 | 登录注册、当前用户、画像引导、Dashboard 基础数据 |
| P0 | Agent 会话、Nebula 探索、资源列表、学习拓扑、资源网络图、学习计划与日历 |
| P0 | 文章列表、文章详情、发布文章、评论、点赞收藏 |
| P1 | 视频进度、文档伴读、练习/测试提交、技能树点亮 |
| P1 | 通知、私信、设置、设备安全 |
| P2 | Manage 数据看板、知识库、争议工单、Prompt 配置、AI 审核 |

## 24. Apifox 导入说明

根目录已提供 Apifox 可直接导入的 OpenAPI 文件：`apifox-openapi.yaml`。该文件用于在 Apifox 中生成接口目录、请求方法、路径参数、查询参数、通用鉴权、通用响应结构和基础请求体；本 Markdown 文档继续作为详细业务说明、数据库落库说明和枚举映射说明。

导入建议：

| 项 | 建议 |
| --- | --- |
| 导入文件 | `apifox-openapi.yaml`，已包含每个接口的 Path Params、Query Params、Body Params 设计 |
| 导入类型 | OpenAPI / Swagger |
| 基础地址 | 本地开发可先使用 `http://localhost:8080`，后续在 Apifox 环境中改为真实后端地址 |
| 鉴权方式 | Bearer Token，对应请求头 `Authorization: Bearer <token>` |
| 导入后维护 | 在 Apifox 中补充更细的 Mock 示例、前置脚本、环境变量和接口用例；接口路径和数据库映射以本 Markdown 文档为准 |

注意：不要直接把本 Markdown 文档作为接口集合导入 Apifox。Markdown 适合阅读，OpenAPI 文件才适合生成可调试接口。
