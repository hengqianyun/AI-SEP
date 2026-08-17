# HOTFIX 候选 — TASK-WSC-909（发布前 UX / 计数 / 审计留档）

```yaml
taskId: TASK-WSC-909
runId: RUN-WSC-011
kind: HOTFIX
blocksRelease: true
status: IN_DEVELOPMENT
source: PO chat 2026-08-17
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
```

## 业务意图（PO）

发布门禁 `RELEASE_REVIEW` 期间提出三项 hotfix：

1. 座序图标题「数据流通链」需要**居中**
2. 产品列表分类 **count** 不对：要展示**全部数量**，而不是当前查询结果里的数量
3. 数据操作增加 **创建人、操作人** 字段留档

## 预落地对照（Orchestrator 只读定位，供确认）

| # | 现象 | 现况 | 建议验收 |
|---|---|---|---|
| 1 | 座序图标题未居中 | `CirculationSeatMap.vue` `.title-row` 为 `justify-content: space-between`，「数据流通链」在左、L1 下拉在右；下方「N条数据」已居中 | 标题视觉居中（相对座序图卡片）；L1 下拉可仍在右侧，不得挡标题 |
| 2 | 分类 count 随当前查询/已加载子集变化 | `groupProductsByL3` 用 `list.length`（当前 `products` 分页结果）作为 `sec.count`；`CatalogBrowsePage` 展示 `{{ sec.count }} 项` | 每个 L3（含「未分类数据」）的 count = **该分类下全部产品数**（不受当前页已加载条数截断）。筛选变更后 count 应对齐**筛选后全集**，而不是本页条数 |
| 3 | 数据操作缺创建人/操作人留档 | 产品实体已有 `create_by`/`update_by`；OpenAPI `Product` 仅有 `createBy`，列表/预览 UI **未展示**；`updateBy`（操作人）未进产品契约 | 产品创建/编辑/导入等写路径写入并刷新创建人、操作人；列表或预览（及维护相关展示面）可见留档；只读展示，禁止客户端伪造覆盖服务端审计 |

> 第 3 项按「产品数据操作（新增/编辑/导入）审计留档 + 界面可见」理解。若实际指目录维护表或其它页，确认时请改口。

## acceptance

1. `/catalog` 座序图：`h2`「数据流通链」相对座序图区域**水平居中**；L1 业务视图下拉仍可用；无企业筛（V1.6 锁定不变）
2. 产品列表各 L3 分区右侧 count = 该 L3 **全集条数**（分页只影响列出的行，不影响 count）。触底加载更多后 count **不得**随已加载条数变大
3. 写路径：创建写入 `create_by`+`update_by`；后续改/导刷新 `update_by`（及 `update_time`），**不**改写 `create_by`
4. 读路径：列表或预览至少展示创建人、操作人（显示名优先，可回退 userId）；契约若缺 `updateBy` 则本任务可最小增量（评估是否 bump `wsc-contracts`；能不加版本则不加）
5. 自动化：座序图标题居中（源码/样式或组件测）；count 不随分页截断；创建/更新后审计字段可断言

## 建议 writeSet

- `frontend/src/features/catalog/browse/components/CirculationSeatMap.vue`（+ 既有 spec）
- `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts`（+ spec）
- `frontend/src/features/catalog/browse/CatalogBrowsePage.vue`
- 若 count 需服务端聚合：`CatalogBrowseController` / `CatalogBrowseService` / `CatalogBrowseSeedStore` + 对应测试
- 若补 `updateBy`：`contracts/openapi/openapi.yaml`（及必要时 `frontend/src/api/catalog.ts`）；产品 model/mapper；编辑/导入写路径
- 预览/列表展示创建人、操作人

## denyModify

- `tests/e2e/playwright.config.ts`、`p0-wsc-v1.4.spec.ts`（除非选择器被本 hotfix 打断，默认不改 908 规格）
- `**/sql/**`（审计列已存在则禁止再出迁移）
- `ai/runs/**/state.yaml`、`events.jsonl`（仅 Orchestrator）
- 无企业管理页；座序图**不得**加企业筛；浏览**不得**加回 `industryCategory` 筛

## 门禁

developer → codeReviewer → tester → VERIFIED 后恢复 `human/APPROVE_RELEASE`

若契约变更触及 OpenAPI Product 字段：评估 `auth-model-change` **不**自动成立（仅审计展示）；若改 RBAC/授权则须 `securityReviewer`。默认本 hotfix **不**改授权模型。
