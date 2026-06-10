# arch-knowledge Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-knowledge` owns courses, knowledge nodes, knowledge relations, and reusable graph/topology queries.

## P0 Scope

Recommended window: W4 resource and study path.

P0 support APIs/data:

- support `GET /api/v1/knowledge-graph` when the controller stays in this module.
- support `GET /api/v1/learning-plans/{planId}/topology` through `arch-study`.
- support `GET /api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resource-network` through `arch-study`.
- support `GET /api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resources` through `arch-study`.

Non-P0 documented APIs:

- skill tree APIs can remain P1 unless a later task promotes them.

Main tables:

- `course`
- `knowledge_node`
- `knowledge_node_relation`
- `user_knowledge_progress` is read with `arch-study` ownership.

## Boundaries

- Do not own per-user learning plan state; that is `arch-study`.
- Do not own learning resources; that is `arch-resource`.
- Do not own Nebula task orchestration; that is `arch-explore`.

## Implementation Notes

- Keep graph query DTO/VO reusable by `arch-study` without creating reverse dependencies.
- API enum names should match the interface docs; database enum translation belongs in assembler/service code.
- Graph/context outputs that feed Nebula or `ContextNormalizer` should stay stable and avoid leaking mapper/entity internals.
