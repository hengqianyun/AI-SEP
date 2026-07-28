# wsc-contracts@1.1.0 — 14 REQ 覆盖映射

> TASK-WSC-001 提供契约与工程底座二级覆盖；主实现归属见 PLAN-WSC-1.1 §8。

| REQ | 契约/模型/路由 | 测试约束（后续任务） |
|---|---|---|
| REQ-SHELL-001 | 路由：`/overview`、`/catalog`、未开放占位；`contracts/ui/state-matrix.md` 壳层行 | 壳层点击「本版本未开放」；写入口随角色更新 |
| REQ-RBAC-001 | `contracts/rbac/matrix.yaml`；OpenAPI 写端点 + `ERR_FORBIDDEN`；securitySchemes | §4.1 三角色×分类/产品写必跑 |
| REQ-OVW-001 | `GET /overview/metrics` | 指标卡空库空态 |
| REQ-OVW-002 | `GET /overview/trend` | 趋势空态 |
| REQ-OVW-003 | `GET /overview/top` | TOP 排序 |
| REQ-OVW-004 | `GET /overview/stream` + `contracts/overview/stream-events.yaml` | fixture 注入三类类型、倒序 |
| REQ-OVW-005 | `GET /overview/distribution` | 行业/地域口径 |
| REQ-CAT-001 | `GET /catalog/products`、categories；路由 `/catalog` | 三栏浏览 |
| REQ-CAT-002 | 列表 query 参数（级联/来源/类型/公共数据/交付/q） | 无结果空态 |
| REQ-CAT-003 | 路由：详情/上链/编辑；RBAC 编辑可见性 | 普通用户无编辑 |
| REQ-CAT-004 | Product/CatalogSnapshot schema；编码 `ProductCode` | 详情分组；编码格式 |
| REQ-CAT-005 | `POST/PUT /catalog/products`；错误码 FORMAT/CONFLICT；ChainAttestationPort；OQ-004 | 新建 v1 / 编辑递增 / 失败无半成品 |
| REQ-CAT-006 | categories CRUD；`ERR_CATEGORY_HAS_PRODUCTS` | 有挂载禁止删；非管理员 403 |
| REQ-CHAIN-001 | `GET .../versions`、`GET .../snapshot`；ChainAttestationPort 字段 | 按 versionId 读完整快照 |

## 工程底座

| 制品 | 路径 |
|---|---|
| OpenAPI | `contracts/openapi/openapi.yaml` |
| 错误码 | `contracts/errors/codes.yaml` |
| RBAC | `contracts/rbac/matrix.yaml` |
| 存证端口 | `contracts/chain/ChainAttestationPort.md` |
| OVW 流 | `contracts/overview/stream-events.yaml` |
| UI 状态 | `contracts/ui/state-matrix.md`（§3.5） |
| 敏感字段 | `contracts/security/sensitive-fields.md`（§3.7） |
| Flyway 初版 | `backend/.../V202607281600__init_wsc.sql` |
| API client | `frontend/src/api/**`（对齐 1.1.0） |
| 回滚 runbook | `ops/runbooks/rollback.md` |
