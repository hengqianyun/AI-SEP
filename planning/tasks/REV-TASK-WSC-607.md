# 代码审查

```yaml
reviewId: REV-TASK-WSC-607
taskId: TASK-WSC-607
planId: PLAN-WSC-5.2
round: 1
kind: HOTFIX
role: codeReviewer
actorInstance: code-reviewer-wsc-607-r1
decision: APPROVE
p0: 0
p1: 0
closedFindings: []
openFindings:
  - FIND-WSC-607-R1-001
  - FIND-WSC-607-R1-002
  - FIND-WSC-607-R1-003
contracts: wsc-contracts@2.2.0（matrix/OpenAPI 叙述同步；info.version 未 bump）
riskTags: [rbac-breaking, data-isolation]
securityReviewSuggested: true
securityReviewNote: |
  本角色不代做 security 结论。本 hotfix 相对 SNAP-WSC-005 扩大 PROVIDER 写面
  （catalogMaintenance UI/API + ownCreateBy 隔离），属 rbac-breaking；建议编排器
  评估是否另调度独立 securityReviewer（或人类 Security Owner）作门禁补强，
  不阻塞本轮 codeReview APPROVE → tester。
reviewedAt: 2026-08-06T17:05:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/runs/RUN-WSC-008/HOTFIX-TASK-WSC-607.md
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-607.md
  - 实地：contracts/rbac/matrix.yaml；openapi maintenance 叙述 + info.version
  - 实地：useCanWrite.ts(+spec)、WorkbenchLayout.vue、CatalogMaintenancePage.vue（只读）
  - 实地：RbacMatrix.java、WriteAuthorizationInterceptor（只读）、CatalogMaintenanceController/Service
  - 实地：RbacMatrixTest、CatalogMaintenanceIntegrationTest
  - 实地：e2e p0-wsc-v1.1 / v1.3 / v1.4 小改
mustDifferFrom:
  - developer-wsc-607
spotCheck: |
  matrix PROVIDER catalogMaintenanceUI=visible、Api on:ownCreateBy；USER 仍 ERR_MAINTENANCE_FORBIDDEN；
  category* 仍 ADMIN-only。RbacMatrix.canMaintainCatalog=ADMIN|PROVIDER；拦截器 MAINTENANCE→矩阵。
  Service：list=productsOwnedBy；requireOwnershipIfProvider 空/异主 403、缺失 404。
  分类按钮仍 categoryMaintainVisible（ADMIN）。E2E：v1.1/1.3 PROVIDER maintenance:true；
  v1.4 PROVIDER 点 nav-catalog-maintenance。未代 tester；未标 VERIFIED；未写 state/events。
```

## Round 1 结论

**APPROVE** — **P0=0，P1=0**。字面 scope / `denyModify` 合规：未向 PROVIDER 开放分类树写；未见本任务归因改动用户管理实现体或座序图/L2。acceptance（菜单、PROVIDER `create_by` 隔离、ADMIN 全量、矩阵三端对齐、自动化正负例）源码可复核。开放 findings 仅 P2×1、P3×2，不阻塞进入独立 tester 门禁。

**安全门禁建议（非本角色结论）**：`riskTags` 含 `rbac-breaking` / `data-isolation`，建议编排器评估是否追加独立 `securityReviewer`；本审查**不**代写安全 APPROVE/REJECT。

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-607-R1-001 | P2 | **OPEN** | E2E 仅抽样 PROVIDER 侧栏/页可达（v1.1/1.3 矩阵 `maintenance:true`；v1.4 点 `nav-catalog-maintenance`）；未在浏览器层断言 list 仅本人或异主 403。后端集成测已覆盖正负例；hotfix acceptance 称 E2E 可选 | 可选：PROVIDER 创建产品→维护列表含本人/不含异主，或 API 抽样；不阻塞 | HOTFIX acceptance #5 |
| FIND-WSC-607-R1-002 | P3 | **OPEN** | OpenAPI `info.version` / contracts 包版本仍 `2.2.0`；description 已注明 HOTFIX-607 未 bump（与 DEV 声明一致） | 若发布门禁要求次版本递增再 bump；否则可保持关闭 | contracts |
| FIND-WSC-607-R1-003 | P3 | **OPEN** | `RbacMatrixTest.java` 物理路径在 `.../rbac/`，package 为 `com.shdata.datachain.common.security`（可编译；风格漂移） | 可选：路径与 package 对齐 | maintainability |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 scope / denyModify（分类树、用户管理、座序图） | **PASS** — `categoryMaintain*` / `canWriteCategory` / `canMaintainCategory` 仍仅 ADMIN；维护页「维护行业分类」钮仍 `categoryMaintainVisible`；未见 607 归因改 users CRUD / seat-map 实现 |
| acceptance #1 菜单 ADMIN+PROVIDER；USER 不可见 | **PASS** — `canMaintainCatalog`；侧栏 `nav-catalog-maintenance`；`WriteEntryDemo` 同源；Vitest 三角色 |
| acceptance #2 PROVIDER mine 隔离；越权 403/404；空归属 | **PASS** — `productsOwnedBy` + `isOwnedBy`；空/异主 `ERR_FORBIDDEN`；缺失 `404`；集成测 list/associate/batch |
| acceptance #3 ADMIN 全量不变 | **PASS** — 非 PROVIDER 走 `catalog.products()`；既有 ADMIN 单条/批量/分页用例保留 |
| acceptance #4 矩阵 / OpenAPI / RbacMatrix / useCanWrite / 拦截器 | **PASS** — 对齐；VERSION 未 bump 已文档化（→ FIND-002 P3） |
| acceptance #5 自动化 | **PASS（源码）** — RbacMatrixTest + CatalogMaintenanceIntegrationTest + useCanWrite.spec；E2E 小改合理。**正式执行权在 tester** |
| rbac-breaking → securityReviewer | **记录建议** — `securityReviewSuggested: true`；本角色不代做 security 结论 |
| 未代 tester / 未标 VERIFIED / 未写 state·events | **PASS** |
| `mustDifferFrom` developer-wsc-607 | **PASS**（`code-reviewer-wsc-607-r1`） |
| P0 / P1 | **P0=0；P1=0** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester PASS。
- **decision: APPROVE**（P0=0、P1=0；开放 P2×1、P3×2 可延期）。
- 建议编排器评估是否追加独立 **securityReviewer**（见 yaml `securityReviewSuggested`）。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 1 | FIND-001 E2E 未深测 mine 隔离（后端已覆盖） |
| P3 | 2 | FIND-002 未 bump VERSION；FIND-003 测试路径/package |
