# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-401-R1
taskId: TASK-WSC-401
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-401
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-006
reviewedAt: 2026-08-03T09:54:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/tasks/TASK-WSC-401.md
  - planning/tasks/DEV-TASK-WSC-401.md
  - design/decisions/DEC-WSC-001.md
  - frontend/src/features/portal/**
  - frontend/src/layouts/PortalLayout.vue
  - frontend/src/router/index.ts
  - frontend/src/router/routes.ts
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/overview/OverviewPage.vue
  - frontend/src/api/catalog.ts
  - backend/.../catalog/browse/**
  - backend/.../catalog/editor/ProductEditorService.java
  - backend/.../catalog/maintenance/CatalogMaintenanceService.java
mustDifferFrom: developer-wsc-401
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。

验收要点成立：

1. **静态门户**：`/` `/workspace` `/docs` 为 Vue 静态页 + `data/content.ts` mock；无 React 依赖；PortalLayout 不包 Workbench；「打开接入端」`RouterLink` → `/login`。
2. **路由 / 鉴权**：门户 `meta.public`；工作台挂 `WorkbenchLayout` + `requiresAuth`；根路径无 `/overview` redirect；未登录受保护路由 → `login`（可带 `redirect`）。
3. **产品编码（DEC-WSC-001）**：create 进入/切换类型调用 `generateProductCode`；映射表与 DEV 一致；字段 `readonly` +「自动生成」；edit 不改编码。
4. **列表排序**：`CatalogBrowseService` 分页前 `updatedAt` DESC（null→EPOCH）；种子 staggered；写路径（editor create/update、maintenance 关联）刷新；导入经 `ProductEditorService.create` 间接覆盖。
5. **高分屏**：总览去掉 `max-width: 1200px` → `width: 100%`；门户 `.portal-container` `max-width: none`。
6. **writeSet**：变更落在门户/路由/layout/editor/overview/api/catalog/browse/editor（及 maintenance 最小 `updatedAt` 触摸）+ DEV；未改 `contracts/**`、`frontend/src/components/**`、`product/**`、`planning/approved/**`、`state`/`events`。

审查复跑 Vitest **21 PASSED**；`listProducts_sortedByUpdatedAtDesc` **PASSED**。进入独立 tester 门禁；**不**代标 VERIFIED。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-401-R1-001 | P2 | OPEN（不阻塞） | `CatalogMaintenanceService` 字面 writeSet 未列 maintenance，但为 `CatalogProduct.updatedAt` 关联/待关联写路径最小刷新；DEV 已声明 | 若需严格字面 writeSet，可在 TASK 补记 maintenance 或后续回合补说明 | REQ-CAT-005 |
| FIND-WSC-401-R1-002 | P3 | OPEN（不阻塞） | 排序集成测断言首条 `MED-PG-0018` + `updatedAt` 非空，未逐对比较相邻项时间戳；种子 seq 使断言有效 | 可选补 `items[i].updatedAt >= items[i+1]` | REQ-CAT-005 |
| FIND-WSC-401-R1-003 | P3 | OPEN（不阻塞） | 门户/路由测为源码字符串 smoke，未挂载鉴权 E2E | 可选补 router + auth mock 导航测 | REQ-SHELL-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## 核对

| 项 | 结果 |
|---|---|
| `/` `/workspace` `/docs` 静态门户 + mock；无 React | PASS（Vue SFC + `content.ts`；`package.json` 无 react） |
| 「打开接入端」→ `/login` | PASS（`WorkspaceIntroPage.vue`；无 `/workspace/console`） |
| 门户 public vs Workbench 鉴权；根不 redirect overview | PASS（`router/index.ts` + `routes.ts`） |
| create 自动编码 DEC-WSC-001；edit 不变 | PASS（`productCode.ts` + `useProductEditor` + 页面只读提示） |
| 列表 `updatedAt` 倒序（分页前） | PASS（`.reversed()` + 种子 + 集成测） |
| 高分屏流体宽度 | PASS（overview / portal-container） |
| writeSet / denyModify | PASS（maintenance 为 P2 边缘，不阻塞） |
| Vitest 相关套件 | **21 PASSED** |
| 后端排序集成测 | **1 PASSED** |

### 审查复跑证据

```text
cd frontend
pnpm exec vitest run src/features/portal/portal.spec.ts src/router/router.portal.spec.ts \
  src/features/catalog/editor/utils/productCode.spec.ts \
  src/features/catalog/editor/composables/useProductEditor.spec.ts \
  src/features/overview/OverviewPage.spec.ts --reporter=default
# Test Files  5 passed (5) | Tests  21 passed

cd backend
mvn -pl app/data-chain-service -am test -Dtest=CatalogBrowseIntegrationTest#listProducts_sortedByUpdatedAtDesc
# Tests run: 1, Failures: 0 | BUILD SUCCESS
```

### 隔离声明

- 审查者 `code-reviewer-wsc-401` ≠ `developer-wsc-401`；未修改被审业务代码、DEV/TASK、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 VERIFIED；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

### 计数（open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 1（FIND-001，不阻塞） |
| P3 | 2（FIND-002/003，不阻塞） |
