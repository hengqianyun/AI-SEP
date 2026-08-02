# DEV-TASK-WSC-107-FIX1

```yaml
taskId: TASK-WSC-107
fixId: FIX1
bugId: BUG-WSC-E2E-001
actorInstance: developer-wsc-e2e-fix1
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T23:21:00+08:00
basedOn:
  - planning/tasks/BUG-WSC-E2E-001.md
  - planning/tasks/TESTRUN-WSC-E2E-V11.md
  - planning/tasks/REV-TASK-WSC-107.md
  - planning/tasks/TASK-WSC-107-FIX1.md
  - frontend/src/features/catalog/import/INTEGRATION.md
  - evidenceId: TESTRUN-WSC-E2E-V11
```

## 摘要

关闭 BUG-WSC-E2E-001（FIX1）：在 `CatalogBrowsePage` 按 INTEGRATION.md 挂载 `ImportDialog`（`?import=1` 打开、关闭清 query、页头「批量导入产品」入口）；补齐 FIND-002 错误报告真实下载（对齐 OpenAPI `getImportErrorReport` → CSV）。未改 contracts / api / backend / e2e。

## 关联缺陷

| 字段 | 值 |
|---|---|
| bugId | BUG-WSC-E2E-001 |
| evidence | TESTRUN-WSC-E2E-V11（场景 4 `#import-modal` 不可见） |
| relatedFindings | FIND-WSC-107-R1-001、FIND-WSC-107-R1-002 |
| rootCause | browse denyModify 导致未挂载；报告链 `@click.prevent` 空操作 |
| bug status | `FIXED_PENDING_VERIFY`（关闭权在独立 tester 复跑 E2E） |

## 变更文件列表

| 路径 | 说明 | REQ / 关联 |
|---|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | 挂载 ImportDialog；页头批量导入按钮 | REQ-CAT-008 / FIND-001 |
| `frontend/src/features/catalog/browse/composables/useCatalogImportEntry.ts` | `?import=1` watch + 关闭清 query | REQ-CAT-008 / ISSUE-UX-R1-002 |
| `frontend/src/features/catalog/browse/composables/useCatalogImportEntry.spec.ts` | 挂载/权限/清 query 单测 | — |
| `frontend/src/features/catalog/import/ImportDialog.vue` | 报告链绑定 download；modelValue immediate | FIND-002 |
| `frontend/src/features/catalog/import/composables/useProductImport.ts` | `downloadErrorReport` / CSV 构建 | FIND-002 / getImportErrorReport |
| `frontend/src/features/catalog/import/composables/useProductImport.spec.ts` | 报告下载动作单测 | — |
| `frontend/src/features/catalog/import/INTEGRATION.md` | 更新为已挂载约定 | — |
| `planning/tasks/BUG-WSC-E2E-001.md` | status → `FIXED_PENDING_VERIFY` | 缺陷跟踪 |
| `planning/tasks/DEV-TASK-WSC-107-FIX1.md` | 本说明 | — |

## 未修改（边界）

- 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**`
- 未改 detail/editor/admin/maintenance、ai/rules、product
- 未标任务 `VERIFIED` / 未将 BUG 标 `CLOSED`
- 未代跑波次 E2E（关闭权在 `tester-wsc-e2e-v11` 等独立 tester）

## 自测命令与结果

| 命令 | 结果 | 备注 |
|---|---|---|
| `pnpm exec vitest run src/features/catalog/browse/composables/useCatalogImportEntry.spec.ts src/features/catalog/import/composables/useProductImport.spec.ts`（cwd: frontend） | **PASSED** — 2 files / 12 tests | 2026-07-31T23:20:19+08:00 |
| E2E 场景 4 复跑 | 交独立 tester | 对照 `TESTRUN-WSC-E2E-V11`；本实例不代关闭 BUG |

### 手测 / E2E 复跑提示

1. ADMIN/PROVIDER 登录 → 壳层「批量导入」→ `/catalog?import=1` → `#import-modal` 可见
2. 关闭弹窗 → URL 无 `import` query，仍在目录浏览
3. 上传 partial-success fixture → 成功/失败计数 → 点击「下载错误报告」触发 CSV 下载（或 prefetch 行数 meta 可见）

## 提交审查

- status: `READY_FOR_REVIEW`
- codeReviewer 须与 `developer-wsc-e2e-fix1` 不同实例
- tester 复跑 `TESTRUN-WSC-E2E-V11` 场景 4（及完整波次）后方可关闭 `BUG-WSC-E2E-001`
