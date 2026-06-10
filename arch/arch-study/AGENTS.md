# arch-study Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-study` owns personalized study plans, study tasks, calendars, user knowledge progress, learning topology, and knowledge-point resource networks.

## P0 Scope

Recommended window: W4 resource and study path.

P0 APIs to implement:

- `GET /api/v1/learning-plans/current`
- `POST /api/v1/learning-plans/generate`
- `PUT /api/v1/learning-plans/{planId}/adjust`
- `GET /api/v1/learning-plans/{planId}/topology`
- `GET /api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resource-network`
- `GET /api/v1/learning-plans/{planId}/topology/nodes/{nodeId}/resources`

Documented calendar APIs can be included after the plan/topology path is stable:

- `GET /api/v1/calendar/events`
- `POST /api/v1/calendar/events`
- `PUT /api/v1/calendar/events/{eventId}`
- `DELETE /api/v1/calendar/events/{eventId}`

Main tables:

- `study_plan`
- `study_task`
- `user_knowledge_progress`
- `user_learning_record`

## Boundaries

- Do not own resource records; read through `arch-resource` service/facade.
- Do not own knowledge node master data; read through `arch-knowledge`.
- Do not call Strict/Coze directly; use `arch-infra`, and track long-running generation with `arch-task`.
- Leave Strict/Coze plan generation empty or mocked until real Agent integration is explicitly started.

## Implementation Notes

- Validate plan ownership for every `{planId}` request.
- Resource network responses should expose frontend target types, not database enum names.
- Practice weak-point feedback can update study progress only through an explicit service method owned here.
- Align Strict plan generation/adjustment inputs and outputs with the Coze UML document before mapping them into `study_plan`, `study_task`, and topology VO responses.
