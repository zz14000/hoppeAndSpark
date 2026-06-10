# arch-boot Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../资源/Hope_and_Sparks_API接口文档.md`.

## Mission

`arch-boot` assembles the runnable Spring Boot application. It owns startup, global config, security wiring, module scanning, and a few global controllers.

## P0 Scope

Recommended window: W1 foundation.

Global P0 APIs:

- `GET /api/v1/health`
- `GET /api/v1/app/info`
- `POST /api/v1/uploads/token`
- `POST /api/v1/uploads/complete`

Security P0 wiring:

- public access for login/register and documented public endpoints.
- frontend user token resolves to `sys_user`.
- Manage admin token resolves to `sys_admin`.
- `/api/v1/manage/**` requires ADMIN identity and then dynamic RBAC authorization.
- `ManageDynamicAuthorizationFilter` checks Controller base-path resources from `sys_admin_resource`.

## Boundaries

- Do not put business controllers in `arch-boot`.
- Upload endpoints only delegate to `arch-infra` file service and record file metadata; they do not decide business ownership.
- Do not place user, admin, resource, KB, or community state transitions here.

## Implementation Notes

- Keep default local startup possible with mock external adapters.
- Keep `ApiAuthenticationEntryPoint` and `ApiAccessDeniedHandler` returning unified API responses.
