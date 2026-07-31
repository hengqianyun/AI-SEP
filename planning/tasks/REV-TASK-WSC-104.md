# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-104-R1
taskId: TASK-WSC-104
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-104
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T16:30:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-104.md
  - planning/tasks/DEV-TASK-WSC-104.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet`：前端 `frontend/src/features/catalog/browse/**` 与后端 `com.shdata.datachain.catalog.browse/**`（含对称集成测）；未改本任务 `denyModify` 面（`contracts/**`、`frontend/src/api/**`、`**/sql/**`、`catalog/admin/**` 及 detail/editor/maintenance/import）。三级标签/L3 分节/预览路径、行业筛 L2|L3、滚动 load-more 与分页字段 `page`/`pageSize`/`total` 对齐 SNAP REQ-CAT-001/002/003 与 PLAN 验收。审查复跑后端 11 测与前端 browse composable 8 测均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-104-R1-001 | P2 | 顶栏 `l2CategoryId` 与筛选项 `industryFilterId`（L2）同时存在时，`buildQuery` 用行业筛覆盖顶栏 L2，UI 仍高亮原行业标签，可能造成「标签≠结果」观感。`selectL2` 会清冲突筛，反向（改筛）未对称清理 | 改筛时同步顶栏 L2，或 AND 双条件并依赖空态提示 | REQ-CAT-001, REQ-CAT-002 |
| FIND-WSC-104-R1-002 | P2 | `CatalogProduct` 增 `l3CategoryId` 后，`ProductEditorService.withChainMeta` / `toProduct` 仍走无 l3 兼容构造，编辑/上链写回会把已有 L3 置 null（DEV 已声明交 105）。browse 种子路径不受影响 | TASK-WSC-105 写路径写入并保留 `l3CategoryId` | REQ-CAT-001, REQ-CAT-005 |
| FIND-WSC-104-R1-003 | P2 | 前端单测覆盖分节/行业解析/空态文案；`loadMore` append 与筛选保留依赖后端集成测 + 页面状态机，无 composable 级 mock 翻页测 | 可选：mock `listProducts` 断言 append 后 `page`/`filters` 不变 | REQ-CAT-001, REQ-CAT-002 |
| FIND-WSC-104-R1-004 | P3 | 分节 `count` 为「当前已加载列表」内计数，滚动分页下非该 L3 全库总数；与「已加载 n / 共 total」并存可接受 | 若产品要「分节全量」再改后端聚合或文案标明「已加载」 | REQ-CAT-001 |
| FIND-WSC-104-R1-005 | P3 | `listScrollEl` 仅绑 ref，滚动保留依赖 DOM 追加不重置 `scrollTop`（满足验收）；变量本身无额外用途 | 去掉未读 ref 或显式注释「位置由浏览器保留」 | REQ-CAT-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet` | PASS（`catalog/browse/**` 前端 + `data-chain-service` `com.shdata.datachain.catalog.browse/**` + 测试） |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**` | PASS（相对 DEV 变更清单；工作区另有 101/103 契约/api/admin/sql 改动不归本轮） |
| 未改 `catalog/admin/**` 及 detail/editor/maintenance/import | PASS（本任务未触碰；∥103 admin 另轨） |
| 未与 TASK-WSC-103 混写同一 browse/admin 交付面 | PASS |

### 2. REQ-CAT-001（三级浏览）

| 项 | 证据 | 结果 |
|---|---|---|
| 顶部空间(L1)/行业(L2)标签切换产品集 | `CatalogBrowsePage` tag-bar + `selectL1`/`selectL2` → `loadProducts` | PASS |
| 子类(L3)分节（标题+数量） | `groupProductsByL3` + section UI；单测覆盖 | PASS |
| 列表字段：序号/编码/名称/上链次数/类型 | 表头与行渲染 | PASS |
| 预览含三级 `categoryPath` | 预览区 `catalog-preview-path`；集成测 `金融服务 / 征信评估 / 征信评分` | PASS（复跑） |
| 滚动加载 loading，保留已加载列表与筛选 | `loadingMore` + append；`buildQuery` 复用 filters；scroll 容器追加不重置 | PASS（实现） |

### 3. REQ-CAT-002（筛选对齐三级）

| 项 | 证据 | 结果 |
|---|---|---|
| 来源/类型/公共数据/交付/关键词 | filter-bar + `buildQuery` | PASS |
| 行业筛可选 L2 或 L3 | `industryFilterOptions` + `resolveIndustryQuery`；后端 `l3CategoryId` / L2 含子 L3 | PASS |
| 无结果空态 | `listState==='empty'` + 稳定文案；集成测 empty | PASS |
| 分页字段 `page`/`pageSize`/`total` | Service/Controller + OpenAPI ProductPage；前端读取 | PASS |
| 筛选 → page2 仍满足筛选 | `listProducts_pagination_pagePageSizeTotal_andFilterPreservedOnPage2` | PASS（复跑） |

### 4. REQ-CAT-003（预览三分入口）

| 项 | 证据 | 结果 |
|---|---|---|
| 详情 / 上链 / 编辑 | `goDetail` / `goChain` / `goEdit` | PASS |
| 普通用户无编辑 | `v-if="productWriteVisible"`（`useCanWrite`） | PASS |

### 5. 测试覆盖（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| 后端三级/筛选/分页 | PASS — `CatalogBrowseIntegrationTest` 11 tests |
| 前端分节与行业解析 | PASS — `useCatalogBrowse.spec.ts` 8 tests |
| 审查复跑 | 后端 11 PASSED / BUILD SUCCESS；vitest 8 PASSED |

## 残余风险（交 tester）

- E2E 抽样：顶栏标签切换 → 分节变化；筛选空态；滚至底部 load-more 且筛选仍成立；USER 角色预览无「编辑」。
- 工作区并存 TASK-WSC-101/103 改动；tester 基线模块为 `data-chain-service`（`repos.yaml`），勿把 admin/sql 变更算进 104。
- 105 落地前勿对「编辑后产品仍带 L3」做硬验收（见 FIND-002）。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 2 |
