# DEV-TASK-WSC-903

```yaml
taskId: TASK-WSC-903
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: developer-wsc-903-sec-fix
status: READY_FOR_REVIEW
completedAt: 2026-08-17T11:15:00+08:00
fixRound: 1
fixCompletedAt: 2026-08-17T11:30:00+08:00
secFixRound: 1
secFixCompletedAt: 2026-08-17T12:22:00+08:00
reqs:
  - REQ-USER-002
  - REQ-RBAC-002
  - REQ-CAT-016
  - REQ-CAT-017
  - REQ-CAT-018
  - REQ-SHELL-009
  - REQ-CAT-015
blocksRelease: false
riskTags: [contracts-bump, rbac-breaking, enterprise-scope, auth-model-change]
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## objective

Wave B：按 PLAN-WSC-8.3 §3.1 冻结 **wsc-contracts@2.3.3 V1.6 增量**；四头版本对账；更新 matrix / OpenAPI / state-matrix / req-coverage；对齐生成 `frontend/src/api/**`。

## 预落地对账（§0.2）

| 项 | 预落地 | 对账动作 |
|---|---|---|
| `contracts/VERSION` | **2.3.3** | **保持**；合并 V1.6 增量至 2.3.3，**不再 bump** |
| `rbac/matrix.yaml` version | 2.3.2 / V1.5 语义 | 升至 **2.3.3**；§3.1 矩阵逐 cell 纠偏 |
| OpenAPI `info.version` | **2.2.0** | 闭合漂移 → **2.3.3** + V1.6 增量 |
| `ui/state-matrix.md` | 2.2.0 / V1.4 侧栏 | 闭合漂移 → **2.3.3** + V1.6 我的目录/ADMIN 双入口 |
| `mine` 叙述 | create_by-only | 改为 **本企业**（905 实现） |
| maintenance entries | 无 scope | 硬冻结 **`scope=full\|myCatalog`** |
| `l2-distribution` | 无 query | 增 **`l1CategoryId`**（902 已 BE） |
| Session / AdminUser | 仅 enterpriseName | 增 **enterpriseId**（904 落地） |
| `frontend/src/api/**` | 2.2.0 / 无 L2 带参 | 对齐 **2.3.3**；`getL2Distribution({ l1CategoryId })` |

## filesChanged

| 路径 | 说明 |
|---|---|
| `contracts/VERSION` | 保持 `2.3.3` |
| `contracts/openapi/openapi.yaml` | V1.6 增量：enterpriseId、mine 本企业、maintenance scope、l1CategoryId、supplierName、安全说明、ADMIN 产品写 |
| `contracts/rbac/matrix.yaml` | §3.1 字面表：myCatalog*、PROVIDER maintenance 403、ADMIN productWrite 200 ownEnterprise |
| `contracts/ui/state-matrix.md` | V1.6 侧栏：我的目录、ADMIN 我的产品 visible、PROVIDER 无目录维护 |
| `contracts/req-coverage.md` | SNAP-008 十条 REQ 映射 + 工程底座 2.3.3 |
| `contracts/errors/codes.yaml` | version → 2.3.3 |
| `contracts/security/sensitive-fields.md` | version → 2.3.3 |
| `frontend/src/api/client.ts` | `CONTRACT_VERSION = '2.3.3'` |
| `frontend/src/api/auth.ts` | Session enterpriseId/enterpriseName |
| `frontend/src/api/admin.ts` | AdminUser* enterpriseId |
| `frontend/src/api/catalog.ts` | `MaintenanceScope`；`ListProductsQuery`；`getL2Distribution(query)`；`listMaintenanceEntries` scope |
| `frontend/src/api/index.ts` / `overview.ts` / `chain.ts` | 版本头 2.3.3 |

**denyModify 遵守**：未改 features/layouts/router/backend/sql/e2e/planning/state/events。

## §3.1 矩阵交叉检查（逐 cell）

| 能力键 | ADMIN | PROVIDER | USER | matrix |
|---|---|---|---|---|
| catalogMaintenanceUI | visible | **hidden** | hidden | PASS |
| catalogMaintenanceApi (scope=full) | 200 | **403** | 403 | PASS |
| myCatalogUI | visible | visible | hidden | PASS |
| myCatalogApi (scope=myCatalog) | 200 ownEnterprise | 200 ownCreateBy | 403 | PASS |
| myProductsUI | visible | visible | hidden | PASS |
| productWriteUI / ImportUI | visible | visible | hidden | PASS |
| productWriteApi / ImportApi | 200 ownEnterprise | 200 ownCreateBy | 403 | PASS |

## REQ 映射

| REQ | 契约落点 |
|---|---|
| REQ-USER-002 | Session/AdminUser enterpriseId+enterpriseName |
| REQ-RBAC-002 | matrix §3.1；maintenance scope；ADMIN 写 200 |
| REQ-CAT-016 | mine=true → 本企业 description |
| REQ-CAT-017 | maintenance scope=myCatalog；state-matrix 我的目录 |
| REQ-CAT-018 | 全链无企业 filter + 安全说明 |
| REQ-SHELL-009 | state-matrix ADMIN 双入口 / PROVIDER 无维护 |
| REQ-CAT-015 | l2-distribution?l1CategoryId= |

## acceptance 自检

| 项 | 结果 |
|---|---|
| 四头同步 2.3.3（VERSION / matrix / OpenAPI / state-matrix） | PASS |
| OpenAPI enterpriseId、mine 本企业、maintenance scope、l1CategoryId、安全说明 | PASS |
| matrix 与 §3.1 逐 cell 一致 | PASS |
| state-matrix 我的目录 + ADMIN 我的产品 + PROVIDER 无目录维护 | PASS |
| V1.5 supplierName/未分类/mine/L2 不回退（OpenAPI supplierName + l1CategoryId） | PASS |
| req-coverage 十条 V1.6 REQ | PASS |
| api client 对齐 2.3.3 | PASS |
| 预落地漂移闭合（2.3.3 / 2.3.2 / 2.2.0） | PASS |

## 自测命令与结果

```bash
# P0 对账（ephemeral node 断言；33 项语义）
node contracts/_check-903-acceptance.mjs
→ ALL 903 P0 ACCEPTANCE CHECKS PASSED (exit 0)
# 脚本已删除；证据见本段与 acceptance 勾选

# 前端 typecheck
cd frontend && pnpm run typecheck
→ EXIT 2 — 预存 `WorkbenchLayout.vue` 重复标识符（906 写集；与 901 DEV 同口径）
  本任务 api/** 变更未引入新 TS 错误；906 修复后全仓 typecheck 可绿

# 契约 lint（写集外脚本滞后）
node tests/contracts/check-contracts.mjs
→ FAIL：仍硬编码 VERSION expected 1.1.0（非本任务 writeSet）
```

## 后续任务提示

- **904**：Flyway + 会话 principal enterpriseId 落地
- **905**：mine 本企业 / maintenance scope-aware RBAC 实现
- **902 FIND-001**：`CirculationSeatMap` 可改用 `getL2Distribution({ l1CategoryId })`（features 归 902/后续）
- **907**：`listMaintenanceEntries` 须显式传 `scope`（OpenAPI required）

待独立 codeReviewer / securityReviewer / tester；developer **不**标 VERIFIED。

## Round 1 修复（FIND-WSC-903-R1-001）

| 项 | 内容 |
|---|---|
| finding | FIND-WSC-903-R1-001（P1） |
| 根因 | `Product.createBy.description` 残留「mine=true 过滤依据」，暗示 create_by-only，与 L519 / REQ-CAT-016 本企业语义冲突 |
| 修复 | `contracts/openapi/openapi.yaml` `Product.createBy.description`：改为审计/归属字段说明；删除 mine 过滤依据表述；指向 `GET /catalog/products` 的 `mine` 参数（enterpriseId 比较，非 create_by-only） |
| closeWhen 对照 | ✅ 删除 mine=true 过滤依据 / create_by-only 暗示；✅ 改为 create_by 归属/审计说明；✅ 指向 listProducts `mine` 本企业语义（对齐 L519 与 REQ-CAT-016） |
| 范围 | 仅 writeSet 内 `contracts/openapi/openapi.yaml`（单行 description） |
| 未改 | state/events/REV；未 bump 版本；未 git commit |

**修复后 excerpt**（L1700–1703）：

```yaml
createBy:
  type: string
  nullable: true
  description: 创建者 userId（审计/归属字段，非本企业 scope 判定依据）。本企业产品范围见 `GET /catalog/products` 的 `mine` 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）。空/缺失不可被 PROVIDER 冒领
```

**REQ**：REQ-CAT-016

**status**：READY_FOR_REVIEW（Round 2 复审）

## 安全审查修复（FIND-SEC-WSC-903-001）

| 项 | 内容 |
|---|---|
| finding | FIND-SEC-WSC-903-001（P1） |
| 依据 | `ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-903.md` |
| 根因 | OpenAPI `Product.createBy.description`（L1703）已改为审计/归属字段，但 `frontend/src/api/catalog.ts` JSDoc 仍写「mine=true 过滤依据」，与 REQ-CAT-016 本企业语义冲突；905/906 只读 client 时可能实现 create_by-only |
| 修复 | 删除「mine=true 过滤依据」；`Product.createBy` JSDoc 改为与 OpenAPI L1703 同口径 |
| closeWhen 对照 | ✅ 删除「mine=true 过滤依据」；✅ 改为审计/归属字段、非本企业 scope 依据；✅ 写明 mine 用 enterpriseId 比较，非 create_by-only |
| 同类残留扫描 | `frontend/src/api/**` 全文检索 `mine=true 过滤依据` / `过滤依据` / `create_by-only`：仅 `catalog.ts` 一处（已修）。`ListProductsQuery.mine`「true → 本企业产品」与 `auth.ts` Session.enterpriseId「mine/myCatalog/写 scope 单一真源」口径正确，未改 |
| 未修（本轮非必须） | FIND-SEC-WSC-903-002 / 003（P2）：PUT/batch scope 归 905；PROVIDER `productImportApi.on` 不在本修复指令 |
| 范围 | 仅 writeSet：`frontend/src/api/catalog.ts`；未改 contracts（OpenAPI 已在 R1 对齐） |
| 未改 | state/events/REV；未标 VERIFIED；未 git commit |

**修复后 excerpt**（`frontend/src/api/catalog.ts` Product.createBy）：

```ts
/** 创建者 userId（审计/归属字段，非本企业 scope 判定依据）。本企业产品范围见 listProducts 的 mine 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）。空/缺失不可被 PROVIDER 冒领 */
createBy?: string | null
```

**REQ**：REQ-CAT-016

**status**：READY_FOR_REVIEW（安全审查复审）
