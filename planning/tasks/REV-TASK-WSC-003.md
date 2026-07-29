# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-003-R1
taskId: TASK-WSC-003
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-003
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
reviewedAt: 2026-07-29T16:25:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-003.md
  - planning/tasks/DEV-TASK-WSC-003.md
  - planning/approved/PLAN-WSC-1.1.md
```

## 结论

**APPROVE** — 本轮无 P0/P1。总览只读 API（metrics/trend/top/stream/distribution）字段对齐 OpenAPI 与 OVW 流契约；五端点均需登录会话否则 401；空库指标 0 / 流空数组 / 趋势全 0 点友好态；前端 §3.5 loading/empty/error+重试完备；fixture 三类流类型倒序可测。写集落在 `overview/**`（及 SCOPE_AMEND 允许的 overview 路由接线），未改 contracts/api/migration/其他 feature。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-003-R1-001 | P2 | 前端 vitest 仅断言 `STREAM_TYPE_LABEL` 三类中文；PLAN `testScope`「至少一条接口失败 error 反馈断言」未在本任务单测落盘（页面已有 `*-error` + 重试） | 补一条 mock 失败 → error 态断言，或交独立 tester 在 TESTRUN 覆盖 | REQ-OVW-001..005, §3.5 |
| FIND-WSC-003-R1-002 | P2 | `OverviewPage` 使用原生 HTML/CSS 柱图，未消费已声明的 `ant-design-vue` / 图表库（RULE-ORG-STACK 弱对齐；与 002 壳层同类） | 后续 UI 统一任务改用栈内组件，或书面冻结「003 原生可接受」 | REQ-OVW-002 |
| FIND-WSC-003-R1-003 | P3 | DEV 声明未改 `routes.ts`，工作区 `/overview` 已挂载 `OverviewPage`（符合 SCOPE_AMEND；或为编排器接线） | 校正 DEV 叙述或明确编排器接线责任 | SCOPE_AMEND |
| FIND-WSC-003-R1-004 | P3 | 集成测仅对 `/overview/metrics` 断言 401；其余四端点同 `withSession` 门禁，未逐一负例 | 可选补全 401 覆盖或 tester 抽样 | 安全门禁 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. allowModify / denyModify 边界

| 项 | 结果 |
|---|---|
| 业务变更 ⊆ `frontend/src/features/overview/**`、`backend/**/overview/**`、对应测试 | PASS |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/db/migration/**` | PASS（只读消费 `@/api/overview`） |
| 未改 `security/**`、`rbac/**`、catalog/chain feature | PASS（会话复用 `AuthController.currentPrincipal`） |
| `routes.ts` | 工作区 overview 已接线；属 SCOPE_AMEND 允许范围；detail 等未动 | PASS |

### 2. 契约对齐（OpenAPI + stream-events）

| 项 | 证据 | 结果 |
|---|---|---|
| metrics 四字段 | `assetTotal`/`activeEnterprises`/`todayAttestations`/`updatedAt` | PASS |
| stream 六字段 + 三类枚举 | `type/subject/actionSummary/relativeTime/occurredAt/chainRecordId`；fixture 含 CATALOG/DATA/TRADE | PASS |
| 倒序 | `occurredAt` 降序；集成测断言 | PASS |
| 不依赖未实现业务 API | `OverviewDataStore` seed/fixture | PASS |

### 3. 安全（读接口需登录）

| 项 | 证据 | 结果 |
|---|---|---|
| 五 GET 均 `withSession` | `OverviewController` | PASS |
| 未登录 401 + `code=401` | `overview_requiresLogin` | PASS |

### 4. 空态 / §3.5

| 项 | 证据 | 结果 |
|---|---|---|
| 空库指标 0、流 `items=[]`、趋势点 count=0 | `metricsAndStream_emptyStore_returnFriendlyZeros` | PASS |
| UI empty/error/loading + 重试 | `OverviewPage` `data-testid=*-empty|*-error` | PASS |

### 5. 自测证据（审查侧抽样，不代替 tester）

| 项 | 结果 |
|---|---|
| DEV：`OverviewApiIntegrationTest` 5 PASS | 已声明 |
| DEV：vitest `useOverviewData.spec.ts` 1 PASS；typecheck exit 0 | 已声明 |

## 残余风险（交 tester）

- 前端 error 反馈缺单测断言，须在 TESTRUN 用 mock/联调覆盖。
- 图表窗口重绘依赖 ResizeObserver；桌面分辨率 NFR 需人工/E2E 抽样。
- 默认 seed 非空；空态依赖测试 `clear()` 或等价环境。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称 testScope 通过。
- **decision: APPROVE**（进入独立 tester 门禁）。
