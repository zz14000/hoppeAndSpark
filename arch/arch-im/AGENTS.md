# arch-im Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../资源/Hope_and_Sparks_API接口文档.md`.

## Mission

`arch-im` owns private messages, group chat, friend requests, and future WebSocket chat behavior.

## P0 Scope

Recommended window: no P0 implementation window.

`arch.md` explicitly places IM and full WebSocket messaging after MVP P0. Keep existing skeleton APIs compatible, but do not spend P0 effort here unless a later task promotes a specific endpoint.

Documented non-P0 APIs include:

- `GET /api/v1/im/conversations` or equivalent existing controller paths.
- chat history and send-message endpoints.
- private chat, group chat, and friend request endpoints.

## Boundaries

- Do not add P0 dependencies from auth/agent/community into IM just to satisfy current scope.
- Do not introduce WebSocket infrastructure in P0 unless explicitly requested.
- Notifications/reminders belong to `arch-notification`, not this module.

## Implementation Notes

- Keep DTO/VO conventions aligned so P2 can fill the module later.
