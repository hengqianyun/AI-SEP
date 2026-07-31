# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-106-R1
taskId: TASK-WSC-106
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-106
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T17:40:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-106.md
  - planning/tasks/DEV-TASK-WSC-106.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet`：`frontend/src/features/catalog/maintenance/**` 与 `backend/.../catalog/maintenance/**`（含集成测）。未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`；**未触碰** detail/editor/chain（并行 TASK-WSC-105 工作区改动不归本轮）。REQ-CAT-007：状态页签、一二三级筛选、分页 `page`/`pageSize`/`total`、单条保存/取消、批量关联、非管理员页不可达 + API `ERR_MAINTENANCE_FORBIDDEN` 均有实现与测试支撑。SCOPE_AMEND（`StubWriteController` 去维护占位、`routes.ts` 注册 `/catalog/maintenance`）合理且最小，对齐 REV-102 关闭条件。审查复跑后端 4 测与 vitest 4 测均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-106-R1-001 | P2 | SCOPE_AMEND：`StubWriteController` 移除维护三端点、`routes.ts` 新增维护路由，不在正式 `writeSet`；属启动/可达必需，且与 REV-102 FIND-001/002 closeWhen 一致；导入 stub 仍保留给 107 | Orchestrator 在合入说明中标注 SCOPE_AMEND；107 替换剩余 import stub | REQ-CAT-007, REQ-SHELL-001 |
| FIND-WSC-106-R1-002 | P2 | 集成测覆盖 PENDING→MAINTAINED、批量、403、分页；**未**断言 `l1CategoryId`/`l2CategoryId`/`l3CategoryId` 筛选查询。后端 `matchesL1/L2/L3` 与前端筛控件已实现 | tester 手工或补集成断言三筛；可选加 query 参数负例 | REQ-CAT-007 |
| FIND-WSC-106-R1-003 | P2 | `@PostConstruct ensurePendingSamples` upsert 演示 PENDING 产品到共享 `CatalogBrowseSeedStore`；与 browse/105 共用内存种子，并行测序可能互相看见演示条目 | 夹具隔离或测试后清理；tester 勿假设 browse 种子仅有原产品 | REQ-CAT-007 |
| FIND-WSC-106-R1-004 | P3 | 非管理员 UI 不可达由 `CatalogMaintenancePage` `onMounted` + `catalogMaintenanceVisible` 实现；无页面级组件测（后端 GET/POST 403 已覆盖） | 可选：加页面测或 E2E 非管理员直链跳转 | REQ-CAT-007 |
| FIND-WSC-106-R1-005 | P3 | 审计日志 `AUTH_FORBIDDEN` 行打印 `code=ERR_FORBIDDEN`，响应体为 `ERR_MAINTENANCE_FORBIDDEN`（拦截器先 audit 再写专用码） | 审计传入实际 errorCode，便于运维对齐契约 | REQ-RBAC-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet`（`catalog/maintenance/**` + 对应测试） | PASS（对照 DEV 变更清单） |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**` | PASS（本任务交付面；工作区另有契约/api 改动不归本轮） |
| 未触碰 detail/editor/chain/**、browse/**、admin/**、import/** | PASS（∥105 detail/editor 另轨，不计入 106） |
| SCOPE_AMEND（StubWrite / routes）合理且最小 | PASS（去冲突映射 + 单路由；见 FIND-001） |

### 2. REQ-CAT-007

| 项 | 证据 | 结果 |
|---|---|---|
| 状态页签 全部/已维护/待关联 | `CatalogMaintenancePage` tabs；`statusTab`→`list(..., status)`；集成 PENDING | PASS |
| 一二三级筛选 | 页筛 `filter-l1/l2/l3`；Service `matchesL1/L2` + L3 equals | PASS（测缺口见 FIND-002） |
| 分页 page/pageSize/total | 后端 pageBody；前端 `pagination`；集成 `page=1/2 pageSize=5` | PASS |
| 单条维护保存/取消 | `saveEdit` PUT；`cancelEdit` 清编辑态 | PASS |
| 批量关联 + 数量 | `batch-bar`/`batch-count`；POST batch；集成 success/failure | PASS |
| 非管理员不可达 + API 403 | 页内 `replace('/catalog')`；PROVIDER GET / USER batch → `ERR_MAINTENANCE_FORBIDDEN` | PASS |
| 非叶节点挂载拒绝 | PUT L2 → `ERR_CATEGORY_LEAF_REQUIRED` | PASS |

### 3. 架构 / 安全

| 项 | 结果 |
|---|---|
| Controller → Service → CatalogBrowseSeedStore（未穿透无关层） | PASS |
| 写/维护权限后端强制（含 GET 列表）；UI 非唯一防护 | PASS（拦截器 `MAINTENANCE`；集成 403） |
| 经既有 `@/api/catalog` + `client`；无硬编码后端 URL | PASS |
| 无密钥入仓；无本任务 SQL/契约改动 | PASS |

### 4. 测试覆盖（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| 待关联→已维护 | PASS — `pending_to_maintained_singleAssociate` |
| 批量关联 + leaf 校验 | PASS — `batchAssociate_and_leafRequired` |
| 非管理员 403 | PASS — `nonAdmin_forbidden` |
| 分页 | PASS — `pagination_pagePageSizeTotal` |
| 前端 composable | PASS — vitest 4（页签/单条/批量/错误面） |
| 审查复跑 | 后端 4 tests PASSED；vitest 4 tests PASSED |

## 残余风险（交 tester）

- 工作区并存 TASK-WSC-105 detail/editor 等改动；取证以 `catalog/maintenance` 与 DEV 清单为 106 基线，勿把 105 回归算进本任务。
- 演示 PENDING 条目由 maintenance `@PostConstruct` upsert；browse/其他集成若断言产品全集，需排除或容忍 `prod-pending-*`。
- L1/L2/L3 筛选以代码审查 + 前端控件为主；建议 tester 补三筛手工路径。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED；未写 TESTRUN。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 2 |
