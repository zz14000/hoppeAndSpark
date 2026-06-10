# arch-practice Agent Guide

Source docs: `../AGENTS.md`, `../CODE_MAP.md`, `../../arch.md`, `../../Coze-多Agent-UML设计图-重构版.md`, `../../资源/Hope_and_Sparks_API接口文档.md`, `../../资源/hope_sparks.sql`.

## Mission

`arch-practice` owns practice sets, question details, answers, flags, Coach hints, submission, code-question text review, evaluation reports, and weak-point feedback signals.

## P0 Scope

Recommended window: W5 practice and feedback.

P0 APIs to implement:

- `GET /api/v1/exercise-sets`
- `GET /api/v1/exercise-sets/{exerciseSetId}`
- `GET /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}`
- `PUT /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/answer`
- `POST /api/v1/exercise-sets/{exerciseSetId}/submit`
- `GET /api/v1/exercise-sets/attempts/{attemptId}/report`

Documented supporting APIs:

- `PUT /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/flag`
- `POST /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/hint`
- `POST /api/v1/exercise-sets/{exerciseSetId}/questions/{questionId}/code-run`

Main tables:

- `question_bank`
- `practice_set`
- `practice_set_question`
- `user_question_record`
- `evaluation_report`

## Boundaries

- Do not introduce a real code execution sandbox in MVP.
- Do not own study plan state; weak-point feedback should call `arch-study` through an explicit service.
- Do not call LLM/Coach SDKs directly; use `arch-infra`.
- Leave Coach/LLM review empty or mocked until real Agent integration is explicitly started.

## Implementation Notes

- Objective questions can be graded synchronously.
- Subjective/code questions store answer text first; AI review can run synchronously in mock mode or through `arch-task` if long-running.
- Report output should include weak points in a shape that `arch-study` can consume later.
- Align Coach hints, evaluation reports, and weak-point payloads with the Coze UML document's standard Agent output protocol.
