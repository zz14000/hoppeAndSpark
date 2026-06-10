# arch-profile Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-profile` owns Spark onboarding, generated user profile, profile rebuild, long-term preferences, and user-center read models that are not owned by auth.

## P0 Scope

Recommended window: W2 identity and onboarding.

P0 APIs to implement:

- `GET /api/v1/onboarding/questions`
- `POST /api/v1/onboarding/answers`
- `POST /api/v1/onboarding/complete`
- `POST /api/v1/spark-profile/rebuild`

Profile/user-center APIs already documented:

- `GET /api/v1/users/{userId}`
- `PUT /api/v1/user/profile`
- `GET /api/v1/user/learning-stats`
- `GET /api/v1/user/collections`
- `POST /api/v1/user/collections`
- `DELETE /api/v1/user/collections/{collectionId}`

Main tables:

- `user_profile`
- `agent_memory` when profile generation stores long-term preference/memory

## Boundaries

- Do not implement user login/session/token logic here.
- Do not own resource or article favorites; aggregate by calling owner services only when that aggregation is explicitly required.
- Do not call Coze or LLM SDKs directly; use `arch-infra` gateways.
- Agent-generated profile behavior should stay mocked/placeheld until real Agent integration is explicitly started.

## Implementation Notes

- Profile generation can use mock agent/LLM output in P0.
- After onboarding completes, trigger or enqueue Strict plan initialization only through `arch-study`/`arch-task` boundaries when that contract exists.
- Align `ProfileBuilder` and L2 long-term profile/memory behavior with the Coze UML document; do not let profile generation mutate unrelated Agent session state directly.
