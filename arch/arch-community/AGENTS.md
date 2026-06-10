# arch-community Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-community` owns articles, drafts, comments, likes, favorites, follows, and async moderation state transitions for article/comment content.

## P0 Scope

Recommended window: W6 community and moderation.

P0 APIs to implement:

- `GET /api/v1/articles`
- `GET /api/v1/articles/{articleId}`
- `POST /api/v1/articles`
- `POST /api/v1/articles/{articleId}/comments`
- `GET /api/v1/articles/{articleId}/comments`
- `POST /api/v1/articles/{articleId}/like`
- `POST /api/v1/articles/{articleId}/collect`

Documented supporting APIs:

- `POST /api/v1/articles/drafts`
- `POST /api/v1/articles/polish`
- `POST /api/v1/users/{userId}/follow`

P0 async moderation:

- article/comment publish writes pending-style status first.
- Redis Stream consumer reviews content asynchronously.
- status transitions support pending, published, risk, blocked, offline semantics.

Main tables:

- `blog_post`
- `blog_comment`
- `blog_like`
- `blog_favorite`
- `blog_view_log`
- `feedback_ticket` when high-risk content needs Manage review

## Boundaries

- Article favorites stay here; resource favorites stay in `arch-resource`.
- Do not implement Manage review endpoints here; expose service methods for `arch-manage`.
- Do not call moderation/LLM SDKs directly; use `arch-infra`.
- Leave Horizon/LLM moderation empty or mocked until real Agent integration is explicitly started.

## Implementation Notes

- List/detail APIs should hide blocked/offline content from normal users.
- Moderation consumer must be idempotent and update `arch-task` status when a task record exists.
- High-risk moderation can create a `feedback_ticket` for Manage dispute/review flow.
- If Horizon is used for article/comment review, map its `publish` / `revise` / `block` decisions from the UML document into community moderation states.
