# DEV-TASK-WSC-307

```yaml
taskId: TASK-WSC-307
actorInstance: developer-wsc-307
runId: RUN-WSC-005
status: READY_FOR_REVIEW
completedAt: 2026-08-02T21:37:00+08:00
kind: HOTFIX
reqs:
  - REQ-API-001
```

## 摘要

HOTFIX：`parseYamlDocument` 对 `key: |` / `key: >`（及可选 `|-`/`>+` 等指示）按块标量收集后续更深缩进行拼成字符串，避免块内无冒号行（含 `- ` 列表行）被当成嵌套 map 而抛「非合法 YAML 行」；父映射可继续解析 `version:` 等键。Vitest 覆盖含 `description: |` 且块内有 `- ` 行的 `/users` GET 样例。状态 **READY_FOR_REVIEW**。

## 变更（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/editor/utils/parseOpenApi.ts` | REQ-API-001 | `isBlockScalarIndicator` + `readBlockScalar`；映射/列表项值遇 `|`/`>` 时吞块内容为字符串，不再压嵌套 map |
| `frontend/src/features/catalog/editor/utils/parseOpenApi.spec.ts` | REQ-API-001 | 块标量 `info.description: |`（含 `- ` 行）→ 解析出 GET `/users` |
| `planning/tasks/DEV-TASK-WSC-307.md` | — | 本报告 |

未改：backend / contracts / api / state / events / REV / 其它 frontend 路径。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-API-001** | OpenAPI YAML 导入支持字面量/折叠块标量，常见多行 `info.description` 可解析出 endpoints |

## 自测

```text
cd frontend
pnpm exec vitest run src/features/catalog/editor/utils/parseOpenApi.spec.ts --reporter=default
```

| 文件 | Tests | 结果 |
|---|---|---|
| `parseOpenApi.spec.ts` | 5 | **PASSED** |

人工建议（tester）：用含 `description: |` 多行（块内有 `- `）的 swagger YAML 导入，应成功列出 endpoints，不再报「非合法 YAML 行」。

## SCOPE / denyModify

- 仅改 writeSet 内 `parseOpenApi.ts` / `parseOpenApi.spec.ts` + 本 DEV
- 未改 REV / state / events / contracts / api / backend
- 未兼任 codeReviewer / tester；未标 VERIFIED
