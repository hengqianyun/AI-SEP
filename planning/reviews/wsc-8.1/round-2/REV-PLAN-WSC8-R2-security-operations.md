# Round 2 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC8-R2-securityOperations
planId: PLAN-WSC-8.2
round: 2
role: securityOperations
actorInstance: security-operations-wsc-011-r2
snapshotIdAtReview: SNAP-WSC-008
decision: APPROVE
summary: |
  对照 PLAN-WSC-8.2 与 Round 1 ISSUE-SEC-WSC8-R1-001..006 的 closeWhen：
  ① 903/905/906 已挂 auth-model-change；§1.4/§6.2/§9 硬绑定独立 securityReviewer，未通过不得 VERIFIED；
  ② 904 riskTags 含 schema-migration；§6.2/§9 硬绑定 migrationReviewer；
  ③ §1.2/§3.1/903/905 强制 scope 仅信 SessionPrincipal，禁止客户端 enterprise 参数 IDOR，905 含篡改负例；
  ④ maintenance scope=full|myCatalog 硬冻结，矩阵分裂 catalogMaintenanceApi vs myCatalogApi，905 scope-aware 拦截与 PROVIDER 正/负例可测；
  ⑤ 905 空/缺失 create_by 产品 → 403 负例已写入 acceptance/testScope；
  ⑥ 904 OQ-V16-002 orphan 默认企业策略与负例验收已落盘。
  六条 ISSUE 均满足 closeWhen，本轮全部关闭。无新增 P0/P1 异议；不伪造高风险接受；不代批其他角色；不写 plan/state/events。
closedIssues:
  - ISSUE-SEC-WSC8-R1-001
  - ISSUE-SEC-WSC8-R1-002
  - ISSUE-SEC-WSC8-R1-003
  - ISSUE-SEC-WSC8-R1-004
  - ISSUE-SEC-WSC8-R1-005
  - ISSUE-SEC-WSC8-R1-006
openIssues: []
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-8.2.md`（`CANDIDATE` / `DRAFT` / Round 2；`basedOn: PLAN-WSC-8.1`） |
| 快照 | `SNAP-WSC-008`（`status: APPROVED`；PO 2026-08-17；本评审不批准 SNAP） |
| 对照 | Round 1 `REV-PLAN-WSC8-R1-security-operations.md` ISSUE-SEC-WSC8-R1-001..006 |
| policies | `ai/workflow/policies.yaml` → `riskTriggers.securityReviewer`: `auth-model-change` / `new-public-endpoint` / `handles-pii`；`migrationReviewer`: `schema-migration` / `data-backfill` |
| 本实例 | `security-operations-wsc-011-r2`；仅关闭本角色 ISSUE |

## Round 1 ISSUE 核验

| id | sev | closeWhen 核验证据（PLAN-WSC-8.2） | 本轮 |
|---|---|---|---|
| ISSUE-SEC-WSC8-R1-001 | P1 | **(1)** TASK-WSC-903 `riskTags` 含 **`auth-model-change`**；905、906 同步含该标签（§5 903/905/906）。**(2)** §1.4 执行硬门禁与 §6.2 第 5 条：命中 `auth-model-change` 须独立 **`securityReviewer`**，对照 enterprise scope / ADMIN 写开放 / PROVIDER maintenance 403 / 三角色 mine·全链矩阵；**未通过不得 `VERIFIED`**；§9 发布门禁第 5 条亦要求 securityReviewer 通过。**(3)** 既有 acceptance/testScope 保留且可测。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC8-R1-002 | P1 | **(1)** TASK-WSC-904 `riskTags`=`[schema-migration, handles-pii, enterprise-model]`，使用 policies 正式名 **`schema-migration`**（不再仅用 `migration`）。**(2)** §6.2 第 4 条与 §9 第 5 条：命中 **`schema-migration`** → 独立 **`migrationReviewer`**，**未通过不得 `VERIFIED`**。**(3)** OQ-V16-002 默认企业策略写入 904 acceptance（orphan 不得获跨企写权限）及 testScope 负例。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC8-R1-003 | P1 | **(1)** §1.2 假设「授权 scope **仅**从 `SessionPrincipal.enterpriseId` 推导；**禁止**客户端传 `enterpriseId` 作授权依据」；§3.1 OpenAPI 冻结行写明 **拒绝/忽略** 客户端 `enterpriseId` query/body。**(2)** TASK-WSC-905 `acceptance`：「**scope 仅信 SessionPrincipal**；篡改 enterprise query/body **不扩大**可见面」；`testScope` 含「篡改 enterprise 参数负例」。**(3)** TASK-WSC-903 `acceptance` 要求 OpenAPI 含全链无企业 filter **安全说明**（与 §3.1 同步）。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC8-R1-004 | P1 | **(1)** §3.1 硬冻结 `GET /catalog/maintenance/entries` query **`scope=full\|myCatalog`**；矩阵字面表分裂 `catalogMaintenanceApi`（`scope=full`）与 `myCatalogApi`（`scope=myCatalog`），PROVIDER 前者 403、后者 200 ownCreateBy。**(2)** TASK-WSC-905 `objective`/`acceptance`/`testScope`：`WriteAuthorizationInterceptor` scope-aware — PROVIDER `scope=full` → **403**、`scope=myCatalog` → **200**（service 层 create_by 过滤）；ADMIN full 全平台 / myCatalog 本企业；§4 单元格分项正/负例表。**(3)** DAG 保证 905 先于 907（905→906→907；905→907）；§0.3 905 独占 interceptor、907 denyModify interceptor。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC8-R1-005 | P1 | TASK-WSC-905 `acceptance`：「空/缺失/非当前授权范围 `create_by` 的产品，PROVIDER 与 ADMIN（myCatalog/mine 路径）编辑/导入/维护 → **403**」；`testScope` 含「空 create_by 负例」后端集成测。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC8-R1-006 | P2 | TASK-WSC-904 `acceptance`：OQ-V16-002 orphan 用户默认 enterprise 策略可审计；**不得**因默认策略获其他企业产品 mine/myCatalog **写**权限；`testScope` 含 orphan 默认策略负例（单默认企业仅演示种子、生产须补录）。满足 closeWhen。 | **CLOSED** |

## 安全运营焦点复检（无新 ISSUE）

| 维度 | 结论 |
|---|---|
| RBAC-002 企业 scope 总口径 | 通过 — §3.1/§3.2/§4 与 SNAP PO 锁定一致 |
| ADMIN 双入口（全量 vs 我的目录本企业） | 通过 — 907 参数化 + 908 场景 5/7；905 scope 分离 |
| PROVIDER 隔离（无目录维护 / 深链不可达） | 通过 — 矩阵 403 + 906/908 深链负例；myCatalog 维护 200 与 full 403 共存 |
| mine/myCatalog 防跨企业泄漏 | 通过 — §3.2 create_by→enterprise 推导 + 905 负例；R1-003/005 已关闭 |
| 会话 enterprise 绑定 | 通过 — 904 principal + scope 仅信会话；R1-003 已关闭 |
| denyModify / 写集：905 独占 RBAC 面 | 通过 — §0.3；906 独占 useCanWrite；905∥906 禁止并行 |
| 全链无企业过滤 | 通过 — 902/905/908 场景 3 |
| 隐藏 ≠ 授权 | 通过 — §1.4/§4.1；906 Vitest USER 深链负例 |
| riskTags → security/migration review | 通过 — R1-001/002 已关闭 |
| E2E 安全证据（908） | 通过方向 — §6.1 场景 3/4/6；实现阶段仍须实证 |
| 控制面伪造 REV/state | 通过 — 全任务 denyModify `ai/runs/**` |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 2 无新增 P0/P1 异议；R1 六条均已按 closeWhen 关闭 | — | — |

## 关闭清单

| 状态 | ISSUE ids |
|---|---|
| **closedIssues**（本角色） | ISSUE-SEC-WSC8-R1-001, ISSUE-SEC-WSC8-R1-002, ISSUE-SEC-WSC8-R1-003, ISSUE-SEC-WSC8-R1-004, ISSUE-SEC-WSC8-R1-005, ISSUE-SEC-WSC8-R1-006 |
| **openIssues**（本角色） | （空） |

## 无异议项 / 边界

- §0.3 **905 独占** `RbacMatrix` / `WriteAuthorizationInterceptor`；**906 独占** `useCanWrite.ts`；905→906 串行 — 降低并行误改鉴权面风险。
- maintenance `scope=full|myCatalog` 硬冻结（§3.1）消除 listProducts 与 maintenance scope 分叉 — R1-004 核心关切已闭合。
- §8 风险表 IDOR / PROVIDER maintenance 分裂 / mine 切换条目与 §3/§5 措施一致。
- 不代替 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor / apiDataDesigner / uxUiPlanner / maintainer 批准。
- 高风险接受未伪造；不代替人类 `securityOperationsOwner` 签署残余风险。
- 本 **APPROVE** 仅针对**计划文本**对 closeWhen 的满足；Wave B 实现阶段仍须实际调度 **`securityReviewer`**（903/905/906）与 **`migrationReviewer`**（904）并产出实证。
- 未写 `planning/proposals/**`、`ai/runs/**`。

## decision

**APPROVE** — ISSUE-SEC-WSC8-R1-001..006 全部满足 closeWhen 并关闭（closedIssueCount=6，openIssueCount=0）。无新增 REQUEST_CHANGES / BLOCK 项。
