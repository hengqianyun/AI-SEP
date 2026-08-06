# wsc-contracts — REQ 覆盖映射

> 基线 TASK-WSC-101；V1.4 增量见 SNAP-WSC-005 / PLAN-WSC-5.2（contracts@2.2.0 / TASK-WSC-601）。

| REQ | 契约/模型/路由 | 测试约束（后续任务） |
|---|---|---|
| REQ-SHELL-001 | 路由：`/overview`、`/catalog`、目录维护入口占位；`contracts/ui/state-matrix.md` | 壳层；管理员可见目录维护；未开放提示 |
| REQ-RBAC-001 | `contracts/rbac/matrix.yaml`；OpenAPI 写端点 + `ERR_FORBIDDEN` / `ERR_MAINTENANCE_FORBIDDEN`；securitySchemes SessionCookie | §4 三角色×分类/维护/产品/导入 |
| REQ-OVW-001 | `GET /overview/metrics` | 指标卡空库空态 |
| REQ-OVW-002 | `GET /overview/trend` | 趋势空态 |
| REQ-OVW-003 | `GET /overview/top` | TOP 排序 |
| REQ-OVW-004 | `GET /overview/stream` + `contracts/overview/stream-events.yaml` | 三类类型、倒序 |
| REQ-OVW-005 | `GET /overview/distribution` | 行业/地域；行业口径可对齐 L1/L2 标签 |
| REQ-CAT-001 | `GET /catalog/products`、categories（L1/L2/L3）；Browse `page`/`pageSize`/`total` | 三级浏览 + 滚动加载 |
| REQ-CAT-002 | 列表 query（含 l2/l3）；筛选空态 | 无结果空态 |
| REQ-CAT-003 | 路由：详情/上链/编辑；RBAC 编辑可见性 | 普通用户无编辑 |
| REQ-CAT-004 | Product/CatalogSnapshot；`categoryPath` + `categoryPathParts`；`ProductCode` | 详情三级路径 |
| REQ-CAT-005 | `POST/PUT /catalog/products`（必填 `industryCategory`；`l3CategoryId` 可空）；FORMAT/CONFLICT；显式非法 L3 → `ERR_CATEGORY_LEAF_REQUIRED`；OQ-004；DEC-WSC-006 | 新建/编辑/上链 |
| REQ-CAT-006 | categories CRUD（L1/L2/L3）；`ERR_CATEGORY_HAS_PRODUCTS` | 有挂载禁止删；非管理员 403 |
| REQ-CAT-007 | `/catalog/maintenance/**`；条目≡产品；分页字段同 Browse | 管理员维护；提供方/用户 403 |
| REQ-CAT-008 | `/catalog/products/import*`；模板列最小集；请求级 vs 行级错误；报告 TTL | ≤10MB；四态；报告鉴权 |
| REQ-CHAIN-001 | versions/snapshot；快照含三级路径字段；ChainAttestationPort | 按 versionId 读快照 |
| REQ-SHELL-001 (V1.4) | 侧栏：`/my-products`、`/admin/users`；`POST /auth/session/role`→410+`ERR_ROLE_SWITCH_DISABLED`；`LoginRequest` 无 role；state-matrix 角色只读 | PROVIDER/ADMIN 菜单可见性；三角色切角色均禁用 |
| REQ-RBAC-001 (V1.4) | `matrix.yaml` 2.2.0；产品写/导入仅 PROVIDER（ADMIN hidden+403）；`userManage*` 仅 ADMIN | ADMIN 写产品 403；隐藏≠授权 |
| REQ-CAT-009 | `GET /catalog/l2-distribution`（`L2Distribution`：totalProducts+items）；前端 CirculationSeatMap | L2 Top5 + hover/leave；真实总数 |
| REQ-USER-001 | `/admin/users` 命名 schema `AdminUser`/`AdminUserCreate`/`AdminUserUpdate`；软删 PUT `deleted`；`ERR_USER_USERNAME_CONFLICT`/`ERR_USER_NOT_FOUND`；响应禁 password/hash | ADMIN CRUD；非 ADMIN 403 |
| REQ-CAT-010 | `/my-products`；`GET /catalog/products?mine=true`；`Product.createBy`；公共目录无增改导；导入宿主=我的产品 | 仅本人；空归属 403 |

## 工程底座（TASK-WSC-101 / 升版至 2.2.0）

| 制品 | 路径 |
|---|---|
| OpenAPI | `contracts/openapi/openapi.yaml`（**2.2.0**） |
| 错误码 | `contracts/errors/codes.yaml` |
| RBAC | `contracts/rbac/matrix.yaml` |
| 存证端口 | `contracts/chain/ChainAttestationPort.md` |
| OVW 流 | `contracts/overview/stream-events.yaml` |
| UI 状态 | `contracts/ui/state-matrix.md`（版本头 2.2.0） |
| 敏感字段 | `contracts/security/sensitive-fields.md` |
| Flyway | `backend/app/data-chain-service/.../sql/init` + `sql/migration` |
| API client | `frontend/src/api/**`（对齐 **2.2.0**；TASK-WSC-601 唯一写） |
| V1.0→V1.1 schema | `ops/runbooks/wsc-v11-schema-migration.md` |
