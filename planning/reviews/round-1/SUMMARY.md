# PLAN-WSC-1.0 Round 1 评审汇总

```yaml
planId: PLAN-WSC-1.0
round: 1
snapshotId: SNAP-WSC-001
consensus: NOT_REACHED
nextAction: planEditor-revise-to-PLAN-WSC-1.1
collectedAt: 2026-07-28T16:40:00+08:00
```

## 决策矩阵

| 角色 | 必需? | decision | ISSUE 数 | 路径 |
|---|---|---|---|---|
| productAnalyst | yes | REQUEST_CHANGES | 2 | REV-PLAN-R1-product-analyst.md |
| solutionArchitect | yes | REQUEST_CHANGES | 3 | REV-PLAN-R1-solution-architect.md |
| qaStrategist | yes | REQUEST_CHANGES | 6 | REV-PLAN-R1-qa-strategist.md |
| parallelPlanner | yes | APPROVE | 0 | REV-PLAN-R1-parallel-planner.md |
| planEditor | yes | APPROVE | 0 | REV-PLAN-R1-plan-editor.md |
| uxUiPlanner | optional | REQUEST_CHANGES | 3 | REV-PLAN-R1-ux-ui-planner.md |
| apiDataDesigner | optional | REQUEST_CHANGES | 4 | REV-PLAN-R1-api-data-designer.md |
| securityOperations | optional | REQUEST_CHANGES | 4 | REV-PLAN-R1-security-operations.md |

**门禁**：`passWhen: all-required-roles-approve` → **未通过**（3/5 必需角色 REQUEST_CHANGES）。

## 必须吸收的 P0 ISSUE（修订优先）

| id | 角色 | closeWhen 摘要 |
|---|---|---|
| ISSUE-SEC-R1-001 | securityOperations | 明确 V1 认证/会话模型与权限变更后写 API 拒绝可测验收 |
| ISSUE-API-R1-001 | apiDataDesigner | 契约含版本化目录快照结构并对齐 REQ-CHAIN-001 |
| ISSUE-QA-R1-001 | qaStrategist | 增加可执行 P0 Playwright E2E testScope/清单 |

## 必须吸收的 P1 及重复主题（合并修订）

- **OQ-004 / 其他数据产品**：PA-001、UX-002、API-004 → TASK-WSC-005 + 契约显式排除专属字段区
- **catalog 写集拆分**：SA-001 → 004/005 互斥路径
- **API client 所有权**：SA-002 → §3 矩阵增加 `frontend/src/api/**`
- **schema-migration 标签**：SA-003 → 从 005 移除或独立迁移任务
- **空态 / 上链版本断言 / RBAC 矩阵 / 分类删除用例**：QA-002..005
- **OVW 动态流数据契约 + fixture**：API-002、QA-006
- **错误码目录**：API-003
- **PII/敏感字段策略、审计日志、回滚口径**：SEC-002..004
- **UI 状态矩阵、敏感标识复制、NFR 门禁**：UX-001/003、PA-002

## Orchestrator 结论

- 不得进入 `PLAN_APPROVED` / 开发派发。
- 下一动作：`planEditor` 发布 `PLAN-WSC-1.1`，在修订说明中逐条引用 ISSUE id 与关闭方式；**不得**自行将 ISSUE 标为已关闭。
- 修订后启动 Round 2 独立评审；原异议提出者确认 closeWhen 满足后才可改 APPROVE。
