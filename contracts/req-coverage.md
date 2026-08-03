# wsc-contracts@2.0.0 — 17 REQ 覆盖映射

> TASK-WSC-101 冻结契约与骨架迁移底座；主实现归属见 PLAN-WSC-2.2 §5 / §8。

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

## 工程底座（TASK-WSC-101）

| 制品 | 路径 |
|---|---|
| OpenAPI | `contracts/openapi/openapi.yaml`（2.0.0） |
| 错误码 | `contracts/errors/codes.yaml` |
| RBAC | `contracts/rbac/matrix.yaml` |
| 存证端口 | `contracts/chain/ChainAttestationPort.md` |
| OVW 流 | `contracts/overview/stream-events.yaml` |
| UI 状态 | `contracts/ui/state-matrix.md` |
| 敏感字段 | `contracts/security/sensitive-fields.md` |
| Flyway | `backend/app/wsc-service/.../sql/init` + `sql/migration` |
| API client | `frontend/src/api/**`（对齐 2.0.0） |
| V1.0→V1.1 schema | `ops/runbooks/wsc-v11-schema-migration.md` |
