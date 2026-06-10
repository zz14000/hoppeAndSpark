# Hope and Sparks Agent Architecture

## Summary

The Agent runtime is implemented inside the Java backend. Coze is not the main path. The current skeleton uses Java ports. When API keys are configured it calls real DeepSeek, DashScope, and Tavily services; mock adapters are only for local runs without credentials. LangChain4j/LangGraph4j dependencies are version-managed in the parent POM and remain available for the next graph/SDK wiring step.

No API key is committed. Runtime credentials must come from environment variables:

- `DEEPSEEK_API_KEY`
- `ALIYUN_DASHSCOPE_API_KEY`

## Gateway And Modules

- `arch-agent`: user-facing Agent sessions, multi-agent orchestration, specialist agents, prompts, memory/cache write decisions.
- `arch-infra`: external capability ports and adapters: LLM, embedding, rerank, Chroma, web search, Mermaid rendering, Redis Stream, file storage.
- `arch-kb`: knowledge-base document and chunk business boundary.
- `arch-task`: async task lifecycle for future long-running generation, parsing, rendering, and embedding jobs.
- `arch-agent-harness`: command-line harness for local Agent graph and tool validation.

The runnable flow is:

```text
AgentSessionController / AgentHarnessApplication
 -> AgentOrchestrationService
 -> SparkEntry
 -> TaskPlanner
 -> ContextNormalizer
 -> TaskScheduler
 -> Sage / Coach / Strict / Nebula
 -> Aggregator
 -> Horizon
 -> AgentRunResultVO
```

## Tool Registry

All external-like actions should go through `ToolRegistry` so calls are auditable.

Current tools:

- `kb_search`: Chroma vector retrieval with user/project isolation.
- `web_search`: web source candidate retrieval.
- `rerank`: DashScope rerank replacement point.
- `mermaid_render`: Mermaid script to PNG/SVG replacement point.
- `memory_read`: memory retrieval replacement point.
- `memory_write`: memory/cache write replacement point.

Each call records:

- tool name
- input summary
- output summary
- duration
- success flag
- failure reason
- timestamp

## Agent Roles

- `SparkEntry`: intent routing only.
- `TaskPlanner`: task split and target-agent selection.
- `Sage`: text Q&A, concept explanation, RAG-backed answers.
- `Coach`: solution steps, hints, mistake diagnosis.
- `Strict`: learning plans, checkpoints, adjustment rules.
- `Nebula`: Mermaid flowcharts, mind maps, knowledge/resource graph outputs.
- `Horizon`: review gate: publish, revise, or block.

## Memory

The skeleton models three levels:

- L1 session memory: recent conversation and current task context.
- L2 project memory: course/project progress, weak points, active plan.
- L3 user memory: stable preferences, long-term goals, repeated weak areas.

Current web cache candidate writes are persisted as local JSONL under `hope.ai.chroma.local-cache-dir`. Long-term memory persistence should map to:

- Redis for hot short-term windows.
- `agent_memory_summary` for rolling summaries.
- `agent_memory` for long-term memory.
- Chroma collections for semantic retrieval.

## Knowledge Base And Search Cache

Default Chroma isolation:

```text
tenant = user_{userId}
database = project_{projectId}
collections = edu_ground_truth, spark_personal_notes, video_transcripts, web_cache_candidates
```

Retrieval flow:

```text
query -> kb_search -> rerank -> Agent context -> citations
```

`EmbeddingGateway` is already exposed as the replacement point for real query/document embedding. The current harness keeps vector retrieval deterministic until Chroma ingestion is implemented end to end.

Web cache flow:

```text
query -> web_search -> cache candidate -> memory_write -> later KB review/import
```

Unreviewed web results must remain cache candidates. They must not be treated as authoritative `edu_ground_truth` content until reviewed or imported through KB governance.

## Real Adapter Switches

Without API keys the harness can still run local mock adapters. With API keys the following ports make real external calls and surface failures instead of silently falling back:

- `DEEPSEEK_API_KEY`: enables `OpenAiCompatibleLlmGateway` at `/chat/completions`.
- `ALIYUN_DASHSCOPE_API_KEY`: enables `OpenAiCompatibleEmbeddingGateway` at `/embeddings` and `DashScopeRerankGateway`.
- `SEARCH_PROVIDER=tavily` and `TAVILY_API_KEY`: enables `TavilyWebSearchGateway`.

Mermaid rendering writes a `.mmd` script into `hope.ai.mermaid.output-dir`. If `MERMAID_CLI`, `mmdc`, or `mmdc.cmd` is available, it also generates PNG/SVG. Otherwise the harness returns the script path with `mock=true`.

Local env can be loaded from `arch/.env` because both boot and harness import `optional:file:.env[.properties]`. Keep real secrets only in `arch/.env`; commit only `arch/.env.example`.

## Current Status

Usable now:

- Harness modes: `qa`, `steps`, `diagram`, `rag`, `graph` entry path.
- Prompt loading from `arch-agent/src/main/resources/prompts/`.
- Deterministic local adapters for no-key development.
- Real DeepSeek chat adapter when `DEEPSEEK_API_KEY` is set.
- Real DashScope embedding/rerank adapter when `ALIYUN_DASHSCOPE_API_KEY` is set.
- Real Tavily search adapter when `SEARCH_PROVIDER=tavily` and `TAVILY_API_KEY` are set.
- RAG harness can answer from Tavily web results, rerank through DashScope, generate through DeepSeek, return citations, and write web cache candidates to local JSONL.
- Chroma tenant/database naming contract in `ChromaScope`.
- Tool call audit records with tool name, input summary, output summary, duration, success, and failure reason.

Not implemented yet:

- Real LangGraph4j `StateGraph` execution; current graph is direct Java staged execution over the same state contract.
- Real Chroma REST/client read-write adapter and collection lifecycle management; current vector store is mock.
- End-to-end document ingestion, chunking, embedding, and persistence into Chroma collections.
- Persistent long-term memory storage; current `memory_write` only persists web cache candidates locally.
- Search result governance workflow that promotes `web_cache_candidates` into reviewed `edu_ground_truth`.
- Real file/storage registration for generated Mermaid images; current render tool returns filesystem paths.
- Unit/integration tests for every Agent node and tool adapter.

## Prompts

Prompt files live in:

```text
arch-agent/src/main/resources/prompts/
```

Current files:

- `spark_entry.md`
- `task_planner.md`
- `sage.md`
- `coach.md`
- `strict.md`
- `nebula.md`
- `horizon.md`

`PromptTemplateService` loads these prompts from the classpath. Future Manage prompt configuration can override this service without changing Agent code.

## Harness Usage

Compile:

```powershell
mvn -q -DskipTests compile
```

Install reactor artifacts before running a single harness module:

```powershell
mvn -q -DskipTests install
```

Run diagram mode:

```powershell
mvn -q -pl arch-agent-harness spring-boot:run "-Dspring-boot.run.arguments=--mode=diagram --query=二分查找流程图 --renderMermaid=true"
```

Run RAG mode:

```powershell
mvn -q -pl arch-agent-harness spring-boot:run "-Dspring-boot.run.arguments=--mode=rag --query=二分查找的时间复杂度"
```

Run steps mode:

```powershell
mvn -q -pl arch-agent-harness spring-boot:run "-Dspring-boot.run.arguments=--mode=steps --query=讲解一道二分查找题"
```

## Implementation Constraints

- Do not write secrets into source, docs, SQL seed data, or committed config.
- Business modules must depend on infra ports, not concrete SDK classes.
- Controllers stay thin; orchestration lives in services.
- Mermaid script is always returned; image generation is optional through `mermaid_render`.
- LangChain4j/LangGraph4j BOM versions are managed in the parent POM. SDK dependencies are still behind the `ai-frameworks` Maven profile until direct graph wiring is complete.
- Mock adapters must remain deterministic so frontend and harness tests can run without external credentials.
