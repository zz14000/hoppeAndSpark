# arch-infra Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-infra` owns infrastructure ports and adapters. Business modules call this module for external capabilities instead of calling SDKs or raw clients directly.

## P0 Scope

Recommended window: W1 foundation.

No frontend REST API is owned here. The P0 support surface is:

- JWT signing/parsing and token storage support for user/admin login.
- file storage port for `/api/v1/uploads/token` and `/api/v1/uploads/complete`.
- Redis Stream publish/read helpers for resource generation, KB parse/embed, community moderation, plan/evaluation tasks.
- Coze/Agent gateway and LLM gateway used by `arch-agent`, `arch-explore`, `arch-study`, `arch-practice`, and `arch-community`.
- `sys_agent_config` access for Bot/Workflow routing; never store Coze Token or secrets in the table.
- `sys_oss_file` metadata access behind the file storage abstraction.

## Boundaries

- Do not implement business state machines here.
- Do not write `learning_resource`, `kb_document`, `blog_post`, `study_plan`, or `evaluation_report` here.
- Do not expose Coze, MinIO, Chroma, Redis, or model SDK types outside infrastructure adapters.
- Do not wire real Coze provider calls yet; keep gateway methods empty, mocked, or explicitly marked TODO.
- Do not commit or seed secrets: Coze Token, MinIO keys, Redis password, MySQL password, Chroma credential, JWT signing secret.

## Implementation Notes

- Provide mock-mode adapters so W2-W7 can run without real Coze, MinIO, Redis Stream, or Chroma credentials.
- Keep Redis Stream queue names aligned with `queue:{domain}:{action}` and existing `queue:kb:parse` / `queue:kb:embed` guidance.
- Preserve `X-Request-Id` in outbound logs, stream messages, and external-call metadata.
- Map Coze calls by stable agent/workflow codes from the UML document; keep `SparkEntry`, `TaskPlanner`, `TaskScheduler`, Agent, and `Horizon` provider details hidden behind gateway DTOs.
- Keep adapter code simple and commented; avoid SDK-heavy abstractions until real integration is explicitly started.
