# AGENTS.md

This file is the local working guide for coding agents in `arch`. It is derived from root `arch.md` v0.12 and should be followed when changing the backend skeleton.

For Coze multi-agent orchestration, also read root `../Coze-多Agent-UML设计图-重构版.md`. It defines `SparkEntry`, `TaskPlanner`, `ProfileBuilder`, `ContextNormalizer`, `TaskScheduler`, `Sage`, `Coach`, `Strict`, `Nebula`, `Aggregator`, `ReviewPackBuilder`, `Horizon`, the revise loop, and L1/L2 memory rules.

## 1. Mission

Build the Hope and Sparks backend as a Java 17 + Spring Boot 3.x + Maven modular monolith.

Primary goals:

- keep the MVP runnable as one Spring Boot application;
- keep module boundaries explicit with Maven submodules;
- support Spark frontend learning flows and Manage backend governance flows;
- isolate external systems behind adapters and gateways;
- make the first implementation easy to start, test, and extend.

Do not redesign the architecture casually. Treat root `arch.md`, root `Coze-多Agent-UML设计图-重构版.md`, and `CODE_MAP.md` as the current source of architectural truth for backend and Agent work.

## 2. Current Architecture

The project uses:

- Java 17;
- Spring Boot 3.x;
- Maven parent POM plus submodules;
- MySQL;
- Flyway;
- MyBatis-Plus, with MyBatis XML for complex SQL;
- Redis and Redis Stream;
- MinIO;
- Chroma;
- Coze for agents;
- SSE for streaming agent responses;
- JWT access token + `user_login_session.session_token` session token for authentication.

MVP external dependencies should have mock adapters where possible so core flows can run without real Coze, MinIO, Redis Stream, or Chroma credentials.

## 3. Module Rules

Top-level `arch` is only a parent POM and documentation location. Do not put business source code directly under top-level `arch/src`.

Follow this dependency direction:

```text
arch-common
  -> used by all modules
arch-infra
  -> external systems and infrastructure adapters
arch-task
  -> async task state and progress
business modules
  -> own business APIs and state transitions
arch-manage
  -> backend aggregation, RBAC, operation log, dashboards
arch-boot
  -> runnable application assembly
```

Hard boundaries:

- Controllers do not contain business logic.
- Services own use-case orchestration, transactions, and state transitions.
- Mappers only handle data access.
- Entities are not returned directly to the frontend.
- DTO and VO classes define API contracts.
- Business modules should not directly use MinIO SDK, Coze SDK, model SDKs, or raw Redis Stream commands.
- Secrets must not be committed or stored in SQL seed data.

## 4. Naming And Package Conventions

Root package is `com.hopeandsparks`.

Use standard package names inside each module:

```text
controller
service
service.impl
mapper
consumer
entity
dto
vo
enums
config
```

Use API/database naming separation:

- API fields: camelCase;
- Java fields: camelCase;
- database columns: snake_case;
- when API docs, DTO/VO, and SQL schema disagree, `资源/hope_sparks.sql` wins;
- frontend-facing IDs: string values in VO responses.

## 4.1 Code Simplicity And Comments

Write implementation code at an ordinary university-student level, i.e. `普通大学生水平`. Prefer readable, direct code over cleverness:

- keep controller methods thin and obvious;
- keep service methods short enough to explain during a project defense;
- avoid unnecessary design patterns, deep inheritance, reflection, complex generics, and over-abstracted helper layers;
- use clear names instead of compact or tricky code;
- add comments to new classes, public methods, DTO/VO fields when useful, and important business steps;
- comments should explain business intent and state transitions, not repeat obvious Java syntax.

`Agent 接入先空着`: Agent/Coze integration is intentionally left empty for now:

- define ports, DTOs, mock adapters, and TODO placeholders only;
- do not call real Coze APIs, configure real Bot/Workflow credentials, or add SDK-specific request logic;
- business flows that need Agent output should return deterministic mock/placeholder data until explicit integration work starts.

## 5. Implementation Priorities

When no more specific user request exists, prefer the engineering order below:

1. keep Maven aggregation compiling;
2. keep `arch-boot` runnable;
3. implement shared primitives in `arch-common`;
4. implement adapters and ports in `arch-infra`;
5. implement async task state in `arch-task`;
6. fill P0 business module skeletons with narrow, testable use cases;
7. add real external integrations only behind existing ports;
8. add tests around shared behavior and cross-module contracts.

P0 feature paths from `arch.md`:

- user registration/login/current user and onboarding;
- agent session, SSE streaming, basic memory persistence;
- Nebula exploration and resource generation progress;
- learning plan and knowledge topology;
- practice submission and AI evaluation report;
- Manage admin login, RBAC, operation log;
- Manage knowledge-base upload and parse state;
- community article/comment flows and moderation.

## 6. Module Ownership Notes

### `arch-common`

Put only cross-cutting primitives here:

- `ApiResponse`;
- `PageResponse`;
- global exception types/handlers;
- request ID and request context helpers;
- shared enums/utilities.

Avoid business-specific concepts here.

### `arch-infra`

Own infrastructure ports and adapter implementations:

- Redis Stream client;
- file storage abstraction and MinIO implementation;
- Coze client and Agent gateway;
- LLM gateway;
- Chroma/vector adapter;
- config properties for external systems.

`sys_agent_config` belongs here because it stores Coze Bot / Workflow routing and runtime config. Do not store Coze Token or other secrets in this table.

### `arch-task`

Own `async_generation_task` and task lifecycle APIs:

- create task;
- update status;
- update progress;
- record external run id;
- retry count and failure reason;
- query task status.

Redis Stream low-level publishing/consuming stays in `arch-infra`.

### `arch-auth`

Own frontend user identity and settings:

- registration/login/logout;
- session token refresh;
- current user;
- user profile basics;
- device security;
- user settings.

Do not put backend admin accounts here. They belong to `arch-manage`.

### `arch-profile`

Own onboarding, Spark profile generation/rebuild, and long-term preferences. It may read agent memory concepts but should keep profile state local to this module unless a shared contract is introduced.

### `arch-agent`

Own local agent records and user-facing agent interaction:

- sessions;
- messages;
- memory summary;
- prompt business;
- SSE streaming transfer.

Call Coze through `arch-infra`; do not call Coze directly from controllers.

### `arch-explore`

Own exploration entrypoints and generation triggers. Do not directly write final learning resource tables. Generated resources are owned by `arch-resource`.

### `arch-resource`

Own learning resource lifecycle:

- generated result persistence;
- versions;
- favorites;
- learning progress;
- export;
- quality feedback and status.

### `arch-study`

Own learning plans, study tasks, calendars, and user knowledge progress.

### `arch-practice`

Own practice sets, question submissions, user question records, and evaluation reports. MVP code-question review is AI text review only; do not introduce a real code sandbox unless explicitly requested.

### `arch-kb`

Own knowledge-base business state:

- document records;
- parse state;
- chunk records;
- parse strategy;
- reparse behavior;
- retrieval/citation business.

Files are referenced by `file_id`; actual storage belongs to `arch-infra`.

### `arch-community`

Own articles, comments, likes, favorites, and moderation state transitions.

Article/comment moderation should use pending/risk/blocked/published/offline style states rather than a simple boolean publish flag.

### `arch-manage`

Own backend management entrypoints and RBAC:

- `/api/v1/manage/**`;
- admin login;
- roles, menus, Controller resources, and resource categories;
- menu permissions and Controller resource checks;
- operation logs;
- read-only dashboards.

For business changes, call the owning business module Service. Do not duplicate core state machines in `arch-manage`.

### `arch-boot`

Own application assembly:

- main Spring Boot application class;
- global application configuration files;
- health endpoint;
- app info endpoint;
- upload entrypoint if it only delegates to `arch-infra` file service.

Business controllers should live in their own business modules, not in `arch-boot`.

## 7. API Rules

- All frontend APIs start with `/api/v1`.
- Manage backend APIs start with `/api/v1/manage`.
- Use unified response shape: `code`, `message`, `data`, `requestId`.
- Use unified paging shape: `page`, `pageSize`, `total`, `list`.
- Validate request DTOs at controller boundary.
- Convert entities to VO before returning responses.
- Keep `X-Request-Id` flowing through logs, errors, Redis Stream messages, and external calls.

## 8. Async And Agent Rules

Use SSE for:

- agent chat;
- companion reading;
- short interactive Coach hints;
- task event subscriptions when a page needs live progress.

Use Redis Stream plus task status for:

- resource generation;
- plan generation;
- knowledge-base parse/embed;
- community moderation;
- long-running quality checks;
- long-running evaluation report generation.

Agent/LLM boundaries:

- business code depends on `AgentGateway` and `LlmGateway`;
- do not expose Coze SDK details outside `arch-infra`;
- keep real Agent/Coze provider integration empty in the current phase; use ports, mocks, and deterministic placeholders;
- use Bot-style calls for dialog agents;
- use Workflow-style calls for long or structured outputs;
- store local-to-external conversation/message IDs for traceability.

Coze multi-agent orchestration:

- follow `../Coze-多Agent-UML设计图-重构版.md` for agent roles, task protocols, review/revise decisions, and L1/L2 memory behavior;
- implement orchestration in this order: `TaskPlanner`, `ContextNormalizer`, `TaskScheduler`, `Aggregator`, `ReviewPackBuilder` + `Horizon` + revise loop, then L1/L2 memory integration;
- keep `task_id`, `task_type`, `target_agent`, `source_agent`, `status`, and `final_decision` fields stable across DTO/VO and gateway boundaries;
- use `arch-task` for async task state and `arch-infra` for Coze/Redis transport; do not bury orchestration state inside controllers.

## 9. Security And Config

Never commit or persist these secrets:

- Coze API Token;
- MinIO access key or secret key;
- Redis password or cloud Redis credential;
- MySQL credential;
- Chroma credential;
- JWT signing secret.

Use environment variables, deployment Secrets, or local ignored `.env` files.

Security split:

- frontend user token resolves to `sys_user`;
- backend admin token resolves to `sys_admin`;
- backend permissions use `sys_admin`, `sys_role`, `sys_admin_menu`, `sys_admin_resource`, and `sys_admin_resource_category`;
- high-risk backend write operations must write `sys_operation_log`.

## 10. Database And Migration Rules

Use Flyway for schema versioning.

The initial `V1` migration should be based on the confirmed complete `资源/hope_sparks.sql` and include the architecture-confirmed additions:

- `sys_agent_config`;
- external Coze ID mapping fields;
- async task tracking fields;
- backend RBAC tables;
- operation log table;
- community moderation status fields.

Use future `V2+` migrations only for new changes. Do not split already-confirmed v0.12 schema decisions into scattered patch migrations unless there is a clear reason.

## 11. Development Commands

Run from `arch/` unless noted otherwise.

```powershell
mvn -q -DskipTests compile
mvn test
mvn -pl arch-boot spring-boot:run
```

Useful searches from repository root:

```powershell
rg --files arch
rg "class .*Controller" arch
rg "TODO|FIXME" arch
rg "sys_agent_config|async_generation_task|RedisStream|Coze|MinIO" arch
rg "TaskPlanner|ContextNormalizer|TaskScheduler|ReviewPackBuilder|Horizon" "Coze-多Agent-UML设计图-重构版.md" arch
```

Before changing architecture-level behavior, reread:

- root `arch.md`;
- root `Coze-多Agent-UML设计图-重构版.md` for Agent orchestration changes;
- `arch/CODE_MAP.md`;
- affected module POM files.

## 12. Testing Guidance

Scale tests to risk:

- shared response/exception/security context code should have focused unit tests;
- task state transitions should have unit tests;
- module contracts that cross boundaries should have integration or slice tests when practical;
- controllers should be tested for request/response shape once behavior is implemented;
- external adapters should have mock-mode tests before real credentials are required.

Do not require real Coze, MinIO, Chroma, or cloud Redis credentials for the default local test path.

## 13. Things To Avoid

- Do not introduce microservices for MVP.
- Do not add direct SDK calls from controllers or business services when an infrastructure port should exist.
- Do not implement real Agent/Coze calls in this phase; leave integration as interfaces, mocks, or TODO placeholders.
- Do not put business source code in top-level `arch/src`.
- Do not return entities directly from controllers.
- Do not place backend admin identity inside `arch-auth`.
- Do not place generated resource persistence inside `arch-explore`.
- Do not place `async_generation_task` inside `arch-infra` or `arch-explore`.
- Do not make `arch-manage` the owner of every business state transition.
- Do not introduce a real code execution sandbox for MVP unless explicitly requested.
- Do not commit credentials, local secrets, or generated bulky artifacts.

## 14. Documentation Maintenance

When module ownership, dependency direction, API conventions, or P0 scope changes:

1. update root `arch.md` first if the architecture decision changed;
2. update `arch/CODE_MAP.md` so future code search starts correctly;
3. update this `AGENTS.md` when agent working rules or implementation guardrails change.
