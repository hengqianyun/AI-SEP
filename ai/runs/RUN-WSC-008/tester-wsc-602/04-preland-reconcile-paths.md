# 预落地对账自动化路径（TASK-WSC-602 行 P0）

对齐 PLAN §0.2 602 行 + testScope「预落地对账自动化」。

| P0 行为 | 命名测试类 / spec 路径 | 方法 / 用例 |
|---|---|---|
| 真登录（成功/失败；会话角色=账号角色） | `backend/.../security/SysUserAuthSecurityIntegrationTest.java` | `login_success_bindsAccountRole`；`login_wrongPassword_returns401`；`login_providerAndUser_bindOwnRoles` |
| 切角色禁用（三角色） | 同上 | `sessionRoleSwitch_disabledForAllRoles` |
| ADMIN CRUD + 软删不可登录 + 响应无 password/hash | 同上 | `adminUsers_crudAndSoftDelete_noPasswordLeak` |
| 非 ADMIN 用户管理 API 403 | 同上 | `adminUsers_nonAdmin_forbidden` |
| 冲突/不存在错误码 | 同上 | `adminUsers_usernameConflict_returns409`；`adminUsers_notFound_returns404` |
| 会话/RBAC 基线（旁证） | `backend/.../model/SessionAuthIntegrationTest.java`（或 security 包同名；surefire：`com.shdata.datachain.model.SessionAuthIntegrationTest`） | 6 tests，本轮一并实跑 |
| authStore 登录 / 切角色禁用 | `frontend/src/features/auth/store/authStore.spec.ts` | login success/failure；`switchRole` → `ERR_ROLE_SWITCH_DISABLED` |
| 角色只读唯一展示面（无 listbox） | `frontend/src/features/shell/components/RoleSwitcher.spec.ts` | readonly UX；Layout 永不挂载 |
| UsersAdminPage 权限与空载态 | `frontend/src/features/users/UsersAdminPage.spec.ts` | token hierarchy + loading/empty/error；ADMIN 门禁；壳层白名单 |

全部路径均存在且本轮 `tester-wsc-602` 实跑 exitCode=0。
