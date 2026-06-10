# arch-agent Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-agent` owns local agent sessions, messages, SSE streaming, prompt business, memory summaries, and AI dispute reporting entrypoints.

## P0 Scope

Recommended window: W3 agent and Nebula entry.

P0 APIs to implement:

- `GET /api/v1/agents`
- `POST /api/v1/agent-sessions`
- `GET /api/v1/agent-sessions/{sessionId}/messages`
- `POST /api/v1/agent-sessions/{sessionId}/messages`
- `GET /api/v1/agent-sessions/{sessionId}/stream`
- `POST /api/v1/ai-disputes`

Main tables:

- `agent_chat_session`
- `agent_chat_message`
- `agent_memory`
- `agent_memory_summary`
- `agent_graph_thread`
- `agent_graph_checkpoint`
- `async_generation_task` through `arch-task` when needed
- `sys_agent_prompt`

## Boundaries

- Do not call Coze directly from controllers or services that can use `arch-infra` gateways.
- Do not implement real Agent/Coze calls yet; leave orchestration and provider calls as interfaces, mocks, or TODO placeholders.
- Do not store Coze Token or runtime secrets.
- Do not own Nebula exploration or generated resources; those belong to `arch-explore` and `arch-resource`.
- Do not make dispute processing decisions here; Manage processing belongs to `arch-manage`.

## Implementation Notes

- SSE should emit user-visible streaming chunks and finish/error events in a predictable shape.
- Persist local messages before/after gateway calls so conversation history survives retries.
- Store external conversation/message IDs for traceability.
- Follow the Coze UML implementation order for orchestration: `TaskPlanner`, `ContextNormalizer`, `TaskScheduler`, `Aggregator`, `ReviewPackBuilder` + `Horizon` + revise loop, then L1/L2 memory integration.
- Keep standard output fields stable: `task_id`, `source_agent`, `task_type`, `payload`, `status`, and review `final_decision`.
- Keep the first implementation simple and commented: plain services, plain DTO/VO objects, deterministic mock responses, and no complex orchestration engine yet.
