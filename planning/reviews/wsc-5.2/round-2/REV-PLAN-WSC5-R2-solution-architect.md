# Round 2 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC5-R2-solutionArchitect
planId: PLAN-WSC-5.2
round: 2
role: solutionArchitect
actorInstance: solution-architect-wsc-008-r2
snapshotIdAtReview: SNAP-WSC-005
decision: APPROVE
closedIssues:
  - ISSUE-SA-R1-001
  - ISSUE-SA-R1-002
  - ISSUE-SA-R1-003
  - ISSUE-SA-R1-004
openIssues: []
summary: |
  对照 SNAP-WSC-005（IN_REVIEW）与 PLAN-WSC-5.2：相对 PLAN-WSC-5.1 Round 1，
  本候选稿在 §2.3 / §3.2 / §5 将写集冲突收口为可字面 scope-check 的文件级独占。
  R1 本角色四条 ISSUE 的 closeWhen 均已满足（001/002 方案 A；003 显式 model；
  004 方案 a+c），故全部关闭。架构维度：契约/实现边界、按类型分层、串行 DAG
  601→605、预落地对账权威仍可接受；无新增架构异议。本轮 APPROVE。
  不代关他人 ISSUE；不伪造他角色结论；不以预落地代码代替共识。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务边界 vs SNAP 六条 P0 | 通过 — §7 映射完整；601 契约 / 602 用户 / 603 我的产品+RBAC / 604 座序图 / 605 E2E |
| 写集冲突 / 互斥（R1 复检） | **通过** — 见下方 closeWhen 核验；文件级独占可机械 scope-check |
| 分层约束（DEC-WSC-005 / LAYOUT） | 通过 — 禁止新建域顶层包；L2 独立类仍落 `controller|service/.../browse/`（文件名互斥） |
| 预落地对账 | 通过 — §0.2 以 SNAP+acceptance 为权威；控制面 deny `ai/runs/**` |
| DAG 601→605 | 通过 — 无环；无同波并行；契约→会话→RBAC/mine→L2→E2E |
| 契约 / 实现边界 | 通过 — 601 独占 `contracts/**` + `frontend/src/api/**` |
| SNAP 门禁 | 记录 — 仍 `IN_REVIEW`；派发须正式批准或委员会确认范围；非本角色伪造 |

## R1 ISSUE closeWhen 核验

| id | sev | closeWhen 要求 | PLAN-WSC-5.2 证据 | 本轮 |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | 明文三选一；落到 603/604 writeSet/denyModify | **方案 (A)**：§2.3 / §3.2 — 604 独占 `L2DistributionController.java` / `L2DistributionService.java`；603 `denyModify` 二者且 writeSet 仅 `CatalogBrowseController/Service`；604 `denyModify` CatalogBrowse*；删除方法级散文例外；603 acceptance 要求预落地 L2 方法迁出 | **关闭** |
| ISSUE-SA-R1-002 | P1 | 明文三选一；改写 602/603 security 写集 | **方案 (A)**：602 writeSet 仅 PasswordHasher / AuthAuditLogger 等具名文件；`denyModify` `RbacMatrix.java` + `WriteAuthorizationInterceptor.java`；603 独占二者（§3.2 / TASK-602/603） | **关闭** |
| ISSUE-SA-R1-003 | P1 | 602 `model/**`→显式列表；deny 非用户 model；603/604 字面列 model | 602 writeSet：`LoginRequest` / `SessionPrincipal` / `Role` / `SwitchRoleRequest` / `AdminUser*`；denyModify：`Catalog*` / `Overview*` / `Distribution*` / `Stream*` / `TopItem*` / `Trend*` / `DemoRequest`；603 列 `CatalogProduct`/`CatalogCategory`；604 列 `DistributionSliceRecord` | **关闭** |
| ISSUE-SA-R1-004 | P2 | (a) 收口波次+不得回退 或 (b) 拆文件 或 (c) CR 白名单 | **(a)+(c)**：§3.2 壳层白名单 — 收口波次=**603**；602 仅 `admin-users`+角色只读、`/admin/users`；603 仅 `my-products`/`/my-products`；603 acceptance「不得回退用户管理菜单与角色只读区」 | **关闭** |

## 架构抽查（本轮增量）

- **字面互斥**：603∥604 browse 后端由「同 glob 双写」改为「文件名互斥」；602∥603 security 由「散文相关部分」改为「具名文件 + deny 矩阵/拦截器」。
- **壳层串行双写**：仍共享 `WorkbenchLayout.vue` / `routes.ts`，但白名单 + 603 收口 + 不得回退清单满足 R1 closeWhen，可检。
- **605**：E2E 独占 `tests/e2e/**`，不改变本角色写集架构结论；属 QA 域细节，本角色不代批。
- **无新增 ISSUE**：未发现新的同路径双作者、过宽 glob、或与 repos.yaml / LAYOUT 冲突。

## 关闭的异议

| id | severity | disposition | note |
|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | **CLOSED** | closeWhen 方案 (A) 已落地 |
| ISSUE-SA-R1-002 | P1 | **CLOSED** | closeWhen 方案 (A) 已落地 |
| ISSUE-SA-R1-003 | P1 | **CLOSED** | model 显式列表 + deny 已落地 |
| ISSUE-SA-R1-004 | P2 | **CLOSED** | closeWhen (a)+(c) 已落地 |

## 仍开放的异议（本角色）

无（`openIssues: []`）。

## 决策

`APPROVE` — 解决方案架构维度 R1 异议 **4/4 关闭**；本轮无新开 ISSUE。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-008-r2`；**只关闭本角色 ISSUE-SA-R1-001..004**；**不**代关他人 ISSUE；**不**伪造他角色 `APPROVE`；**不**写 `ai/runs/**`。
