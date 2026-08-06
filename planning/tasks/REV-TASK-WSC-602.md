# 代码审查

```yaml
reviewId: REV-TASK-WSC-602
taskId: TASK-WSC-602
planId: PLAN-WSC-5.2
round: 2
role: codeReviewer
actorInstance: code-reviewer-wsc-602-r2
decision: APPROVE
p0: 0
p1: 0
closedFindings:
  - FIND-WSC-602-R1-001
  - FIND-WSC-602-R1-002
openFindings:
  - FIND-WSC-602-R1-003
  - FIND-WSC-602-R1-004
  - FIND-WSC-602-R1-005
  - FIND-WSC-602-R2-001
contracts: wsc-contracts@2.2.0（只读对照；非本任务 writeSet）
reviewedAt: 2026-08-05T17:24:40+08:00
basedOn:
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-602.md（round 2 修复声明）
  - planning/tasks/REV-TASK-WSC-602.md（Round 1 REQUEST_CHANGES）
  - planning/approved/PLAN-WSC-5.2.md (§3.2 / TASK-WSC-602)
  - 实地：WorkbenchLayout.vue、routes.ts、UsersAdminPage.vue(+spec)；useCanWrite* 只读核对
mustDifferFrom: developer-wsc-602 / developer-wsc-602-r2
```

## Round 2 结论

**APPROVE** — 本轮 **P0=0，P1=0**。FIND-WSC-602-R1-001 / 002 均满足 `closeWhen`，予以关闭。无新增 P0/P1。开放项均为 P2/P3（提交隔离、软删用户名、字符串级 spec、既有 `canMaintainCatalog` 读依赖），不阻塞进入独立 tester 门禁。正式 VERIFIED 仍须独立 tester / securityReviewer / migrationReviewer；本报告不代替其门禁。

### closeWhen 核验

| finding | 结果 | 证据 |
|---|---|---|
| FIND-WSC-602-R1-001 | **CLOSED** | `WorkbenchLayout.vue`：无 `myProductsVisible` / `nav-my-products` / `/my-products` / `isActive('/my-products')`；仅 `nav-users`→`/admin/users` + footer `session-role-label`。`routes.ts`：仅新增 `/admin/users`；无 `/my-products`、无 `mode: 'catalog'`/`mine` props。符合 §3.2 SA-004 602 白名单 |
| FIND-WSC-602-R1-002 | **CLOSED** | `UsersAdminPage.vue`：内联 `role.value === 'ADMIN'`；**无** `useCanWrite` import。`WorkbenchLayout`：`userManageVisible = role.value === 'ADMIN'`；**无** `canManageUsers` / `canSeeMyProducts` import。spec 断言上述门禁与壳层白名单。DEV 本轮声明未改 `useCanWrite*`；602 表面不再依赖 R1 越界新导出 |

### `canMaintainCatalog` 残留评估（非 P0）

Layout 仍 `import { canMaintainCatalog } from useCanWrite` 用于「目录维护」菜单可见性。R1 FIND-002 焦点为 602 交付面绑定 `canManageUsers`/`canSeeMyProducts`（及改 denyModify 源）导致不可隔离交付——该焦点已消除。`canMaintainCatalog` 为既有分类/目录维护可见性读依赖（基线助手已有该导出；语义非 my-products/用户管理），**不构成 P0**；记为 **FIND-WSC-602-R2-001（P2）**，提交时仍须勿将工作区脏改的 `useCanWrite(+spec)` 暂入 602。

## Findings（全量状态）

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-602-R1-001 | P0 | **CLOSED**（R2） | 壳层/路由 my-products 越界已移除；见上表 | （已满足）从 602 diff 移除 my-products 菜单/路由/catalog mode；仅 admin-users + role-readonly | §3.2 SA-004；REQ-SHELL-001 |
| FIND-WSC-602-R1-002 | P0 | **CLOSED**（R2） | 602 表面内联 ADMIN；不依赖 canManageUsers/canSeeMyProducts | （已满足）内联 `role === 'ADMIN'`；不暂存 useCanWrite(+spec) | PP-002；denyModify |
| FIND-WSC-602-R1-003 | P2 | **OPEN** | 工作区仍有 `useCanWrite.ts(+spec)` 等 denyModify 脏改（含 603 向矩阵助手扩展）；非 602 writeSet | 编排/提交按 writeSet 隔离暂存；勿并入 602 | writeSet/denyModify |
| FIND-WSC-602-R1-004 | P2 | **OPEN** | `existsByUsername` 含软删行（未改） | 可选：未删除唯一或文档化 | REQ-USER-001 |
| FIND-WSC-602-R1-005 | P3 | **OPEN** | UsersAdminPage/RoleSwitcher spec 仍以源码字符串为主 | 可选 mount 测 | testScope |
| FIND-WSC-602-R2-001 | P2 | **OPEN** | Layout 仍读 `canMaintainCatalog`（目录维护）；非用户管理/my-products 门禁 | 可选：603 收口时一并整理；或接受既有读依赖。提交勿暂存 useCanWrite 脏 diff | §3.2；PP-002 |

P0/P1 已清零 → 本轮可 `APPROVE`。

## Round 2 检查清单（增量）

| 项 | 结果 |
|---|---|
| §3.2 壳层白名单：无 my-products | **PASS**（FIND-001 CLOSED） |
| denyModify useCanWrite：602 表面不依赖新导出；本轮未改源文件内容（脏树隔离见 FIND-003） | **PASS**（FIND-002 CLOSED） |
| 用户管理门禁内联 ADMIN | **PASS**（Layout + UsersAdminPage + spec） |
| `canMaintainCatalog` 残留 | **P2 only**（FIND-R2-001；不阻塞） |
| `mustDifferFrom` developer-wsc-602(-r2) | PASS（`code-reviewer-wsc-602-r2`） |

## 复跑测试摘要（R2）

| 命令 | 结果 |
|---|---|
| `pnpm exec vitest run`（authStore / RoleSwitcher / UsersAdminPage specs） | **PASSED** — 3 files / 10 tests（2026-08-05 R2） |
| 后端 security 集成测 | 未复跑（DEV：R2 未改后端；R1 已 PASS） |

未宣称完整 testScope / e2e / 安全 / 迁移正式 VERIFIED。

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester / securityReviewer / migrationReviewer。
- **decision: APPROVE**（P0/P1=0；可进入独立 tester 门禁）。提交前须按 FIND-003 / R2-001 隔离暂存，勿将 `useCanWrite*` 脏改并入 602。

## 计数（Round 2）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | R1 两项已关闭 |
| P1 | 0 | — |
| P2 | 3 | R1-003、R1-004、R2-001 |
| P3 | 1 | R1-005 |

---

## Round 1 历史（保留；已被 R2 覆盖决策）

```yaml
# historical — round 1
round: 1
actorInstance: code-reviewer-wsc-602-r1
decision: REQUEST_CHANGES
p0: 2
p1: 0
reviewedAt: 2026-08-05T17:05:00+08:00
```

**REQUEST_CHANGES** — P0=2（FIND-001 壳层 my-products 白名单越界；FIND-002 denyModify `useCanWrite` 被改且被 602 表面依赖）。验收面大体到位，但 P0 未清零不得 APPROVE。详见原 findings 表（现已并入上方全量表，R1-001/002 于 R2 关闭）。
