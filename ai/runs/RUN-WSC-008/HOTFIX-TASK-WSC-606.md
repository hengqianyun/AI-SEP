# HOTFIX 候选 — TASK-WSC-606（V1.4 发布前 UX）

```yaml
taskId: TASK-WSC-606
runId: RUN-WSC-008
kind: HOTFIX
blocksRelease: true
status: CHANGES_REQUESTED
source: PO chat 2026-08-06
codeReview: planning/tasks/REV-TASK-WSC-606.md
blockingFinding: FIND-WSC-606-R1-001
```

## Round 2 扩写（CR REQUEST_CHANGES）

- **FIND-WSC-606-R1-001 (P1)**：适配 V1.4 E2E 场景 2（用户管理弹框创建 / 编辑角色按钮）；复跑或声明需独立 tester 重跑 `test:p0-v14`
- **writeSet 扩**：`tests/e2e/specs/p0-wsc-v1.4.spec.ts`；`tests/e2e/helpers/v14-selectors.ts`（若有）；必要时更新 `tests/e2e/reports/p0-wsc-v1.4/**` 仅在本轮开发自跑

## 范围（acceptance）

1. **数据目录滚动**：页面可正常滚动（修复 `CatalogBrowsePage` 主区/列表 `overflow` 导致无法滑动；保留触底分页行为合理）
2. **用户管理**
   - 创建用户改为**弹框**创建（非页内表单卡）
   - 操作栏增加**编辑角色**按钮（勿仅用行内 `<select>` 冒充编辑）
   - 页面使用与其它页一致的 **白色卡片 / `wsc-surface`** 风格（含列表区可见白底卡片）
   - **去除**副标题「创建账号并配置角色（持久化 sys_user）」
3. **目录维护**：去除「← 返回目录」按钮
4. **数据目录「行业分类」下拉**：选项改为 GB/T 门类枚举 `INDUSTRY_CATEGORY_OPTIONS`（`frontend/src/api/catalog.ts`）；筛选应对齐产品字段 `industryCategory`（必要时后端增加 `industryCategory` query；**勿**再把 L2/L3 树当该下拉选项）

## 建议 writeSet

- `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` (+ scoped CSS)
- `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` (+ spec)
- `frontend/src/features/users/UsersAdminPage.vue` (+ spec)
- `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue`
- 若需服务端筛选：`backend/.../CatalogBrowseController.java`、`CatalogBrowseService.java` + 对应测试
- 若契约声明 query：`contracts/openapi/openapi.yaml`（仅增 `industryCategory` query；评估是否需 VERSION bump——默认 **尽量不 bump**，若门禁要求则注明）
- **R2 扩**：`tests/e2e/specs/p0-wsc-v1.4.spec.ts`；`tests/e2e/helpers/v14-selectors.ts`；自跑时可写 `tests/e2e/reports/p0-wsc-v1.4/**`

## denyModify（建议）

- 座序图/L2 实现体（除非滚动布局连带）；auth/login；sql
- 除上列外的其它 e2e 规格（v1.1/v1.3 等）勿顺手大改
- `state.yaml` / `events.jsonl`（仅 Orchestrator）

## 门禁

developer → codeReviewer → tester → VERIFIED 后恢复 `human/APPROVE_RELEASE`
