# 代码审查报告 — TASK-WSC-909

```yaml
reviewId: REV-TASK-WSC-909
taskId: TASK-WSC-909
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-909
mustDifferFrom: developer-wsc-909
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T16:40:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/skills/code-review/SKILL.md
  - ai/runs/RUN-WSC-011/HOTFIX-TASK-WSC-909.md（验收权威）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-909.md（只读对照，不采信为 PASS）
  - 实地：CirculationSeatMap.vue(+spec)、useCatalogBrowse.ts(+spec)、CatalogBrowsePage.vue(+spec)、frontend/src/api/catalog.ts
  - 实地：CatalogBrowseController/Service、CatalogBrowseSeedStore、ProductEditorService、ProductImportService、ListProductsFilterSortIntegrationTest、ProductEditorIntegrationTest、openapi.yaml、contracts/VERSION
  - denyModify 对照：**/sql/**、tests/e2e/playwright.config.ts、p0-wsc-v1.4.spec.ts、RbacMatrix/WriteAuthorizationInterceptor（909 未改授权判定）
riskTags: [hotfix, blocksRelease, ux-count-audit]
kind: HOTFIX
note: |
  无 auth-model-change / schema-migration：本 REV 不代 securityReviewer / migrationReviewer。
  未代 tester 宣称 PASS / VERIFIED。未改被审源码 / state.yaml / events.jsonl / DEV。
```

## 结论

**APPROVE** — **P0=0，P1=0**。对照 `HOTFIX-TASK-WSC-909.md` 三项验收实地核验通过：座序图 h2 相对卡片水平居中且 L1 下拉仍在右侧可用；L3 `count` 取筛选全集 `l3Counts`、loadMore 不增大；创建/更新写路径服务端写入审计且客户端 body 无法覆盖，`create_by` 在 update 时保留，预览只读展示创建人/操作人。

字面 writeSet 内交付；未改 `CatalogProduct` / `CatalogEntityMapper` 的理由成立（导入成功行走 `ProductEditorService.create`）。无新 Flyway；未改 v14 e2e；未改授权拦截器判定。OpenAPI 纯加可选字段、未 bump `wsc-contracts`（仍 2.3.3）可接受。902 命名用例仍在且未删 L1 能力。

`CatalogBrowsePage.spec.ts` 全文件 ADMIN 不可写红测判定为 **906/V1.6 范围外已知债**，**不**因此对 909 REQUEST_CHANGES。开放 **P2×2** 不阻塞进入独立 tester。

## 三项验收（独立核验）

### 1. 座序图标题居中且 L1 仍可用

| 项 | 实地 | 结果 |
|---|---|---|
| h2「数据流通链」水平居中 | `.title-row` `position: relative`；`h2` `left: 50%` + `translateX(-50%)` + `text-align: center` | **通过** |
| L1 下拉仍在右侧可用 | `Select` `data-testid="seat-map-l1-select"` 仍为 `.title-row` 子节点；`.title-row` `justify-content: flex-end`；`.l1-select` `z-index: 1` | **通过** |
| 无企业筛 | `CirculationSeatMap.vue` 无 `enterprise` / `企业管理` / `industryCategory` | **通过** |
| 不与 Cascader 联动 | spec `does not couple to browse Cascader state` 仍断言无 `useCatalogBrowse` / `l2CategoryId` / `defineProps` | **通过** |

902 命名用例 **未删**：`902-seatmap-l1-dropdown`（title-row + L1 Select + `l1CategoryId` query）、`902-no-enterprise-filter` 仍在。布局断言按 909 更新：居中放到 `909-seatmap-title-center`（禁止再断言 `space-between`），902 只保留 L1 能力断言。

### 2. L3 count = 筛选全集；loadMore 不增大

| 层 | 实地 | 结果 |
|---|---|---|
| 服务端 | `CatalogBrowseService.listProducts` 在分页 `subList` **之前**对 `filtered` 调 `l3CountsOf`；键为 `l3CategoryId` 或 `__uncategorized__` | **通过** |
| 契约 | OpenAPI `ProductPage.l3Counts` 可选；描述与实现一致 | **通过** |
| 前端分组 | `groupProductsByL3(..., l3Counts)` → `sectionCount` 优先服务端 totals，缺省才回退已加载条数 | **通过** |
| 列表绑定 | `useCatalogBrowse` `sections` 传入 `l3Counts.value`；`CatalogBrowsePage` 仍渲染 `{{ sec.count }} 项` | **通过** |
| loadMore | 追加页若带非空 `l3Counts` 则覆盖为同一 map；空 map 在 append 时保留旧值。具名用例 `909-l3-count-full-total`：page1 count=4、loadMore 后 products=4 而 count 仍 4 | **通过** |
| 后端测 | `l3Counts_fullTotal_independentOfPageSize`：`q=计数` 共 5 条，pageSize=2 时 L3=3、未分类=2，翻页不变 | **通过** |

未分类键前后对齐：前端 `UNCATEGORIZED_SECTION_KEY` 与后端 `UNCATEGORIZED_L3_KEY` 均为 `__uncategorized__`。

### 3. 创建人/操作人写读路径 + 客户端不可覆盖

| 路径 | 实地 | 结果 |
|---|---|---|
| sanitize | `sanitizeWriteBody` 丢掉 `createBy`/`updateBy`/`createByName`/`updateByName`/`create_by`/`update_by`；`create`/`update` 均先 sanitize 再 `validateWrite` | **通过** |
| 新建 | `upsertProduct` 无 existing 时 `setCreateBy(actor)` + `setUpdateBy(actor)` | **通过** |
| 更新保留 create_by | existing 分支 `entity.setCreateBy(old.getCreateBy())`；随后只刷 `updateBy` | **通过** |
| update_time | `BaseEntity.@PreUpdate` 每次更新刷新 `updateTime` | **通过** |
| 读路径 | `productToMap` / editor `toMap` 调 `appendAuditFields`；显示名 `actorLabel` 优先 displayName，回退 userId | **通过** |
| UI | 预览 `dt/dd` 只读「创建人」「操作人」；`auditActorLabel(name, userId)`；无 `v-model` 绑定审计字段 | **通过** |
| 集成测 | `audit_createBy_updateBy_ignoresClientBody_andPreservesCreator`：伪造 body 无效；ADMIN 更新后 createBy 仍为提供方、updateBy 为管理员；GET 详情一致 | **通过** |

验收「列表或预览至少展示」由浏览预览满足；维护表未改不升 P1。

## Scope 检查（writeSet / denyModify）

相对 HEAD 的 909 相关脏文件落在 hotfix 建议 writeSet（含对应测试与 OpenAPI 最小增量）。`CatalogBrowsePage.vue` 相对 HEAD 的大段删减（nav-card / industryCategory 筛）属 **901 已落地**，909 增量是预览审计字段 + `auditActorLabel`。

| 项 | 结果 | 证据 |
|---|---|---|
| 未改 `CatalogProduct` / `CatalogEntityMapper` | **成立** | `git diff HEAD` 空。审计从实体列 `findProductAudit` 读取，不入库新列 |
| 未改 `ProductImportService` 写路径 | **成立** | 成功行仍 `productEditorService.create(body, actorUserId, …)`（约 L111）；相对 HEAD 仅注释。sanitize + upsert 与新建同路径 |
| 无新 Flyway / `**/sql/**` | **通过** | 无 `V8__*`；`git diff HEAD -- **/sql/**` 空（既有 V7 属 904，非本 hotfix） |
| 未改 v14 e2e | **通过** | `playwright.config.ts`、`p0-wsc-v1.4.spec.ts` diff 空；亦未改 v16 spec |
| 未改授权拦截器 | **通过（909）** | 本任务未改 `RbacMatrix` / `WriteAuthorizationInterceptor` 的授权判定。工作树相对 HEAD 的拦截器/矩阵脏来自本 Run **905/906**，无 l3Counts/审计/居中逻辑。**不**升 P0/P1，**不**代 securityReviewer |
| 无企业管理页；座序图无企业筛；浏览无 `industryCategory` 筛 | **通过** | SeatMap 无 enterprise；`buildQuery` 仍不传 `industryCategory`/`enterpriseName` |
| 未 bump `contracts/VERSION` | **可接受** | 仍 `2.3.3`；`Product`/`ProductPage` 只加可选字段，`required` 未扩 |

工作树 `state.yaml` 由 Orchestrator 推进，**本实例未写**。

## 契约对齐

| 字段 | OpenAPI | `frontend/src/api/catalog.ts` |
|---|---|---|
| `Product.updateBy` / `createByName` / `updateByName` | 可选 string, nullable | 同名可选 `string \| null` |
| `Product.createBy` | 既有 | 既有 |
| `ProductPage.l3Counts` | 可选 object, additionalProperties int64 | `l3Counts?: Record<string, number>` |
| `ProductWrite` | **不含**审计键（只读挂在 Product） | `ProductWrite` 无 createBy/updateBy |

纯加字段、未改 breaking required → 不 bump VERSION 与 hotfix 口径「能不加版本则不加」一致。

## 范围外已知债（不计入 909 P0/P1）

`CatalogBrowsePage.spec.ts` L16–22 仍期望 `canWriteProduct('ADMIN')===false` / `canImportProduct('ADMIN')===false`（TASK-WSC-603 V1.4）。实地 `useCanWrite.ts` 已是 ADMIN+PROVIDER（SNAP-WSC-008 / 906）。**独立判定：906/V1.6 范围外已知债（既有 FIND-WSC-906-001），909 未改该断言、未改坏 RBAC UI。** 全文件 Vitest 红 **不得**当作 909 回归；具名 `909-audit-createBy-updateBy` 为源码断言、与 ADMIN 写权限无关。

## 测试覆盖与门禁边界

| 项 | 本 REV |
|---|---|
| 三项验收自动化存在且非空 | **通过**（规格审查：源码/样式测 + composable + 后端集成） |
| DEV 自测 vitest/mvn exit 0 | **见过 DEV 落盘说明**；**不等于**独立 tester PASS |
| 本实例重跑命令 | **否**（以源码与测试断言独立核验，不替代 tester） |
| 标 VERIFIED | **禁止且未做** |

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-909-001 | P2 | OPEN | `CatalogBrowsePage.spec.ts` L16–22 全文件仍期望 ADMIN 不可写/导入；与 906 后 `useCanWrite` 不一致。909 **未**改该断言。tester 跑全文件会红，属 906 已知债 | 由后续非 909 任务把该用例改为 ADMIN true，或 tester 只跑 `909-*` 具名用例；**不要**因此打回 909 | REQ-RBAC-002 / TASK-WSC-906 |
| FIND-WSC-909-002 | P2 | OPEN | ① DEV 自测 49+1 前端 / 12 后端 **不是**独立 tester 门禁。② 列表 `productToMap` 对每条调用 `appendAuditFields`（实体解析 + 用户显示名），pageSize 默认 20，可后续批量预取 | 独立 tester 按 hotfix 具名用例再跑；N+1 不要求本轮返工 | REQ-CAT-012、审计展示 |

## 架构与可维护性备注（非阻塞）

- 审计不塞进 `CatalogProduct` record，避免牵动维护构造；从 `DataProductEntity` 列读取，符合「不新迁移」。
- 两参 `CatalogBrowseSeedStore` 保留给单测；三参 `@Autowired` 注入 `SysUserRepository`，`userRepo==null` 时 `actorLabel` 回退 userId。
- h2 `pointer-events: none` 避免挡住 L1；窄屏下标题与右侧 Select 可能视觉接近，不升 P1。
- 前端 `sectionCount` 在缺 `l3Counts` 时回退已加载条数，属兼容；本任务 API 在非空筛选下总会给出 map。

## 决策依据

- P0=0、P1=0 → **APPROVE**，可进入独立 **tester**（勿把 DEV 自测或 CatalogBrowsePage 全文件 ADMIN 红测当 909 FAIL）。
- 本 REV **不**宣称测试门禁通过，**不**标 VERIFIED。
- 无 SEC/MIG 触发；909 未改 RBAC/拦截器授权；**不**因缺安全/迁移审查而 REQUEST_CHANGES。
- P2 不要求 909 返工。
