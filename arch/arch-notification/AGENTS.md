# arch-notification Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../资源/Hope_and_Sparks_API接口文档.md`.

## Mission

`arch-notification` owns notification list, read/delete actions, reminder delivery, and future realtime notification push.

## P0 Scope

Recommended window: no P0 implementation window.

Notifications are not part of `arch.md` P0. Keep existing skeleton APIs compatible and defer full behavior to P1 unless a later task explicitly promotes a notification flow.

Documented non-P0 APIs include:

- `GET /api/v1/notifications`
- mark read
- delete
- action callback

## Boundaries

- Do not implement WebSocket push in P0.
- Do not own study scheduling logic; `arch-study` owns plans/tasks/calendar.
- Do not own IM message delivery; `arch-im` owns chat.

## Implementation Notes

- Future reminder events should consume stable business events instead of polling business tables directly where possible.
