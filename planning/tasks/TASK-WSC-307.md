# 任务包 TASK-WSC-307（HOTFIX）

```yaml
taskId: TASK-WSC-307
runId: RUN-WSC-005
status: VERIFIED
wave: HOTFIX
verifiedAt: 2026-08-02T21:41:00+08:00
evidenceId: EVID-TASK-WSC-307-1
evidence: planning/tasks/TESTRUN-TASK-WSC-307.md
kind: HOTFIX
goal: |
  OpenAPI YAML 导入支持字面量块标量 | / >（info.description 等多行），
  避免「接口定义解析失败：非合法 YAML 行」。
reqs: [REQ-API-001]
actorInstance: developer-wsc-307
```

## 现象（已复现）

前端 `parseYamlDocument` 遇 `description: |` 时按嵌套 map 处理，下一行无 `:` → `OpenApiParseError: 接口定义解析失败：非合法 YAML 行`。

## writeSet

- `frontend/src/features/catalog/editor/utils/parseOpenApi.ts`
- `frontend/src/features/catalog/editor/utils/parseOpenApi.spec.ts`
- `planning/tasks/DEV-TASK-WSC-307.md`

## denyModify

其余路径；state/events/REV 由 orchestrator 写。

## acceptance

- 含 `info.description: |` 多行（含块内 `- ` 列表行）的 OpenAPI 3 YAML 可解析出 endpoints
- 既有 shared fixture / JSON 路径不回归
- Vitest 覆盖块标量样例

## 派发

- 2026-08-02T21:36:00+08:00 PO 反馈 swagger 导入 yaml 解析失败
