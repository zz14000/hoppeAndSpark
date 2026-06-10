# arch-explore Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-explore` owns Nebula exploration entrypoints, exploration detail/progress, mind-map generation, knowledge association graph triggers, and resource generation task creation.

## P0 Scope

Recommended window: W3 agent and Nebula entry.

P0 APIs to implement:

- `POST /api/v1/explore`
- `GET /api/v1/explore/{exploreId}`
- `POST /api/v1/explore/{exploreId}/mindmap`

Related documented API that may be implemented with `arch-knowledge` support:

- `GET /api/v1/knowledge-graph`

Main data sources:

- `knowledge_node`
- `knowledge_node_relation`
- `learning_resource`
- `async_generation_task`
- `kb_chunk_record` / Chroma through `arch-infra`

## Boundaries

- Do not directly own final `learning_resource` writes; generated resources are persisted by `arch-resource`.
- Do not own async task records; create/update them through `arch-task`.
- Do not call Coze Workflow directly; use `arch-infra`.
- Leave Nebula/Coze workflow execution empty or mocked until real Agent integration is explicitly started.

## Implementation Notes

- `POST /api/v1/explore` should return an exploration/task id that `GET /api/v1/explore/{exploreId}` can use for progress.
- Queue long Nebula resource generation through Redis Stream using `arch-infra`.
- Mind-map output should reference resource IDs or node IDs, not duplicate full resource records.
- Keep Nebula input/output semantics aligned with the UML document; Nebula produces structured exploration/resource drafts, while final persistence stays in `arch-resource`.
