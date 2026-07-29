# DEV-TASK-WSC-002

```yaml
taskId: TASK-WSC-002
actorInstance: developer-wsc-002
status: READY_FOR_REVIEW
completedAt: 2026-07-28T17:42:00+08:00
reqs:
  - REQ-RBAC-001
  - REQ-SHELL-001
```

## 摘要

实现 §3.2 HttpSession 鉴权、三角色演示登录、角色切换（旧会话失效）、写 API 矩阵拦截（`ERR_FORBIDDEN`）、最低审计日志，以及工作台壳层（侧栏/企业信息/角色切换/登录守卫）。catalog 写路径以 `StubWriteController`（security 包）占位，供矩阵测试。

## SCOPE 例外

| 项 | 说明 |
|---|---|
| `application.yml` | **未修改**。会话 Cookie 名 `WSC_SESSION`、CORS（Vite `5173` + credentials）均用 Java Config（`SecurityWebConfig`）。 |
| `frontend/src/api/**` | **未修改**。角色切换走 feature 内 `features/auth/api/switchRole.ts` 直连 `POST /api/v1/auth/session/role`。 |
| `routes.ts` / `pom.xml` / migration / contracts | **未修改**。登录路由与 Layout 嵌套在 `router/index.ts` 完成（SCOPE_AMEND）。 |
| `planning/**` | 仅本 DEV 报告。 |

## 文件列表

### 后端（新增/更新）

| 路径 | REQ | 说明 |
|---|---|---|
| `backend/src/main/java/com/wsc/security/Role.java` | REQ-RBAC-001 | 角色枚举 |
| `backend/src/main/java/com/wsc/security/SessionPrincipal.java` | REQ-RBAC-001 | 会话主体 |
| `backend/src/main/java/com/wsc/security/SessionKeys.java` | REQ-RBAC-001 | Session 属性键 |
| `backend/src/main/java/com/wsc/security/ApiEnvelope.java` | REQ-RBAC-001 | 统一响应包装 |
| `backend/src/main/java/com/wsc/security/CorrelationIdSupport.java` | REQ-RBAC-001 | correlationId |
| `backend/src/main/java/com/wsc/security/DemoAccountService.java` | REQ-RBAC-001 | 演示账号 admin/provider/user，密码 `demo` |
| `backend/src/main/java/com/wsc/security/AuthAuditLogger.java` | REQ-RBAC-001 | `AUTH_LOGIN_FAILED` / `AUTH_FORBIDDEN` |
| `backend/src/main/java/com/wsc/security/LoginRequest.java` | REQ-RBAC-001 | 登录 DTO |
| `backend/src/main/java/com/wsc/security/SwitchRoleRequest.java` | REQ-RBAC-001 | 切角色 DTO |
| `backend/src/main/java/com/wsc/security/SessionViews.java` | REQ-RBAC-001 | Session JSON 视图 |
| `backend/src/main/java/com/wsc/security/AuthController.java` | REQ-RBAC-001 | POST/GET/DELETE `/api/v1/auth/session`；POST `/api/v1/auth/session/role` |
| `backend/src/main/java/com/wsc/security/StubWriteController.java` | REQ-RBAC-001 | 分类/产品写 stub（走拦截器） |
| `backend/src/main/java/com/wsc/security/SecurityWebConfig.java` | REQ-RBAC-001 | Cookie / CORS / Interceptor |
| `backend/src/main/java/com/wsc/security/package-info.java` | — | 包说明 |
| `backend/src/main/java/com/wsc/rbac/RbacMatrix.java` | REQ-RBAC-001 | matrix.yaml 写权限 |
| `backend/src/main/java/com/wsc/rbac/WriteAuthorizationInterceptor.java` | REQ-RBAC-001 | 写路径 401/403 |
| `backend/src/main/java/com/wsc/rbac/package-info.java` | — | 包说明 |
| `backend/src/test/java/com/wsc/rbac/RbacMatrixTest.java` | REQ-RBAC-001 | 单元矩阵 |
| `backend/src/test/java/com/wsc/security/SessionAuthIntegrationTest.java` | REQ-RBAC-001 | §4.1 + 登出 + 切角色旧凭证 |

### 前端（新增/更新）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/auth/store/authStore.ts` | REQ-RBAC-001 / REQ-SHELL-001 | session / login / logout / switchRole |
| `frontend/src/features/auth/api/switchRole.ts` | REQ-RBAC-001 | 切角色直连（不改 api 包） |
| `frontend/src/features/auth/composables/useCanWrite.ts` | REQ-RBAC-001 | 写入口可见性 helpers |
| `frontend/src/features/auth/composables/useCanWrite.spec.ts` | REQ-RBAC-001 | 权限可见性单测 |
| `frontend/src/features/auth/views/LoginView.vue` | REQ-SHELL-001 | 登录页 |
| `frontend/src/features/shell/components/RoleSwitcher.vue` | REQ-SHELL-001 | 三角色切换 UI |
| `frontend/src/features/shell/components/WriteEntryDemo.vue` | REQ-RBAC-001 | 壳层写入口演示 |
| `frontend/src/layouts/WorkbenchLayout.vue` | REQ-SHELL-001 | 侧栏/企业信息/未开放菜单 |
| `frontend/src/router/index.ts` | REQ-SHELL-001 | Layout 嵌套 + 登录守卫 |
| `frontend/src/App.vue` | REQ-SHELL-001 | 根 RouterView |
| `frontend/src/main.ts` | REQ-SHELL-001 | Pinia + Router（既有，保持启用） |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-RBAC-001** | 服务端会话角色；`RbacMatrix`+拦截器；ADMIN 分类+产品写；PROVIDER 仅产品写；USER 全 403；前端 `useCanWrite` 控制写入口可见性；切角色后 store 立即更新且旧会话拒绝 |
| **REQ-SHELL-001** | `WorkbenchLayout`：总览/数据目录；数据登记/交易订单/连接器管理 → `/unavailable/*`（Placeholder「本版本未开放」）；`enterpriseName`；`RoleSwitcher`；登录页与守卫 |

## 演示账号

| 用户名 | 密码 | 默认角色 |
|---|---|---|
| admin | demo | ADMIN |
| provider | demo | PROVIDER |
| user | demo | USER |

## 自测命令与结果

### 后端

```text
mvn "-Dtest=com.wsc.security.SessionAuthIntegrationTest,com.wsc.rbac.RbacMatrixTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `com.wsc.rbac.RbacMatrixTest` | 2 | PASS |
| `com.wsc.security.SessionAuthIntegrationTest` | 6 | PASS |
| **合计** | **8** | **BUILD SUCCESS** |

覆盖：§4.1 三角色矩阵；登出后写 API 401；角色变更后旧会话拒绝；登录失败 401；403 含 `ERR_FORBIDDEN` + `correlationId`；审计日志 `AUTH_LOGIN_FAILED` / `AUTH_FORBIDDEN`。

集成测试通过 `@SpringBootTest(properties=…)` 排除 DataSource/JPA/Flyway，避免无业务实体时依赖 DB（未改 `application.yml`）。

### 前端

```text
pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts
pnpm typecheck
```

| 命令 | 结果 |
|---|---|
| vitest `useCanWrite.spec.ts` | **3 passed** |
| `pnpm typecheck` | **exit 0** |

## 备注（联调）

- 前端默认 `VITE_API_BASE_URL` 为空时请求相对路径 `/api/v1`；跨端口联调请设 `VITE_API_BASE_URL=http://localhost:8080/api/v1`（CORS 已放行 `localhost:5173` / `127.0.0.1:5173` + credentials）。
- `POST /api/v1/auth/session/role` 为 §3.2「或等价」扩展，未写入冻结 OpenAPI；登录仍对齐契约 `POST/GET/DELETE /auth/session`。
