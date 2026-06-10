# arch-auth Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-auth` owns Spark frontend user identity, login sessions, current user, user settings, and device/security features.

## P0 Scope

Recommended window: W2 identity and onboarding.

P0 APIs to implement from the existing docs:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/user/current`

Supporting APIs that are already documented but can stay outside the first P0 pass unless needed:

- `POST /api/v1/auth/password/reset-request`
- `POST /api/v1/auth/password/reset-confirm`
- `GET /api/v1/user/devices`
- `DELETE /api/v1/user/devices/{sessionId}`
- `PUT /api/v1/user/password`
- `PUT /api/v1/user/email`
- settings endpoints under `/api/v1/user/settings` if restored from API docs.

Main tables:

- `sys_user`
- `user_login_session`
- `user_settings`

## Boundaries

- Do not manage backend admins here; `sys_admin` and Manage tokens belong to `arch-manage`.
- Do not own Spark profile/onboarding state; that belongs to `arch-profile`.
- Do not own favorites aggregation; resource favorites belong to `arch-resource`, article favorites to `arch-community`.

## Implementation Notes

- Passwords must be hashed through infrastructure/security utilities.
- Login returns access token plus `sessionToken`; refresh validates `user_login_session.session_token`.
- `GET /api/v1/user/current` should include `onboarded` based on profile availability without making `arch-auth` own profile generation.
