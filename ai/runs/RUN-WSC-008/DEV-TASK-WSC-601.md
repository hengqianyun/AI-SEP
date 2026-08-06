# DEV-TASK-WSC-601

```yaml
taskId: TASK-WSC-601
runId: RUN-WSC-008
actorInstance: developer-wsc-601
planId: PLAN-WSC-5.2
contractsTarget: wsc-contracts@2.2.0
status: READY_FOR_REVIEW
completedAt: 2026-08-05T15:20:00+08:00
reqs:
  - REQ-RBAC-001
  - REQ-SHELL-001
  - REQ-CAT-009
  - REQ-USER-001
  - REQ-CAT-010
```

> 交付说明落于 `ai/runs/RUN-WSC-008/`（避免 `planning/**` denyModify）。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## 对账结论（预落地 → §3.1）

| 项 | 预落地状态 | 本任务动作 |
|---|---|---|
| `VERSION` / OpenAPI `info.version` | 已是 `2.2.0` | **确认，禁止再 bump** |
| `rbac/matrix.yaml` | 大致对齐；缺切角色行 | 补 `sessionRoleSwitch: 410`；notes 对齐 §4 / SNAP |
| `/admin/users` | 路径存在，**裸 `type: object`** | 改为命名 `AdminUser` / `Create` / `Update` + 信封；软删 PUT `deleted`；409/404 码 |
| `/catalog/l2-distribution` | 存在，inline schema | 升为命名 `L2Distribution*` |
| `GET /catalog/products?mine` | 已有 | 保留；`Product.createBy` 补可读字段 |
| `POST /auth/session/role` | **缺失** | 新增：始终 410 + `ERR_ROLE_SWITCH_DISABLED` |
| `LoginRequest.role` | **仍存在** | **删除**；description 声明角色仅来自 `sys_user` |
| 错误码 | 缺切角色/用户冲突/404 | `codes.yaml` + OpenAPI enum **硬同步** |
| OpenAPI「ADMIN 可产品写」 | `createProduct` summary 含「管理员/提供方」 | 改为仅 PROVIDER；import 同口径 |
| `ui/state-matrix.md` | 版本头 2.1.0；导入宿主=目录弹窗 | **2.2.0**；导入宿主=我的产品；角色只读；公共目录只读 |
| 2.1.0 导入/typeSpecific/报告白名单 | 已在 | **不回退**（验收脚本勾选） |
| `frontend/src/api/**` | 部分 2.1.0 / 缺 admin·L2·切角色 | 手工对齐生成至 2.2.0（仓库无 openapi-generator 脚本） |

无未决 ISSUE（偏差均已就地修正）。

## 改动摘要（writeSet）

| 路径 | 说明 |
|---|---|
| `contracts/VERSION` | 保持 `2.2.0` |
| `contracts/openapi/openapi.yaml` | §3.1 硬冻结全量对账修正 |
| `contracts/errors/codes.yaml` | +`ERR_ROLE_SWITCH_DISABLED` / `ERR_USER_*` |
| `contracts/rbac/matrix.yaml` | §4 对齐 + sessionRoleSwitch |
| `contracts/ui/state-matrix.md` | 2.2.0 壳层/导入宿主/只读角色 |
| `contracts/req-coverage.md` | V1.4 映射行 + 工程底座版本 |
| `contracts/security/sensitive-fields.md` | 禁回显 password/hash |
| `frontend/src/api/client.ts` | `CONTRACT_VERSION=2.2.0` |
| `frontend/src/api/auth.ts` | 无 role 登录；`switchSessionRole` |
| `frontend/src/api/admin.ts` | **新建** 用户 CRUD client |
| `frontend/src/api/catalog.ts` | `getL2Distribution` + `createBy` |
| `frontend/src/api/overview.ts` / `chain.ts` / `index.ts` | 版本头与 re-export |

未改：`frontend/package.json` / lock（无需新依赖）。

## acceptance 勾选

- [x] `VERSION`=2.2.0；OpenAPI info.version 一致
- [x] `rbac/matrix.yaml` 对齐 §4（ADMIN 产品写/导入 hidden+403；PROVIDER 写/导入；USER 只读；userManage 仅 ADMIN）
- [x] `/admin/users` 命名 AdminUser schema；GET/POST + PUT `/{userId}`；软删 PUT `deleted`；响应禁 password/hash
- [x] `GET /catalog/l2-distribution`；`GET /catalog/products` 支持 `mine`
- [x] `POST /auth/session/role`：410 + `ERR_ROLE_SWITCH_DISABLED`；codes + enum 硬同步；`LoginRequest` 无 role
- [x] `ERR_USER_USERNAME_CONFLICT`(409)、`ERR_USER_NOT_FOUND`(404)、`ERR_FORBIDDEN`
- [x] 废止 ADMIN 产品写/导入叙述；state-matrix 2.2.0；导入宿主=我的产品；角色只读；公共目录只读
- [x] 2.1.0 typeSpecific / 导入四态码 / 报告白名单不回退
- [x] `req-coverage.md` 已更新
- [x] `frontend/src/api/**` 可 typecheck
- [x] 预落地对账完成（见上表）

## 自测命令与结果

```text
# P0 对账自动化（一次性 node 断言；33 项）
node contracts/_check-601-acceptance.mjs
→ ALL P0 ACCEPTANCE CHECKS PASSED (exit 0)
# 脚本已删除；证据见本段与下方清单项名

# 前端 typecheck
cd frontend && pnpm run typecheck
→ EXIT 0（vue-tsc --noEmit；无错误）

# 既有官方契约脚本（不在 writeSet，未改）
node tests/contracts/check-contracts.mjs
→ FAILED：仍硬编码 VERSION expected 1.1.0, got 2.2.0
  （脚本滞后于 2.x；非本任务范围。2.2.0 语义由上方 P0 清单覆盖）
```

### 预落地对账自动化证据定位

| P0 检查 | 证据 |
|---|---|
| `ERR_ROLE_SWITCH_DISABLED` ∈ codes + OpenAPI enum | `contracts/errors/codes.yaml`；`openapi.yaml` `ApiResponseError.code` enum |
| `LoginRequest` 无 role | `openapi.yaml` `LoginRequest` properties 仅 username/password；`frontend/src/api/auth.ts` `LoginRequest` |
| `AdminUser` required 齐全 | `openapi.yaml` `AdminUser.required`；`frontend/src/api/admin.ts` |
| matrix 三角色 × 关键能力 | `contracts/rbac/matrix.yaml` |
| 导入/typeSpecific/白名单不回退 | `openapi.yaml` TypeSpecific*；`codes.yaml` importSemantics.reportFieldsWhitelist |

## REQ 映射

| REQ | 契约落点 |
|---|---|
| REQ-RBAC-001 | matrix 2.2.0；产品写仅 PROVIDER |
| REQ-SHELL-001 | 切角色 410；state-matrix 角色只读 / 菜单面 |
| REQ-CAT-009 | `/catalog/l2-distribution` |
| REQ-USER-001 | `/admin/users` + 用户错误码 |
| REQ-CAT-010 | `mine` + createBy；公共目录只读叙述 |

待独立 codeReviewer / tester；developer **不**标 VERIFIED。
