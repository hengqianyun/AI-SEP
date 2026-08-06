# 预落地对账表 601 行 — P0 自动化证据定位

> 对照 PLAN-WSC-5.2 §0.2 对账表 **601** 行 + §3.1 硬冻结。  
> 执行器：`assert-601-p0.mjs`（EXIT 0，见 `02-p0-assert-exit.txt`）+ grep `05-regression-21.txt`。

| P0 行为（对账表 / §3.1） | 文件路径定位 | 断言名 / 证据 |
|---|---|---|
| VERSION=`2.2.0`；OpenAPI info.version 一致 | `contracts/VERSION`；`contracts/openapi/openapi.yaml` L4 | `VERSION==2.2.0`；`OpenAPI info.version 2.2.0` |
| `rbac/matrix.yaml` 与 §4 / SNAP 一致 | `contracts/rbac/matrix.yaml`；交叉表 `03-matrix-vs-section4.md` | matrix 三角色 productWrite/Import/userManage/sessionRoleSwitch 系列 |
| openapi 含 `/admin/users`、命名 AdminUser | `contracts/openapi/openapi.yaml` L309+；schema L1117+ | `OpenAPI /admin/users`；`AdminUser.required includes *` |
| `/catalog/l2-distribution` | `contracts/openapi/openapi.yaml` L291+；`L2Distribution` L1187+ | `OpenAPI /catalog/l2-distribution`；`L2Distribution named schema` |
| `mine` on products | `contracts/openapi/openapi.yaml` L475 `name: mine` | `OpenAPI mine param on products` |
| `ERR_ROLE_SWITCH_DISABLED` ∈ codes + enum；POST role→410 | `contracts/errors/codes.yaml` L82+；openapi enum L1054；path L87–116 | codes + enum + session/role 410 断言 |
| `LoginRequest` 无 role | openapi L1090–1098；`frontend/src/api/auth.ts` L16–18 | `LoginRequest has no role property`；auth.ts block |
| 用户错误码 CONFLICT/NOT_FOUND | `contracts/errors/codes.yaml` L88–96；openapi enum L1055–1056 | codes + enum 断言 |
| state-matrix 2.2.0；导入宿主=我的产品 | `contracts/ui/state-matrix.md` 头 + L14/L42 | state-matrix 版本/宿主断言 |
| req-coverage V1.4 行 | `contracts/req-coverage.md` L23–27 | req-coverage has REQ-* |
| 2.1.0 不回退：typeSpecific / 导入码 / 白名单 | openapi TypeSpecific*；`codes.yaml` importSemantics + reportFieldsWhitelist | `05-regression-21.txt` + 2.1.0* 断言 |
| client 生成可消费 | `frontend/src/api/client.ts` CONTRACT_VERSION；admin/auth/catalog | client/auth/admin/catalog 断言 + typecheck EXIT 0 |

禁止仅 prose「已对账」——上表每行均有路径 + 可命名断言。
