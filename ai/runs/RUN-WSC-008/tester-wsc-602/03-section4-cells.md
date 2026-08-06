# §4 单元格表证据定位（TASK-WSC-602）

对齐 `planning/approved/PLAN-WSC-5.2.md` TASK-WSC-602 `testScope` 单元格表。

| 能力 | ADMIN | PROVIDER | USER | 自动化证据 |
|---|---|---|---|---|
| 用户管理 API | **200** | **403** | **403** | `SysUserAuthSecurityIntegrationTest#adminUsers_crudAndSoftDelete_noPasswordLeak`（ADMIN list/create/update/soft-delete → 200）；`#adminUsers_nonAdmin_forbidden`（provider/user → 403 + `ERR_FORBIDDEN`） |
| 用户管理 UI | **可达** | **结构不可达** | **结构不可达** | `UsersAdminPage.spec.ts`：内联 `role.value === 'ADMIN'` + `users-forbidden`；`WorkbenchLayout` `userManageVisible` + `nav-users`；非 ADMIN 门禁 |
| `POST …/session/role` | **410+码** | **410+码** | **410+码** | `SysUserAuthSecurityIntegrationTest#sessionRoleSwitch_disabledForAllRoles`（`@ValueSource` admin/provider/user → 410 + `ERR_ROLE_SWITCH_DISABLED`）；`authStore.spec.ts` `switchRole always fails with ERR_ROLE_SWITCH_DISABLED` |
| 角色区 UI | 只读无切换 | 只读无切换 | 只读无切换 | `RoleSwitcher.spec.ts`：无 listbox/chevron/option/select；Layout **永不挂载** RoleSwitcher；唯一 `session-role-label` |

实跑绑定：`01-backend-security.txt`（11 tests SysUser + 6 SessionAuth）；`02-vitest.txt`（3 files / 10 tests）。
