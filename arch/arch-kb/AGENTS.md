# arch-kb Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-kb` owns knowledge-base document business state, parse state, chunks, vectorization mapping, parse strategy, retrieval, and citation behavior.

## P0 Scope

Recommended window: W7 Manage governance.

P0 is reached through Manage entrypoints, with business state owned here:

- `GET /api/v1/manage/knowledge-base/documents`
- `POST /api/v1/manage/knowledge-base/documents`
- `PUT /api/v1/manage/knowledge-base/documents/{documentId}`
- `DELETE /api/v1/manage/knowledge-base/documents/{documentId}`
- `GET /api/v1/manage/knowledge-base/documents/{documentId}/parse-status`

Main tables:

- `kb_document`
- `kb_chunk_record`
- `kb_parse_strategy`
- `sys_oss_file` by file id through `arch-infra`
- `async_generation_task` through `arch-task`

## Boundaries

- Do not expose public `/api/v1/kb/**` APIs unless the API docs are updated.
- Manage controllers own backend routes, permission checks, operation logs, and page VO assembly.
- This module owns parse/reparse state changes and document validation.
- Do not directly use MinIO, Chroma, or Redis Stream SDKs; use `arch-infra`.

## Implementation Notes

- Upload should accept a completed `fileId`, then create/update `kb_document`.
- Parse and embed work should run through Redis Stream and update `async_generation_task`.
- Keep parse status query deterministic for polling by Manage pages.
- Retrieval/citation outputs that feed Agent context should be shaped for `ContextNormalizer` rather than leaking KB storage internals.
