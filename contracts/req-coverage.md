# wsc-contracts — REQ 覆盖映射

> 基线 TASK-WSC-101；V1.6 增量见 SNAP-WSC-008 / PLAN-WSC-8.3（contracts@2.3.3 / TASK-WSC-903）。
> V1.7 增量见 SNAP-WSC-009 / PLAN-WSC-9.2（contracts@2.3.3 / TASK-WSC-911）。

## V1.7（SNAP-WSC-009 — 21 条 REQ 增量）

| REQ | priority | change | 契约/模型/路由 | 测试约束（后续任务） |
|---|---|---|---|---|
| REQ-WSC-ORDER-001 | P0 | 新增 | matrix ordersUI/ordersListApi；state-matrix 侧栏角色化文案 | 914/919 导航 + 角色列表 scope |
| REQ-WSC-ORDER-002 | P0 | 新增 | OpenAPI OrderProductSnapshot | 915 订购入口字段 |
| REQ-WSC-ORDER-003 | P0 | 新增 | OpenAPI POST /orders；OrderCreateRequest | 913/915 简易流程边界 |
| REQ-WSC-ORDER-004 | P0 | 新增 | OpenAPI GET /orders/notices/current；OrderNotice | 913/915 平台统一须知 |
| REQ-WSC-ORDER-005 | P1 | 新增 | 前端外部跳转（非 API 契约） | 918 外部跳转非建单 |
| REQ-WSC-ORDER-006 | P0 | 新增 | OpenAPI POST /orders 创建；OrderDetail 快照 | 913/919 创建+快照+mock |
| REQ-WSC-ORDER-007 | P0 | 新增 | OpenAPI GET /orders 分页；OrderPage | 913/919 分页 pageSize 档位 |
| REQ-WSC-ORDER-008 | P0 | 新增 | OpenAPI OrderListItem 九组字段 | 915/918 列表字段+脱敏 |
| REQ-WSC-ORDER-009 | P0 | 新增 | OpenAPI GET /orders/{orderId}；OrderDetail | 915/918 详情分区+快照 |
| REQ-WSC-ORDER-010 | P0 | 新增 | OpenAPI 状态枚举+四步 path；matrix 写能力键 | 916/919 主状态流转 |
| REQ-WSC-ORDER-011 | P0 | 新增 | matrix ordersConfirmApi/confirmContractApi/submitContractApi | 916/919 状态动作权限 |
| REQ-WSC-ORDER-012 | P0 | 新增 | OpenAPI POST /orders/{id}/cancel；matrix ordersCancelApi | 916/919 取消旁路 |
| REQ-WSC-ORDER-013 | P0 | 新增 | OpenAPI POST /orders/{id}/contract multipart | 916/919 签署附件+交易信息 |
| REQ-WSC-ORDER-014 | P0 | 新增 | OpenAPI OrderTransactionInfo 金额双字段；currency CNY | 916/919 计费+金额 |
| REQ-WSC-ORDER-015 | P0 | 新增 | OpenAPI POST /orders/{id}/contract/confirm | 916/919 统一合约确认页 |
| REQ-WSC-ORDER-016 | P0 | 新增 | OpenAPI OrderTimelineEvent/OrderChainLog；OrderChainAttestationPort | 913/916/919 时间线+mock |
| REQ-WSC-ORDER-017 | P1 | 新增 | OpenAPI OrderContractVersion | 918/919 数字合约版本 |
| REQ-WSC-ORDER-FE-001 | P0 | 新增 | 前端 toast（非 API 契约） | 915/917/919 写成功 toast |
| REQ-WSC-ORDER-FE-002 | P0 | 新增 | 前端二次确认（非 API 契约） | 917/919 取消二次确认 |
| REQ-WSC-ORDER-FE-003 | P0 | 新增 | 前端分页 jumper+pageSize（非 API 契约） | 915/919 分页跳页 |
| REQ-SHELL-011 | P0 | 修订 | matrix ordersUI；state-matrix 侧栏角色化 | 914/919 开放订单导航 |

### 继承声明（SNAP-WSC-008 不得削弱）

以下 REQ **行为继承** SNAP-WSC-008 / 既有基线，V1.7 **不得削弱**：

- `REQ-CAT-001..008`：浏览结构、详情、编辑、导入、分类维护等
- `REQ-CAT-012`：`supplierName` 模糊搜、未分类 API 全局置底
- `REQ-CAT-013`：维护页 Pagination / Cascader / 本页全选
- `REQ-CAT-014..019`：V1.6 浏览筛选、座序图 L1、mine 本企业、我的目录、全链无企业过滤、行业类别枚举复用
- `REQ-SHELL-009`、`REQ-SHELL-010`：目录双入口导航、侧栏企业只读
- `REQ-USER-002`、`REQ-RBAC-002`：用户企业归属、写权限企业范围
- `REQ-OVW-001..005`、`REQ-CHAIN-001`、`REQ-API-001`
- `REQ-UX-001..011`（SNAP-WSC-003）UX 令牌不回退

## V1.6（SNAP-WSC-008 — 十条 REQ）

| REQ | 契约/模型/路由 | 测试约束（后续任务） |
|---|---|---|
| REQ-CAT-014 | 浏览 query l1CategoryId/l2CategoryId；**无** browse industryCategory 叙述 | 901/908 Cascader；无卡片/toggle |
| REQ-CAT-015 | `GET /catalog/l2-distribution?l1CategoryId=`；无企业 filter | 902/908 座序图 L1；cross-enterprise parity |
| REQ-CAT-016 | `mine=true` → 本企业；ADMIN productWrite 200 ownEnterprise | 905/906/908 mine 本企业 |
| REQ-CAT-017 | maintenance `scope=myCatalog`；路由 `/my-catalog`；matrix myCatalogUI/Api | 907/908 myCatalog scope 分离 |
| REQ-CAT-018 | 全链 list/l2-distribution **无**登录企业 filter；安全说明 | 905/908 全链 parity |
| REQ-CAT-019 | browse 不传 industryCategory；editor/import 仍用 IndustryCategory enum | 901/907 industry 回归 |
| REQ-SHELL-009 | state-matrix 侧栏：我的目录；ADMIN 双入口；PROVIDER 无目录维护 | 906/908 导航 |
| REQ-SHELL-010 | Session enterpriseName 只读展示（P1） | 906 侧栏企业区 |
| REQ-USER-002 | Session/AdminUser **enterpriseId**+enterpriseName；AdminUserCreate enterpriseId | 904/908 会话 enterprise |
| REQ-RBAC-002 | matrix §3.1 字面表；maintenance scope=full\|myCatalog | 905/906 securityReviewer |

## V1.5 及更早（继承 — 不得削弱）

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
| REQ-CAT-007 | `/catalog/maintenance/**`；条目≡产品；分页字段同 Browse | scope=full\|myCatalog（V1.6） |
| REQ-CAT-008 | `/catalog/products/import*`；模板列最小集；请求级 vs 行级错误；报告 TTL | ≤10MB；四态；报告鉴权 |
| REQ-CHAIN-001 | versions/snapshot；快照含三级路径字段；ChainAttestationPort | 按 versionId 读快照 |
| REQ-SHELL-001 (V1.4) | 侧栏：`/my-products`、`/admin/users`；`POST /auth/session/role`→410+`ERR_ROLE_SWITCH_DISABLED`；`LoginRequest` 无 role；state-matrix 角色只读 | PROVIDER/ADMIN 菜单可见性；三角色切角色均禁用 |
| REQ-RBAC-001 (V1.4) | matrix 产品写/导入；`userManage*` 仅 ADMIN | V1.6 修订见 REQ-RBAC-002 |
| REQ-CAT-009 | `GET /catalog/l2-distribution`（`L2Distribution`：totalProducts+items）；可选 l1CategoryId | L2 Top5 + hover/leave；真实总数 |
| REQ-USER-001 | `/admin/users` 命名 schema `AdminUser`/`AdminUserCreate`/`AdminUserUpdate`；软删 PUT `deleted`；`ERR_USER_USERNAME_CONFLICT`/`ERR_USER_NOT_FOUND`；响应禁 password/hash | ADMIN CRUD；非 ADMIN 403 |
| REQ-CAT-010 | `/my-products`；`GET /catalog/products?mine=true`；`Product.createBy`；公共目录无增改导；导入宿主=我的产品 | V1.6 修订见 REQ-CAT-016（本企业） |
| REQ-CAT-011 | 高级筛选常显；浏览无 industryCategory | supersede → REQ-CAT-014 |
| REQ-CAT-012 | listProducts `supplierName` query；未分类排序 | 705/901 不回退 |
| REQ-CAT-013 | 维护 Pagination/Cascader/本页全选 | 907 我的目录复用 |

## 工程底座

| 制品 | 路径 |
|---|---|
| VERSION | `contracts/VERSION`（**2.3.3**） |
| OpenAPI | `contracts/openapi/openapi.yaml`（**2.3.3**） |
| 错误码 | `contracts/errors/codes.yaml` |
| RBAC | `contracts/rbac/matrix.yaml` |
| 存证端口 | `contracts/chain/ChainAttestationPort.md` |
| 订单存证端口 | `contracts/chain/OrderChainAttestationPort.md`（V1.7 新增） |
| OVW 流 | `contracts/overview/stream-events.yaml` |
| UI 状态 | `contracts/ui/state-matrix.md`（**2.3.3**） |
| 敏感字段 | `contracts/security/sensitive-fields.md` |
| Flyway | `backend/app/data-chain-service/.../sql/init` + `sql/migration` |
| API client | `frontend/src/api/**`（对齐 **2.3.3**；TASK-WSC-911 唯一写） |
