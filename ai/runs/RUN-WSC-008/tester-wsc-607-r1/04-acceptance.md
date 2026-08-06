# TASK-WSC-607 acceptance 对照（tester-wsc-607-r1）

| # | acceptance | 结果 | 证据 |
|---|---|---|---|
| 1 | 侧栏「目录维护」对 ADMIN + PROVIDER 可见；USER 仍不可见 | **PASS** | Vitest `useCanWrite.spec.ts` 三角色（`02-vitest.txt`，3/0）；菜单可见性由 `canMaintainCatalog` 驱动 |
| 2 | PROVIDER list/update/batch 仅 `create_by=本人`；越权 403/404 | **PASS** | `CatalogMaintenanceIntegrationTest` 6/0（`01-backend.txt`）含 mine/异主/空归属正负例 |
| 3 | ADMIN 行为不变：可维护全量 | **PASS** | 同集成测 ADMIN 路径；`RbacMatrixTest` 含 ADMIN |
| 4 | `matrix.yaml` + OpenAPI + `RbacMatrix` / `useCanWrite` / 拦截器对齐 | **PASS**（执行层） | `RbacMatrixTest` 7/0 + Vitest 3/0；契约对齐以 REV 源码审查为前提，本轮未改实现 |
| 5 | 自动化：RbacMatrix / Maintenance 集成；Vitest；可选 E2E | **PASS**（必需层） | Maven 13/0；Vitest 3/0。可选 E2E：**SKIPPED**（5173 不可用，见 `03-e2e-skipped.md`，不记 PASS） |

## 备注

- 审查前提：`REV-TASK-WSC-607` Round 1 **APPROVE**，P0=0，P1=0。
- 未修改业务实现；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- E2E 环境阻塞不伪造 PASS；不阻塞本 HOTFIX 必需自动化门禁。
