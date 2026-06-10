# arch-resource Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-resource` owns learning resources, resource versions, favorites, learning progress, export, quality feedback, and persistence of generated resource outputs.

## P0 Scope

Recommended window: W4 resource and study path.

P0 APIs to implement:

- `GET /api/v1/resources`
- `GET /api/v1/resources/{resourceId}`

Documented supporting APIs that are useful but can be scoped carefully:

- `PUT /api/v1/resources/{resourceId}/progress`
- `POST /api/v1/resources/{resourceId}/export`
- `POST /api/v1/resources/{resourceId}/feedback`

Generation-progress support:

- final generated resource writes happen here.
- async status/progress comes from `arch-task` and owner-specific APIs such as `GET /api/v1/explore/{exploreId}`.

Main tables:

- `learning_resource`
- `learning_resource_version`
- `user_resource_favorite`
- `user_learning_record`
- `sys_oss_file` through `arch-infra`

## Boundaries

- Do not trigger Nebula exploration here; `arch-explore` owns exploration entrypoints.
- Do not own knowledge topology; `arch-knowledge` and `arch-study` own graph/plan reads.
- Do not directly use MinIO SDK; use `arch-infra`.
- Treat Agent-generated resource data as mock/placeholder input until real Agent integration is explicitly started.

## Implementation Notes

- Resource details must return `detailRoute` and `detailApi` when needed by the frontend.
- Store generated versions and quality status separately from the stable resource record.
- Favorites under resources stay here; article favorites stay in `arch-community`.
- When consuming Nebula/Horizon outputs, treat the Coze UML document's Agent output/review protocol as the input contract and convert it into resource/version/quality fields here.
