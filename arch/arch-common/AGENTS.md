# arch-common Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`.

## Mission

`arch-common` is the lowest shared layer. It owns cross-cutting contracts only: response wrappers, paging, exception shape, request context, validation helpers, and shared web utilities.

## P0 Scope

Recommended window: W1 foundation.

This module does not expose REST APIs. It must support all P0 APIs with stable shared primitives:

- `ApiResponse`: `code`, `message`, `data`, `requestId`.
- `PageResponse`: `page`, `pageSize`, `total`, `list`.
- global API error mapping for validation, auth, permission, not found, and business errors.
- `RequestContext` and `RequestIdFilter` behavior used by auth, agent, task, manage, and community flows.

## Boundaries

- Do not add user, agent, resource, study, practice, community, KB, or manage business concepts here.
- Do not depend on any business module.
- Keep IDs as frontend-facing strings in VO contracts, but do not define business VO classes in this module.
- If DTO/VO, API docs, and SQL disagree, owning modules must follow `../../资源/hope_sparks.sql`; common should not encode table-specific rules.
- Multi-agent task/agent/review protocol fields from the Coze UML document can use common primitives, but business DTO/VO definitions stay in the owning modules.

## Implementation Notes

- Controllers in other modules should return common response types, never raw maps once real DTO/VO work starts.
- Add tests for shared response and exception behavior before using it across P0 modules.
