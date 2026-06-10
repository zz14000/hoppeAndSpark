# arch-task Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-task` owns application-level async task state. It tracks what an async business job is doing; it does not own Redis Stream transport or business result persistence.

## P0 Scope

Recommended window: W1 foundation.

Core service contract for P0:

- create `async_generation_task` records with task type, owner, idempotency key, and initial status.
- query task status/progress for frontend and Manage screens.
- update status: `pending`, `processing`, `success`, `failed`.
- update progress, failure reason, retry count, max retry, external run id, started/finished timestamps.
- support task types from existing docs: `resource_generate`, `resource_audit`, `plan_generate`, `kb_parse`, `kb_embed`, `community_moderation`, and evaluation/report generation when long-running.
- preserve orchestration identifiers from the Coze UML document, especially `task_id`, `task_type`, `target_agent`, dependencies, and external workflow run ids when a task maps to an async Coze Workflow.

## API Ownership

Existing API docs expose task progress through owner-specific APIs such as:

- `GET /api/v1/explore/{exploreId}` for Nebula exploration/generation progress.
- `GET /api/v1/manage/knowledge-base/documents/{documentId}/parse-status`.

Only add a generic `/api/v1/tasks/{taskId}` style endpoint if the API docs are updated or a later task explicitly asks for it.

## Boundaries

- Do not publish or consume Redis Stream messages directly; use `arch-infra`.
- Do not write business outputs such as resources, KB chunks, articles, comments, or reports.
- Do not depend on business modules.
