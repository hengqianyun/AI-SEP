# Round 2 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC8-R2-solutionArchitect
planId: PLAN-WSC-8.2
round: 2
role: solutionArchitect
actorInstance: solution-architect-wsc-011-r2
snapshotIdAtReview: SNAP-WSC-008
decision: APPROVE
closedIssues:
  - ISSUE-SA-R1-001
  - ISSUE-SA-R1-002
  - ISSUE-SA-R1-003
  - ISSUE-SA-R1-004
  - ISSUE-SA-R1-005
  - ISSUE-SA-R1-006
openIssues: []
summary: |
  对照 SNAP-WSC-008（status=APPROVED）与 PLAN-WSC-8.2 Round 2：相对 PLAN-WSC-8.1，
  本候选稿已将 R1 本角色六条 ISSUE 的 closeWhen 全部落到可字面 scope-check 的文件级
  独占与 §3.1/§3.2 契约硬冻结。重点复检：905 denyModify + 906 独占 useCanWrite 且
  905→906 串行；904 显式 model 列表；maintenance `scope=full|myCatalog` 与 listProducts
  分离；路由常量 `/my-catalog`；§3.2 本企业 scope 推导与 904 enterprise_name 策略。
  架构维度无新增异议。本轮 APPROVE。不代关他人 ISSUE；不伪造他角色结论。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务边界 vs SNAP 十条 REQ | 通过 — §7 映射完整；901 浏览 / 902 座序图 / 903 契约 / 904 用户企业 / 905 list+RBAC / 906 壳层 / 907 我的目录 / 908 E2E 归属合理 |
| 写集冲突 / 互斥（R1 复检） | **通过** — 905∥906 已改为 denyModify + 串行；904 model 显式列表；见下方 closeWhen 核验 |
| 分层约束（DEC-WSC-005 / CLAUDE.md） | 通过 — §0.2/§1.3 禁止业务域顶层包；Java 写集落在按类型分层路径 |
| 预落地对账 | 通过 — §0.2 列 void 前 901/906/903 迹象；路由冻结 `ROUTE_MY_CATALOG=/my-catalog`；验收以 SNAP+acceptance 为准 |
| DAG A→B→C→D→E | 通过 — 无环；903 在 901+902 后；904→905 串行；905→906 串行；禁止 903∥907 |
| 契约 2.3.2 / 实现边界 | **通过** — §3.1 硬冻结 maintenance scope、matrix 字面表、listProducts 无 myCatalog；903 独占 contracts/api |
| 座序图 API（l1 / 无企业过滤） | 通过 — 902 独占 L2Distribution*；905 denyModify 同路径；REQ-CAT-018 负例在 902/905/908 |
| Flyway / 企业实体 | 通过 — 904 独占 sql/migration；§3.2 + 904 acceptance 写明 enterprise_name 策略与会话单一真源 |
| 维护页复用（907） | 通过 — full vs myCatalog 参数化；契约与 905 interceptor / 907 BE 边界清晰 |
| BE/FE 边界 | 通过 — 905 仅 BE scope；906 独占 FE 门控与 useCanWrite；907 维护页 + 串行 routes 增量 |

## R1 ISSUE closeWhen 核验

| id | sev | closeWhen 要求 | PLAN-WSC-8.2 证据 | 本轮 |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | 明文三选一；改写 writeSet/denyModify/并行表 | **方案 (A)+(B)**：§0.3 — 906 **独占** `useCanWrite.ts`+spec；905 **denyModify** 同路径；905 writeSet **不含** composable；§1.4 / §6 并行表 — **禁止 905∥906**、`dependsOn` 905→906；删除「写集无交」 | **关闭** |
| ISSUE-SA-R1-002 | P1 | 904 `model/**`→显式列表；deny 非用户 catalog model | §5 TASK-904 writeSet 字面列 `SessionPrincipal`、`AdminUserCreate/Update/Response` 等；**无** `model/**` glob；denyModify `CatalogProduct*.java`（及一切 catalog 相关 model） | **关闭** |
| ISSUE-SA-R1-003 | P1 | §3.1 + 903 acceptance 硬冻结 maintenance full vs myCatalog | §3.1 — `GET /catalog/maintenance/entries?scope=full\|myCatalog`（OpenAPI enum）；PROVIDER full→403；matrix 含 `catalogMaintenanceApi` / `myCatalogApi`；**删除** listProducts 上 myCatalog；903 acceptance 列 maintenance scope 与 matrix 逐 cell 交叉检查 | **关闭** |
| ISSUE-SA-R1-004 | P2 | 固定 API/路由/矩阵 key；§0.2/906/907/908 同一常量 | §0.2 **路由冻结** — `ROUTE_MY_CATALOG=/my-catalog`；void 前 `/my-maintenance` 迁移说明；906/907 acceptance、§6.1 E2E 均引用 `/my-catalog`；§3.1 明确 myCatalog 走 maintenance API 非 listProducts | **关闭** |
| ISSUE-SA-R1-005 | P2 | §3.2 或 905 acceptance 明文 enterprise scope 推导 + 测试种子 | §3.2 — `create_by → sys_user.enterprise_id` 与会话 `enterpriseId` 比较；myCatalog ADMIN/PROVIDER 规则；905 acceptance + testScope 含同企多 ADMIN、跨企负例、篡改 enterprise 参数负例 | **关闭** |
| ISSUE-SA-R1-006 | P2 | 904 acceptance：(a) enterprise_id FK + enterprise_name 策略；(b) 幂等迁移；(c) 会话单一真源 | §3.2 `enterprise_name` 列策略；TASK-904 acceptance — 回填/deprecate/兼容、Flyway 幂等、principal `enterpriseId`+`enterpriseName` 单一真源；testScope 含既有 `enterprise_name` 数据迁移 smoke | **关闭** |

## 架构抽查（Round 2 增量）

### 905 / 906 useCanWrite 互斥（ISSUE-SA-R1-001）

- §0.3 互斥总览：`useCanWrite.ts` → **906 独占全文**；905 **denyModify**。
- TASK-WSC-905 objective 明示「**不改** FE `useCanWrite.ts`（归 906）」；writeSet 仅 BE + 集成测。
- TASK-WSC-906 writeSet 含 composable 全文；acceptance 要求导出与 §3.1 matrix 一致（含 ADMIN 产品写）。
- §6 并行表：**禁止 905∥906**；906 `dependsOn` 905 — 满足 closeWhen 方案 A 与串行交接，Orchestrator 可机械 scope-check。

### 904 model 写集（ISSUE-SA-R1-002）

- 相对 PLAN-WSC-8.1 的 `model/**` glob，8.2 改为具名 SessionPrincipal / AdminUser* 路径。
- denyModify 字面禁止 catalog 相关 model，避免 905/907 scope DTO 被 904 越界修改 — 与 WSC-5.2 R2 同类模式一致。

### §3.1 maintenance scope（ISSUE-SA-R1-003）

- ADMIN 双入口后端语义闭合：`scope=full` = 全平台维护；`scope=myCatalog` = ADMIN 本企业 / PROVIDER ownCreateBy。
- listProducts 仅 `mine=true`→本企业，**无** `scope=myCatalog` — 消除 list vs maintenance 契约分叉。
- §4 / §3.1 matrix 与 903 acceptance 四头同步要求对齐 — 907 参数化维护页有单一契约真源。

### 路由 /my-catalog 与企业 scope（ISSUE-SA-R1-004 / 005）

- 全计划常量 `ROUTE_MY_CATALOG` 统一菜单、routes、深链、E2E（§6.1 场景 5–6）。
- §3.2 权威推导规则与 SNAP REQ-CAT-016/017/018 一致；产品表不新增 `enterprise_id` 列，scope 经用户联结 — 架构可实施且与 904 Flyway 边界不冲突。

### 904 enterprise_name 迁移（ISSUE-SA-R1-006）

- §3.2 + 904 acceptance 三联闭合：FK 策略、幂等、会话单一真源 — 避免 `enterprise_name` 与 `enterprise_id` 双源漂移。

### 仍共享但可接受的串行写集

- `router/routes.ts`：906 导航/meta → 907 页级 guard 增量；§0.3 串行拥有权与 R1 判定一致。
- `CatalogMaintenancePage` 参数化 full vs myCatalog — 非 fork，符合 REQ-CAT-013 / REQ-CAT-017。

### 无新增 ISSUE

- 未发现新的同路径双作者、过宽 glob、maintenance/list scope 分叉或与 repos.yaml / 分层铁律冲突。

## 关闭的异议

| id | severity | disposition | note |
|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | **CLOSED** | closeWhen 方案 (A)+串行 (B) 已落地 |
| ISSUE-SA-R1-002 | P1 | **CLOSED** | 显式 model 列表 + catalog deny 已落地 |
| ISSUE-SA-R1-003 | P1 | **CLOSED** | §3.1 maintenance `scope=full\|myCatalog` 硬冻结 |
| ISSUE-SA-R1-004 | P2 | **CLOSED** | `ROUTE_MY_CATALOG=/my-catalog` 全计划统一 |
| ISSUE-SA-R1-005 | P2 | **CLOSED** | §3.2 + 905 testScope 推导与种子已落地 |
| ISSUE-SA-R1-006 | P2 | **CLOSED** | 904 acceptance enterprise_name 策略已落地 |

## 仍开放的异议（本角色）

无（`openIssues: []`）。

## 无异议项（延续 R1）

- 903 **唯一**写 `contracts/**` + `frontend/src/api/**`；禁止 903∥907。
- 901 ∥ 902 Wave A：`CatalogBrowsePage` 筛选 vs `CirculationSeatMap` 互斥 deny。
- 902 可先 BE、903 对账 OpenAPI `l1CategoryId` — FE/BE 解耦可接受。
- Out of Scope 与 SNAP 一致；REQ-SHELL-010（P1）不阻塞 A/B。
- 不代替 securityOperations / apiDataDesigner / qaStrategist 裁定；不以预落地 void 代码代替委员会共识。

## 决策

`APPROVE` — 解决方案架构维度 R1 异议 **6/6 关闭**；本轮无新开 ISSUE。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-011-r2`；**只关闭本角色 ISSUE-SA-R1-001..006**；**不**代关他人 ISSUE；**不**伪造他角色 `APPROVE`；**不**修改 plan/state/events；**不** commit。
