# Hope and Sparks Backend Code Map

> Source of truth: root `arch.md` v0.12.
> Scope: `arch` Maven modular-monolith backend project.
> Root package: `com.hopeandsparks`.

## 1. Project Shape

`arch` is the target backend project. It is a parent POM aggregation project and should not contain business source code at the top level.

```text
arch/
├─ pom.xml
├─ AGENTS.md
├─ CODE_MAP.md
├─ arch-boot/
├─ arch-common/
├─ arch-infra/
├─ arch-task/
├─ arch-auth/
├─ arch-profile/
├─ arch-agent/
├─ arch-explore/
├─ arch-knowledge/
├─ arch-resource/
├─ arch-study/
├─ arch-practice/
├─ arch-kb/
├─ arch-community/
├─ arch-im/
├─ arch-notification/
└─ arch-manage/
```

The backend uses a modular monolith:

- one Spring Boot application process for MVP and demo stage;
- multiple Maven modules for boundaries and maintainability;
- no microservice split in the first implementation pass;
- Redis Stream consumers run inside the same application at MVP stage, but are organized so workers can be split later.

## 2. Dependency Direction

Dependencies should stay one-way and layered.

```text
arch-common
  ↑
arch-infra      arch-task
  ↑              ↑
business modules: auth/profile/agent/explore/knowledge/resource/study/practice/kb/community/im/notification
  ↑
arch-manage
  ↑
arch-boot
```

Rules:

- `arch-common` is the lowest shared layer.
- `arch-infra` depends on `arch-common` and owns external infrastructure adapters.
- `arch-task` depends on `arch-common` and owns async task state.
- Business modules may depend on `arch-common`, `arch-infra`, and `arch-task` as needed.
- `arch-manage` is a backend/admin aggregation entry and may call business Services or Facades.
- `arch-boot` assembles all modules and owns the runnable Spring Boot application.
- Business modules must not depend on `arch-boot`.
- Business modules should avoid arbitrary peer-to-peer dependencies. Prefer Facade/Service boundaries when cross-module calls are necessary.
- MVP does not split `xxx-api` modules unless the architecture changes later.

## 3. Standard Module Layout

Each business module should follow the same package style.

```text
arch-{module}/
└─ src/main/
   ├─ java/com/hopeandsparks/{module}/
   │  ├─ controller/
   │  ├─ service/
   │  │  └─ impl/
   │  ├─ mapper/
   │  ├─ consumer/
   │  ├─ entity/
   │  ├─ dto/
   │  ├─ vo/
   │  ├─ enums/
   │  └─ config/
   └─ resources/
      └─ mapper/
```

Layer intent:

- `controller`: REST API, SSE, WebSocket, and management endpoints.
- `service`: use-case orchestration, transactions, state transitions, external capability calls.
- `mapper`: MyBatis-Plus mapper and MyBatis XML access.
- `entity`: database entities, mapped from snake_case tables to camelCase fields.
- `dto`: request objects and internal commands.
- `vo`: response view objects.
- `consumer`: Redis Stream consumers and worker-style task handlers.
- `enums`: module-local enums and state values.
- `config`: module-local configuration.

## 4. Core Modules

| Module | Package | Responsibility | Main Tables |
| --- | --- | --- | --- |
| `arch-boot` | `com.hopeandsparks.boot` | Spring Boot startup, global config assembly, module scanning, a few global controllers | none |
| `arch-common` | `com.hopeandsparks.common` | unified response, exceptions, paging, shared enums, utilities, basic request/security context | none |
| `arch-infra` | `com.hopeandsparks.infra` | Redis Stream client, MinIO file capability, Chroma, Coze, LLM gateway, infrastructure config clients | `sys_oss_file`, `sys_agent_config` |
| `arch-task` | `com.hopeandsparks.task` | async task creation, status transition, progress, retry, failure reason, idempotency key | `async_generation_task` |
| `arch-auth` | `com.hopeandsparks.auth` | user registration/login, token, current user, user profile basics, device security, user settings | `sys_user`, `user_login_session`, `user_settings` |
| `arch-profile` | `com.hopeandsparks.profile` | Spark onboarding, profile generation/rebuild, long-term preferences | `user_profile`, `agent_memory` |
| `arch-agent` | `com.hopeandsparks.agent` | agent sessions, messages, SSE forwarding, memory summary, prompt business | `agent_chat_session`, `agent_chat_message`, `agent_memory`, `sys_agent_prompt` |
| `arch-explore` | `com.hopeandsparks.explore` | Nebula exploration entry, mind map, knowledge association graph, resource generation trigger | optional `explore_*` process tables |
| `arch-knowledge` | `com.hopeandsparks.knowledge` | courses, knowledge nodes, relationships, topology queries | `course`, `knowledge_node`, `knowledge_node_relation` |
| `arch-resource` | `com.hopeandsparks.resource` | generated learning resource lifecycle, versions, favorites, progress, export, quality feedback | `learning_resource`, `learning_resource_version`, `user_resource_favorite`, `user_learning_record` |
| `arch-study` | `com.hopeandsparks.study` | study plan, Strict plan generation/adjustment, calendar events, task execution | `study_plan`, `study_task`, `user_knowledge_progress` |
| `arch-practice` | `com.hopeandsparks.practice` | practice list, answer submission, code-question AI review, Coach report | `question_bank`, `practice_set`, `user_question_record`, `evaluation_report` |
| `arch-kb` | `com.hopeandsparks.kb` | knowledge-base document upload, parse, chunk, embed, retrieve, cite, reparse | `kb_document`, `kb_chunk_record`, `kb_parse_strategy` |
| `arch-community` | `com.hopeandsparks.community` | articles, comments, likes, favorites, async moderation | `blog_post`, `blog_comment`, `blog_like`, `blog_favorite` |
| `arch-manage` | `com.hopeandsparks.manage` | management backend aggregation, admin accounts, RBAC, operation logs, read-only dashboards | `sys_admin`, `sys_role`, `sys_admin_menu`, `sys_admin_resource`, `sys_admin_resource_category`, `sys_admin_role`, `sys_role_admin_menu`, `sys_role_admin_resource`, `sys_operation_log` |

## 5. Postponed Or Secondary Modules

| Module | Package | Status |
| --- | --- | --- |
| `arch-im` | `com.hopeandsparks.im` | private message and group chat; WebSocket can be postponed |
| `arch-notification` | `com.hopeandsparks.notification` | notifications and reminders; can be enhanced after P0 |
| `arch-achievement` | `com.hopeandsparks.achievement` | not present in current Maven modules; future achievement system |
| `arch-challenge` | `com.hopeandsparks.challenge` | not present in current Maven modules; future weekly challenge system |
| `arch-mall` | `com.hopeandsparks.mall` | not present in current Maven modules; future virtual asset marketplace |

## 6. Important Boundary Decisions

### `arch-auth`

`auth` and `user` stay in the same module. Login, current user, user settings, device security, and token state all revolve around the same user identity model.

Do not put backend admin accounts here. Admin identity and RBAC belong to `arch-manage`.

Do not put favorites here:

- resource favorites belong to `arch-resource`;
- article favorites belong to `arch-community`;
- user center aggregation can be implemented later if needed.

### `arch-infra`

Infrastructure owns adapters and external capability ports:

- Redis Stream low-level client and producer/consumer helpers;
- MinIO client, upload signature, file URL generation, `sys_oss_file` metadata;
- Chroma vector adapter;
- Coze client and Agent gateway;
- LLM gateway and future model adapters;
- `sys_agent_config` runtime routing for Coze Bot / Workflow.

Business modules should not directly depend on MinIO SDK, Coze SDK, model SDKs, or raw Redis Stream commands.

### `arch-task`

`arch-task` owns async application task state, not queue infrastructure.

It manages:

- `async_generation_task`;
- task type;
- status;
- progress;
- retry count;
- max retry;
- failure reason;
- idempotency key;
- external run id.

Redis Stream read/write primitives remain in `arch-infra`. Business consumers update task state through `arch-task`.

### `arch-agent`

`arch-agent` owns local agent business records:

- local sessions;
- local messages;
- memory summaries;
- prompt business;
- SSE forwarding behavior.

Coze Bot/Workflow IDs and runtime route configuration are stored in `sys_agent_config` and owned by `arch-infra`.

### `arch-explore` And `arch-resource`

Generated learning resources are written by `arch-resource`, not by `arch-explore`.

Recommended flow:

```text
arch-explore
 -> arch-task creates async_generation_task
 -> arch-infra publishes Redis Stream message
 -> Nebula / Strict workflow runs
 -> completion handler calls arch-resource Service
 -> arch-resource writes learning_resource and learning_resource_version
 -> arch-infra writes sys_oss_file or resolves file URL
```

### `arch-manage`

`arch-manage` is a management aggregation entry, not a replacement owner for every business workflow.

It owns:

- `/api/v1/manage/**` backend entrypoints;
- admin accounts;
- admin/role/menu/resource relationships;
- menu permissions and Controller resource checks;
- operation logs;
- read-only dashboard and statistics queries.

It should call the owning business module for state changes:

- user disable/enable: `arch-auth`;
- knowledge-base parse/reparse: `arch-kb`;
- resource status/quality changes: `arch-resource`;
- community moderation: `arch-community`;
- agent prompt/session business: `arch-agent`;
- Coze runtime config: `arch-infra`.

## 7. Runtime And External Systems

| Capability | MVP Choice | Notes |
| --- | --- | --- |
| Backend framework | Java 17 + Spring Boot 3.x | Maven modular monolith |
| ORM | MyBatis-Plus first, MyBatis XML for complex SQL | no SQL in Service layer |
| Database | MySQL | schema source is `资源/hope_sparks.sql` |
| Migration | Flyway | `V1` should contain the confirmed full schema |
| Cache/session | Redis | login state, blacklist, short-term context, cache |
| Async queue | Redis Stream | resource generation, KB parse/embed, community moderation |
| Vector database | Chroma | KB chunks, notes, transcripts, multimodal assets |
| File storage | MinIO | business sees file IDs and file service, not object internals |
| Agent platform | Coze | Bot for dialog, Workflow for long/structured processes |
| Streaming | SSE | agent dialog and optional task progress |
| Realtime messaging | WebSocket | IM and notifications can be postponed |

## 8. API Conventions

- All APIs use `/api/v1` prefix.
- Management backend APIs use `/api/v1/manage/**`.
- Responses use unified shape: `code`, `message`, `data`, `requestId`.
- Paging uses `page`, `pageSize`, `total`, `list`.
- API fields use camelCase.
- Database fields use snake_case.
- If API docs, DTO/VO, and SQL schema disagree, `资源/hope_sparks.sql` wins.
- IDs returned to the frontend should be strings.
- DTO/VO must isolate frontend contracts from database entities.

## 9. Async And Streaming Map

Use SSE for user-visible, immediate agent output:

- Ava dialog;
- Sage dialog and companion reading;
- Coach single-question hint when short enough;
- general agent chat streaming.

Use Redis Stream plus task status for long-running workflows:

- Nebula resource generation;
- Horizon resource quality check;
- Strict plan generation;
- knowledge-base parsing;
- embedding/vectorization;
- community article/comment moderation;
- evaluation reports when long-running.

## 10. Database Notes

Confirmed additions or semantic expansions from `arch.md`:

- `sys_agent_config`: Coze Bot / Workflow runtime config, owned by `arch-infra`; no secrets stored.
- `agent_chat_session`: should map local session to external Coze conversation/thread IDs.
- `agent_chat_message`: should map local message to external message ID and optionally raw response summary.
- `async_generation_task`: should support `task_type`, `external_run_id`, retry fields, start/end timestamps.
- backend RBAC tables: `sys_admin`, `sys_role`, `sys_admin_menu`, `sys_admin_resource`, `sys_admin_resource_category`, relation tables, `sys_operation_log`.
- `blog_post.post_status`: expand to draft/published/pending/risk/blocked/offline semantics.
- `blog_comment.comment_status`: add similar moderation status.
- code question MVP: store code text and use AI review; no real sandbox in MVP.

## 11. P0 Implementation Focus

The first high-value paths:

1. user registration/login/current user and onboarding;
2. agent session, streaming response, basic memory persistence;
3. Nebula exploration, resource list/detail, resource generation progress;
4. study plan, knowledge topology, knowledge-resource network;
5. practice submission, evaluation report, weak-point feedback;
6. Manage admin login, menu permissions, Controller resource permissions, operation log;
7. Manage knowledge-base document upload, parse status, dispute workflow;
8. community article list/detail/publish/comment/like/favorite;
9. community article/comment async moderation.

## 12. Existing Skeleton Snapshot

Current notable source files include:

- `arch-boot/src/main/java/com/hopeandsparks/boot/HopeAndSparksApplication.java`
- `arch-boot/src/main/java/com/hopeandsparks/boot/controller/HealthController.java`
- `arch-boot/src/main/java/com/hopeandsparks/boot/controller/AppInfoController.java`
- `arch-boot/src/main/java/com/hopeandsparks/boot/controller/UploadController.java`
- `arch-common/src/main/java/com/hopeandsparks/common/response/ApiResponse.java`
- `arch-common/src/main/java/com/hopeandsparks/common/response/PageResponse.java`
- `arch-common/src/main/java/com/hopeandsparks/common/exception/GlobalExceptionHandler.java`
- `arch-common/src/main/java/com/hopeandsparks/common/web/RequestIdFilter.java`
- `arch-infra/src/main/java/com/hopeandsparks/infra/redis/RedisStreamClient.java`
- `arch-infra/src/main/java/com/hopeandsparks/infra/file/FileStorageService.java`
- `arch-infra/src/main/java/com/hopeandsparks/infra/coze/CozeAgentClient.java`
- `arch-infra/src/main/java/com/hopeandsparks/infra/llm/LlmGateway.java`
- `arch-task/src/main/java/com/hopeandsparks/task/service/AsyncTaskService.java`
- `arch-task/src/main/java/com/hopeandsparks/task/enums/AsyncTaskStatus.java`

Representative controllers currently exist in:

- `arch-auth`: auth, user, settings.
- `arch-profile`: onboarding, user center.
- `arch-agent`: agent catalog, agent session, AI dispute.
- `arch-explore`: explore.
- `arch-knowledge`: skill tree, knowledge graph.
- `arch-resource`: resource, reading, document, video.
- `arch-study`: learning plan, calendar.
- `arch-practice`: practice.
- `arch-kb`: document service and parse consumer.
- `arch-community`: article, follow, moderation consumer.
- `arch-im`: chat, friend request.
- `arch-notification`: notification.
- `arch-manage`: auth, dashboard, RBAC, user, resource, knowledge base, storage, moderation, dispute, agent prompt.

Update this section when the skeleton becomes real implementation code.
