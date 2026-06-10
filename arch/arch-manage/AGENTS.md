# arch-manage Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`, `../../Spring Security JWT 动态URL权限总结.md`.

## Mission

`arch-manage` owns Manage backend entrypoints, admin identity, RBAC, menu permissions, Controller base-path resource permissions, operation logs, dashboards, and backend aggregation.

## P0 Scope

Recommended window: W7 Manage governance.

P0 core APIs:

- `POST /api/v1/manage/auth/login`
- `GET /api/v1/manage/menus`
- role/admin/menu/resource-permission APIs already scaffolded under `/api/v1/manage/roles`, `/api/v1/manage/admins`, and related controllers.
- `GET /api/v1/manage/operation-logs`

P0 governance APIs from existing docs:

- `GET /api/v1/manage/knowledge-base/documents`
- `POST /api/v1/manage/knowledge-base/documents`
- `PUT /api/v1/manage/knowledge-base/documents/{documentId}`
- `DELETE /api/v1/manage/knowledge-base/documents/{documentId}`
- `GET /api/v1/manage/knowledge-base/documents/{documentId}/parse-status`
- `GET /api/v1/manage/ai-disputes`
- `PUT /api/v1/manage/ai-disputes/{disputeId}`

Useful documented management APIs:

- `GET /api/v1/manage/dashboard/overview`
- `GET /api/v1/manage/users`
- `POST /api/v1/manage/users/{userId}/actions`
- `GET /api/v1/manage/users/{userId}/learning-trace`
- `GET /api/v1/manage/resources`
- `DELETE /api/v1/manage/resources/{resourceId}`
- `GET /api/v1/manage/agent-prompts`
- `PUT /api/v1/manage/agent-prompts/{promptId}`
- `GET /api/v1/manage/moderation/content`
- `PUT /api/v1/manage/moderation/content/{recordId}`

Main tables:

- `sys_admin`
- `sys_role`
- `sys_admin_menu`
- `sys_admin_resource`
- `sys_admin_resource_category`
- `sys_admin_role`
- `sys_role_admin_menu`
- `sys_role_admin_resource`
- `sys_operation_log`
- `feedback_ticket`

## Controller Resource Permission Rule

P0 Controller resources are backend Controller base paths, not every method path. Store real base paths in `sys_admin_resource.url`, for example `/api/v1/manage/users` or `/api/v1/manage/knowledge-base/documents`.

Do not confuse these RBAC resources with generated learning resources under `/api/v1/manage/resources`.

## Boundaries

- Do not duplicate business state machines from `arch-kb`, `arch-resource`, `arch-community`, `arch-agent`, or `arch-auth`.
- For business writes, call the owning module service/facade, then write `sys_operation_log`.
- Do not place frontend user login/session logic here.

## Implementation Notes

- Login returns an ADMIN token that `arch-boot` can distinguish from frontend USER tokens.
- High-risk writes must log actor, module, action, target id, path, request summary, result, failure reason, IP, User-Agent, and time.
- Menu permissions control visible pages; Controller resource permissions control backend route access.
- Agent Prompt and Coze runtime governance should preserve the UML document's agent codes and protocol expectations; do not let Manage UI rename codes in a way that breaks orchestration.
