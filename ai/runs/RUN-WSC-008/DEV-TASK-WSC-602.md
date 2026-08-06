# DEV-TASK-WSC-602 — 交付说明

- **actorInstance**: `developer-wsc-602-r2`（round 2 修复；首轮 `developer-wsc-602`）
- **taskId**: `TASK-WSC-602`
- **requirements**: `REQ-USER-001`, `REQ-SHELL-001`, `REQ-RBAC-001`
- **status**: CR Round 1 P0 已修复，提交复审（**未**标 VERIFIED；**未**写 `state.yaml` / `events.jsonl`）
- **basedOn**: `planning/tasks/REV-TASK-WSC-602.md`（FIND-WSC-602-R1-001 / 002）

## Round 2 — 对 FIND 回应

| finding | severity | 回应 |
|---|---|---|
| FIND-WSC-602-R1-001 | P0 | **已修**：从 602 交付面移除 `myProductsVisible` / `nav-my-products` / `isActive('/my-products')`；`routes.ts` 删除 `/my-products` 与 catalog `props: { mode: 'catalog' }`。壳层仅保留 `/admin/users` + footer `session-role-label`。my-products 语义留给 603 |
| FIND-WSC-602-R1-002 | P0 | **已修**：`WorkbenchLayout` / `UsersAdminPage` 用户管理门禁改为内联 `role.value === 'ADMIN'`；不再 import `canManageUsers` / `canSeeMyProducts`。**未改** `useCanWrite.ts` / `useCanWrite.spec.ts`（denyModify）。`UsersAdminPage.spec.ts` 改为断言内联 ADMIN 门禁与壳层白名单（无 my-products） |
| FIND-WSC-602-R1-003..005 | P2/P3 | 未改（非阻塞；提交时按 writeSet 隔离暂存） |

## 对账结论（§0.2 / 602 行）

| 预落地迹象 | 对账结果 |
|---|---|
| `V6__create_sys_user.sql` | **保留并补注释**：可重复 `IF NOT EXISTS`；username 唯一 / role / password_hash / 软删 / 审计；种子由应用 BCrypt 写入 |
| `SysUserEntity` / `SysUserRepository` / `UserAccountService` | **保留并强化**：冲突→`ERR_USER_USERNAME_CONFLICT`；不存在→`ERR_USER_NOT_FOUND`；返回 `AdminUser` 视图 |
| `AuthController` 真登录 + 切角色 410 | **符合**：会话角色=账号角色；`POST …/session/role`→410+`ERR_ROLE_SWITCH_DISABLED` |
| `AdminUserController` | **修正**：命名 DTO；错误码硬对齐契约；响应显式序列化禁 password/hash |
| `UsersAdminPage` / `authStore` | **补齐**：令牌层次 + loading/empty/error；非 ADMIN **内联**结构门禁；login 无 client role |
| `RoleSwitcher` 可切换下拉 | **偏离已修**：改为只读标签；WorkbenchLayout **永不挂载**，footer `session-role-label` 为唯一角色展示面 |
| `DemoAccountService`（内存+可切角色） | **删除**（避免与真登录权威源冲突） |
| `LoginRequest.role` | **删除**（对齐 OpenAPI；`@JsonIgnoreProperties` 忽略旧客户端误传） |
| 壳层 my-products（R1 越界） | **R2 已从 602 面移除**；符合 §3.2 SA-004 白名单 |

## filesChanged

### Backend（round 1 保留；本轮未改）
- `…/sql/migration/V6__create_sys_user.sql`
- `…/entity/SysUserEntity.java`（对账保留）
- `…/repository/SysUserRepository.java`（+`existsByUsername`）
- `…/controller/security/AuthController.java`（对账保留）
- `…/controller/security/AdminUserController.java`
- `…/service/security/UserAccountService.java`
- `…/service/security/SessionAuthSupport.java`（清理重复 import）
- `…/service/security/DemoAccountService.java`（删除）
- `…/common/security/PasswordHasher.java`（对账保留 BCrypt）
- `…/common/security/AuthAuditLogger.java`（明文口令禁录声明）
- `…/model/LoginRequest.java`（去 role）
- `…/model/AdminUser.java` / `AdminUserCreate.java` / `AdminUserUpdate.java` / `AdminUserPage.java`（新增）
- `…/model/SessionPrincipal.java` / `Role.java` / `SwitchRoleRequest.java`（对账保留）
- `…/config/SecurityWebConfig.java`（对账保留）
- `…/test/…/security/SysUserAuthSecurityIntegrationTest.java`（新增 P0）

### Frontend
- `features/auth/store/authStore.ts` + `authStore.spec.ts`
- `features/auth/api/switchRole.ts`（再导出 `@/api/auth`）
- `features/users/UsersAdminPage.vue` + `UsersAdminPage.spec.ts`（**R2**：内联 ADMIN；去 canManageUsers 依赖）
- `features/shell/components/RoleSwitcher.vue` + `RoleSwitcher.spec.ts`
- `layouts/WorkbenchLayout.vue`（**R2**：去 my-products；用户管理内联 ADMIN；保留 `canMaintainCatalog` 既有目录维护）
- `router/routes.ts`（**R2**：仅 `/admin/users`；无 `/my-products`、无 catalog mode props）

## acceptance 勾选

- [x] Flyway `sys_user` 可重复；BCrypt 单向哈希；种子可文档化（`UserAccountService.DEFAULT_DEMO_PASSWORD`），无生产密钥明文落盘
- [x] 真登录：正确建会话；错误失败；角色=账号角色
- [x] `POST /auth/session/role` → 410 + `ERR_ROLE_SWITCH_DISABLED`；三角色均禁用
- [x] 壳层 footer **唯一**只读角色；无 listbox；RoleSwitcher 只读且永不挂载
- [x] ADMIN `/admin/users` CRUD + PUT `deleted`；PROVIDER/USER UI 门禁 + API 403
- [x] 响应永不含 password/passwordHash；审计不记明文口令
- [x] UsersAdminPage 令牌层次 + loading/empty/error
- [x] WorkbenchLayout/routes 仅按 §3.2 白名单（**无** my-products 菜单/路由）
- [x] 预落地对账；testScope 单元格有命名自动化
- [x] R2：不依赖 / 不改 `useCanWrite*`（denyModify）

## testScope 证据定位

| 能力 | ADMIN | PROVIDER | USER | 证据 |
|---|---|---|---|---|
| 用户管理 API | 200 | **403** | **403** | `SysUserAuthSecurityIntegrationTest#adminUsers_crud…` / `#adminUsers_nonAdmin_forbidden` |
| 用户管理 UI | 可达 | **结构不可达** | **结构不可达** | `UsersAdminPage.spec.ts`（内联 `role === 'ADMIN'`）；壳层 `userManageVisible` |
| `POST …/session/role` | **410+码** | **410+码** | **410+码** | `SysUserAuthSecurityIntegrationTest#sessionRoleSwitch_disabledForAllRoles` |
| 角色区 UI | 只读无切换 | 只读无切换 | 只读无切换 | `RoleSwitcher.spec.ts` + WorkbenchLayout `session-role-label` |

其它：登录成功/失败、软删不可登录、响应无 password/hash、冲突 409、不存在 404 — 同 `SysUserAuthSecurityIntegrationTest`；`authStore.spec.ts` 覆盖 login / switchRole 禁用。

## 自测命令与结果

```text
# frontend R2（cwd=frontend）
pnpm exec vitest run src/features/auth/store/authStore.spec.ts \
  src/features/shell/components/RoleSwitcher.spec.ts \
  src/features/users/UsersAdminPage.spec.ts
→ 3 files / 10 tests passed（2026-08-05 R2）

# backend：本轮未改后端，跳过 mvn security 复跑
```

## scope 声明

- 仅修改 `writeSet` 内路径；**未改** `useCanWrite.ts` / `useCanWrite.spec.ts`、`contracts/**`、`frontend/src/api/**`、`RbacMatrix`、`WriteAuthorizationInterceptor`、catalog/overview 等 denyModify。
- 未写 `ai/runs/**/state.yaml`、`events.jsonl`；未标 VERIFIED。
