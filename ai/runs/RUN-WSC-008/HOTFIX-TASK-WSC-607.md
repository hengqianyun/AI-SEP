# HOTFIX 候选 — TASK-WSC-607（PROVIDER 目录维护 · 仅本人产品）

```yaml
taskId: TASK-WSC-607
runId: RUN-WSC-008
kind: HOTFIX
blocksRelease: true
status: AWAITING_PO_CONFIRM
source: PO chat 2026-08-06
problem: |
  提供方导入产品后未挂目录树；目录维护现仅 ADMIN。
  希望 PROVIDER 可进目录维护，仅见/仅改 create_by=本人的条目。
```

## 业务意图（PO）

- 导入创建的产品常处于「未维护到目录」状态
- 将**目录维护**部分权限开放给**数据提供方**
- PROVIDER 进入目录维护页：只查询**自己的**产品，并可维护这些产品的目录关联
- **行业分类树维护 / 用户管理**等 ADMIN 专属能力保持不变（默认）

## acceptance（建议）

1. 侧栏「目录维护」对 **ADMIN + PROVIDER** 可见；USER 仍不可见
2. PROVIDER 调用 maintenance list/update/batch：仅 `create_by=当前用户`；越权改他人 → 403/404（与产品写隔离一致）
3. ADMIN 行为不变：可维护全量
4. 契约 `contracts/rbac/matrix.yaml` + OpenAPI maintenance 叙述同步；`RbacMatrix` / `useCanWrite` / 拦截器对齐
5. 自动化：RbacMatrix / CatalogMaintenance 集成测正负例；Vitest 菜单可见性；可选 E2E 抽样

## 建议 writeSet

- `contracts/rbac/matrix.yaml`（+ 必要时 openapi maintenance 描述；评估 VERSION）
- `frontend/src/features/auth/composables/useCanWrite.ts`(+spec)
- `frontend/src/layouts/WorkbenchLayout.vue`（若仅依赖 useCanWrite 则可不动）
- `frontend/src/features/catalog/maintenance/**`
- `backend/.../RbacMatrix.java` + WriteAuthorizationInterceptor（若需）
- `backend/.../CatalogMaintenanceService.java` + Controller（注入会话 userId / mine 过滤）
- 对应测试；必要时 `tests/e2e` 小改

## denyModify（建议）

- 分类树 ADMIN API；用户管理；座序图；登录模型大改
- `state.yaml` / `events.jsonl`（仅 Orchestrator）

## 门禁

developer → codeReviewer → tester → VERIFIED → 恢复 `human/APPROVE_RELEASE`

> 注：相对 SNAP-WSC-005 / matrix「目录维护仅 ADMIN」为**范围变更**；确认即视为 PO 批准本 hotfix 相对快照的增量修订意图（正式 SNAP 修订可选并行）。
