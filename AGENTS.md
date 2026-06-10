# Hope and Sparks Agent Guide

This file is the repository-level entry point for coding agents.

## Source Documents

- `arch.md`: backend architecture source of truth.
- `Coze-多Agent-UML设计图-重构版.md`: Coze multi-agent orchestration, agent roles, task protocols, review/revise loop, and two-level memory design.
- `arch/AGENTS.md`: working guide for the Maven backend project.
- `arch/CODE_MAP.md`: module ownership and code navigation map.
- `资源/Hope_and_Sparks_API接口文档.md`: frontend/Manage API contract.
- `资源/hope_sparks.sql`: schema source when API docs and DTO/VO details disagree.

## Working Rules

- For backend implementation, work under `arch/` and follow `arch/AGENTS.md`.
- For Agent/Coze work, read `Coze-多Agent-UML设计图-重构版.md` before changing `arch-agent`, `arch-infra`, `arch-explore`, `arch-study`, `arch-practice`, or any Horizon review/revise behavior.
- Write code at an ordinary university-student level, i.e. `普通大学生水平`: clear, direct, and easy to explain in a demo or defense.
- Avoid complex abstractions, clever generic code, reflection, deep inheritance, or heavy framework tricks unless an existing file already requires them.
- Add comments to new code. Prefer short class/method comments and key-step comments that explain business intent.
- `Agent 接入先空着`: leave Agent/Coze real integration empty for now. Keep interfaces, mock implementations, placeholders, or TODO notes, but do not wire real Coze calls or credentials.
- Keep Coze SDK and external provider details behind `arch-infra` gateways.
- Keep business state in the owning module; do not move generated resources, study plans, KB documents, or community moderation state into generic Agent code.
- Do not commit credentials or generated local artifacts.

## Coze Implementation Order

When implementing the multi-agent orchestration from the UML document, prefer this order:

1. `TaskPlanner`
2. `ContextNormalizer`
3. `TaskScheduler`
4. `Aggregator`
5. `ReviewPackBuilder`, `Horizon`, and the revise loop
6. L1/L2 memory integration through `ProfileBuilder` and `Aggregator`
