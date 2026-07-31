# DEV-TASK-WSC-102

```yaml
taskId: TASK-WSC-102
actorInstance: developer-wsc-102
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T15:27:00+08:00
reqs:
  - REQ-SHELL-001
  - REQ-RBAC-001
```

## 摘要

在 `backend/app/data-chain-service`（`com.shdata.datachain`）上实现/保持 §3.3 会话鉴权，并按 PLAN-WSC-2.2 §4 / `contracts/rbac/matrix.yaml@2.0.0` 扩展写过滤器：目录维护、批量导入、导入报告 GET；前端壳层增加管理员「目录维护」侧栏与三角色写入口（含批量导入）可见性。未实现 catalog 业务页（denyModify）；维护/导入业务逻辑仍由 106/107 落地——本任务以 `StubWriteController` 供过滤器可测。

## 变更文件列表

### 后端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/rbac/RbacMatrix.java` | REQ-RBAC-001 | 增 `canMaintainCatalog` / `canImportProduct` / `canGetImportReport` |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/rbac/WriteAuthorizationInterceptor.java` | REQ-RBAC-001 | 分类/产品写 + 维护（含 GET）+ 导入 + 报告 GET；维护拒绝码 `ERR_MAINTENANCE_FORBIDDEN` |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/rbac/package-info.java` | — | 包说明对齐 102 |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/security/StubWriteController.java` | REQ-RBAC-001 | 维护/导入/报告占位端点（供 §4 过滤器与会话吊销测试） |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/rbac/RbacMatrixTest.java` | REQ-RBAC-001 | 矩阵单测 + classify |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/security/SessionAuthIntegrationTest.java` | REQ-RBAC-001 | §4 三角色 + 登出后导入拒绝 + 切角色旧会话拒绝 |

> 既有 `AuthController` / `SecurityWebConfig` / `DemoAccountService` 等会话能力保持（登录失效旧会话、切角色吊销、Cookie `WSC_SESSION`）；本轮未改其语义。

### 前端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/auth/composables/useCanWrite.ts` | REQ-RBAC-001 | `canMaintainCatalog` / `canImportProduct`；composables 暴露四类可见性 |
| `frontend/src/features/auth/composables/useCanWrite.spec.ts` | REQ-RBAC-001 | §4 UI 可见性单测 |
| `frontend/src/layouts/WorkbenchLayout.vue` | REQ-SHELL-001 | 管理员侧栏「目录维护」；占位菜单仍走 `/unavailable/*` |
| `frontend/src/features/shell/components/WriteEntryDemo.vue` | REQ-RBAC-001 / REQ-SHELL-001 | 分类维护 / 目录维护 / 新增 / 批量导入入口 |

> 既有 `authStore`（login/logout/switchRole）、`LoginView`、`RoleSwitcher`、路由守卫未改；角色切换后 Pinia session 立即更新，写入口随 `useCanWrite` 重算。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-SHELL-001** | 侧栏总览/数据目录；管理员可见「目录维护」；数据登记/交易订单/连接器 → 未开放；企业信息 + 角色切换；写入口随角色立即更新 |
| **REQ-RBAC-001** | 服务端会话角色；§4：ADMIN 分类+维护+产品写+导入；PROVIDER 仅产品写+导入（维护 `ERR_MAINTENANCE_FORBIDDEN`）；USER 全写 403；前端可见性非唯一防护；登出/切角色后旧会话写 API 拒绝 |

## 已知边界 / 后续任务

- `/catalog/maintenance` 业务页路由属 TASK-WSC-106；本任务仅壳层入口（链到该 path）。
- 目录页「批量导入」弹窗属 TASK-WSC-107；壳层按钮暂链到 `/catalog?import=1`。
- `StubWriteController` 占位维护/导入 API；106/107 落地真实 Controller 后须移除或改由 Orchestrator 协调（security 包不在后继写集）。

## 自测命令与结果

### 后端

```text
mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.security.SessionAuthIntegrationTest,com.shdata.datachain.rbac.RbacMatrixTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `com.shdata.datachain.rbac.RbacMatrixTest` | 6 | **PASSED** |
| `com.shdata.datachain.security.SessionAuthIntegrationTest` | 6 | **PASSED** |
| **合计** | **12** | **BUILD SUCCESS** |

覆盖：§4 三角色（含维护 GET/`ERR_MAINTENANCE_FORBIDDEN`、导入、报告 GET）；登出后导入 401；角色变更后旧会话导入拒绝 + 新 USER 会话导入 403。

集成测试使用 H2 内存库 + 排除 Redis（避免本机无 MySQL/Redis 时因 `@EnableJpaAuditing` 上下文失败）。

### 前端

```text
cd frontend
pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts
pnpm typecheck
pnpm lint
pnpm build
```

| 命令 | 结果 |
|---|---|
| vitest `useCanWrite.spec.ts` | **PASSED**（3 tests） |
| `pnpm typecheck` | **PASSED** |
| `pnpm lint` | **PASSED**（placeholder exit 0） |
| `pnpm build` | **PASSED** |

## SCOPE / denyModify

- 未修改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`、catalog/overview/chain 业务包
- 未修改 `ai/rules/**`、`product/**`、`planning/approved/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer；未自行标记 VERIFIED
