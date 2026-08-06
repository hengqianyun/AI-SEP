# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC5-R1-apiDataDesigner
planId: PLAN-WSC-5.1
round: 1
role: apiDataDesigner
actorInstance: api-data-designer-wsc-008-r1
snapshotIdAtReview: SNAP-WSC-005
contractsBaselineAtReview: wsc-contracts@2.1.0
contractsTargetAtReview: wsc-contracts@2.2.0
decision: REQUEST_CHANGES
summary: |
  PLAN-WSC-5.1 对 2.2.0 升版主线、matrix 三角色、mine / l2-distribution 形状、
  601 独占 contracts+api 生成大体可调度，且与只读核对的 matrix.yaml / VERSION=2.2.0
  声称一致。阻塞 APPROVE：① 角色切换禁用契约仍用「建议」软措辞，且未要求废止
  LoginRequest 可选 role、也未钉死 OpenAPI 路径+错误码硬同步；② /admin/users 仅要求
  「路径存在」，未冻结命名 schema / 软删形状 / 响应禁出 password / 用户冲突码，预落地
  OpenAPI 仍为 type:object 骨架，601/602 易分叉。另有 OpenAPI/state-matrix 与 RBAC
  叙述漂移（P2）建议同轮吸收。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-5.1.md`；`product/requirements/SNAP-WSC-005.md`（`IN_REVIEW`）；只读 `contracts/rbac/matrix.yaml`、`contracts/VERSION`、抽查 `openapi.yaml` / `errors/codes.yaml` / `ui/state-matrix.md` |
| 聚焦 | 2.2.0 冻结范围、`/admin/users`、`/catalog/l2-distribution`、`mine`、错误码、RBAC matrix、601 独占契约写集 |
| 不裁定 | 页面交互/文案；威胁建模结论；他角色 ISSUE；DAG/写集工程细节（除非直接破坏契约所有权） |
| 禁区 | 不代批；不写 `ai/runs/**`；不直接改 `contracts/**`（只评计划） |

## 对照核对

### 1. `wsc-contracts@2.2.0` 冻结范围与 601 独占 — **通过（计划级）**

| 检查项 | 计划证据 | 只读契约现状 | 判定 |
|---|---|---|---|
| `contractsTarget` | front-matter / §1.2 / §3：`2.2.0`；已是 2.2.0 则对账禁止再 bump | `contracts/VERSION` = `2.2.0`；OpenAPI `info.version: 2.2.0` | **通过** |
| 独占写集 | §3.2 / TASK-601：唯一写 `contracts/**` + `frontend/src/api/**`；602..604 denyModify | — | **通过** |
| 不回退面 | §3.1：2.1.0 typeSpecific / 导入四态 / 报告白名单 / 分类维护错误码 | codes 仍含 TEMPLATE_UNSUPPORTED 与 importSemantics | **通过（计划声明）** |
| req-coverage / state-matrix | §3.1 + 601 writeSet/acceptance 点名更新 | req-coverage 已有 V1.4 行；state-matrix 头仍标 2.1.0、导入宿主仍写目录页 | 方向通过；漂移见 **ISSUE-003** |

### 2. RBAC `matrix.yaml` — **通过（计划声称 ↔ 契约一致）**

| 能力 | 计划 §4 | `contracts/rbac/matrix.yaml` @2.2.0 | 判定 |
|---|---|---|---|
| 产品写/导入 | ADMIN **隐藏/403**；PROVIDER 200；USER 403 | 同左（`ERR_FORBIDDEN`） | **一致** |
| 分类/目录维护 | 仅 ADMIN 200 | 同左（维护 API 非 ADMIN → `ERR_MAINTENANCE_FORBIDDEN`） | **一致** |
| `userManage*` | 仅 ADMIN | `userManageUI/Api` 仅 ADMIN | **一致** |
| `myProductsUI` | 仅 PROVIDER | 同左 | **一致** |
| 切角色 | 三角色均禁用 | notes：登录绑定角色；禁止任意会话切角色 | **意图一致**；契约路径/码见 **ISSUE-001** |
| 隐藏≠授权 | §1.4 / REQ-RBAC-001 | notes 首条 | **通过** |

### 3. `GET /catalog/products?mine=` — **通过（计划级）**

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| 语义 | §3.1：`mine=true` → 仅 `create_by=当前用户` | **通过** |
| 所有权 | 601 冻契约；603 实现过滤 + create_by 隔离 | **通过** |
| 预落地 | OpenAPI 已有 `mine` query | 对账路径正确；不单开 ISSUE |
| 非 PROVIDER 调 `mine` | 计划未写 403 vs 空列表 | **可接受**（UI 门禁 + 列表过滤即可）；不升 P1 |

### 4. `GET /catalog/l2-distribution` — **通过（计划级）**

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| 形状 | §3.1：`totalProducts` + `items[]`（code/name/count），按 count 降序；消费方 Top5 | **通过** — 与预落地 OpenAPI required 字段一致 |
| 真实总数 | 604 acceptance：`totalProducts` 非 Top5 之和冒充 | **通过** |
| 范围 | 仅公共目录消费；我的产品不挂 | UI 归属 604；契约面无歧义 | **通过** |

### 5. `/admin/users` — **缺口（P1）**

| 检查项 | 计划证据 | 预落地 OpenAPI | 判定 |
|---|---|---|---|
| 路径存在 | §3.1 / 601 acceptance「含 `/admin/users`」 | `GET/POST /admin/users`；`PUT /admin/users/{userId}` | 路径有；**形状未冻** |
| 命名 schema | **未**写入 §3.1 冻结表 | list `200` schema=`type: object`；无 `AdminUser` / 分页 envelope | **缺口** |
| 软删语义 | 仅文字「软删」；未钉 DELETE vs `deleted` 字段 | PUT body 可选 `deleted: boolean`；无 DELETE | 计划未裁决/验收 | **缺口** |
| 敏感字段 | 未要求响应禁出 password/hash | create/update 有 password；list 无字段约束 | **缺口** |
| 错误码 | §3.1「用户管理相关码（**若有**）」 | codes.yaml **无**用户冲突/不存在码 | **软措辞** → 见 ISSUE-002 |

### 6. 错误码 / 会话切角色 — **缺口（P1）**

| 检查项 | 计划证据 | 契约现状 | 判定 |
|---|---|---|---|
| 切角色禁用 | §3.1「**建议** 410 + … `ERR_ROLE_SWITCH_DISABLED`」；§0.3 提示 codes 可能未入 | OpenAPI **无** `/auth/session/role`；`codes.yaml` **无**该码；`ApiResponseError.code` enum 无该码 | **未硬冻结** |
| LoginRequest.role | 计划未要求废止客户端选角色 | `LoginRequest` 仍可选 `role` | **与「登录绑定账号角色」冲突风险** |
| 601 acceptance | 「角色切换禁用错误码已硬同步」 | 未点名码值、HTTP 状态、OpenAPI path、LoginRequest 修订 | 偏软；见 ISSUE-001 |

### 7. 数据迁移协议 — **通过（计划级，本角色边界内）**

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| `sys_user` | §3.3：602 独占 Flyway；禁止 `db/migration/` | **通过** |
| `create_by` | 优先无破坏；缺列/索引才 603 追加；601/604 deny sql | **通过** |
| 历史空归属 | §8 风险：不可被 PROVIDER 冒领 | 实现策略可接受；不单开契约 ISSUE |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-WSC5-R1-001 | P1 | PLAN §3.1 会话行对 `POST /auth/session/role` 使用「**建议** 410 + 稳定错误码」软措辞；601 acceptance 仅笼统「禁用语义/错误码硬同步」，未钉死 path、HTTP 状态、码名，也未要求修订仍允许客户端传 `role` 的 `LoginRequest`。只读现状：OpenAPI 无该 path；`errors/codes.yaml` 与 `ApiResponseError.code` enum 均无 `ERR_ROLE_SWITCH_DISABLED`（或等价稳定码）。后果：602 可自选 403/410/无文档端点，client 生成与测试矩阵无法引用同一契约真源，削弱 REQ-SHELL-001「下线自由切角色」。 | 在 §3.1 冻结表**硬性**写明（禁止「建议」）：(1) OpenAPI **必须**收录 `POST /auth/session/role`（或计划点名的等价 path）为**始终禁用**操作，固定 HTTP 状态（推荐 410）+ 稳定 `code`（如 `ERR_ROLE_SWITCH_DISABLED`）及 example；(2) 同码写入 `errors/codes.yaml` 且进入 OpenAPI `ApiResponseError.code` enum；(3) `LoginRequest` **不得**再接受客户端选择角色（删除 `role` 属性，或明确 ignore 并在 description 声明角色仅来自 `sys_user`）；(4) 601 `acceptance`/`testScope` 可测上述四点。 | REQ-SHELL-001, REQ-RBAC-001, REQ-USER-001 |
| ISSUE-API-WSC5-R1-002 | P1 | §3.1 / 601 对用户管理仅要求 OpenAPI「含 `/admin/users`」与模糊「CRUD」。预落地 OpenAPI：`GET` 200 为裸 `type: object`；无命名 `AdminUser`（或等价）列表/详情 schema；软删仅隐式 `deleted` boolean，计划未冻结；未要求 list/get **永不**返回 password/hash；用户名冲突/用户不存在等错误码落在「若有」软句。后果：601 生成的 client 与 602 实现 DTO/错误语义易分叉，REQ-USER-001 契约面计划级不可验收。 | §3.1 增加用户资源冻结行，至少包含：(1) 命名响应 schema 必填字段（如 `userId`/`username`/`displayName`/`role`/`enterpriseName`/`deleted` 或等价审计字段）；(2) 创建/更新请求 schema（create：username+password+role；update：改角色/资料/软删字段）；(3) 软删权威形状（PUT `deleted` **或** DELETE——二选一写死）；(4) 响应/列表禁止密码明文或 hash；(5) 最少错误码：非 ADMIN→`ERR_FORBIDDEN`；username 唯一冲突→稳定 409 码（新建或点名复用策略）；用户不存在→404 语义；(6) 601 acceptance：路径 + **命名 schema** + 上述错误码硬同步 `codes.yaml`/OpenAPI examples，不得仅「路径存在」。 | REQ-USER-001 |
| ISSUE-API-WSC5-R1-003 | P2 | 预落地契约与 V1.4 RBAC/入口叙述仍漂移，而计划未把「废止冲突叙述」列为 601 硬验收：OpenAPI `createProduct` summary 仍写「管理员/提供方」；`ui/state-matrix.md` 仍写导入载体为目录页弹窗、版本头仍偏 2.1.0；§3.1 错误码「用户管理相关码（若有）」延续上轮已否决的软措辞模式。遗漏则 2.2.0 文档自相矛盾，前端/测试按旧叙述实现。 | §3.1 / 601 acceptance **硬性**要求：(1) 修订一切仍暗示 ADMIN 可产品写/导入的 OpenAPI summary/description，与 matrix「仅 PROVIDER」一致；(2) state-matrix 更新侧栏可见性、公共目录只读、我的产品写入口、角色只读展示，并改导入宿主为「我的数据产品」；(3) 删除「若有」——用户管理码按 ISSUE-002 清单硬同步，或显式写「本版仅 ERR_FORBIDDEN + 下列码」。 | REQ-RBAC-001, REQ-CAT-010, REQ-SHELL-001 |

## 不阻塞项（记录）

- `l2-distribution` 形状与「消费方 Top5 / 真实 totalProducts」计划级可验收；实现断言归 604。
- `mine=true` 过滤语义与 601/603 分工正确；Product schema 是否暴露 `createBy` 字段本版可不强制（隔离以服务端为准）。
- `matrix.yaml` 与计划 §4 / SNAP 角色表一致；本角色确认计划声称无误。
- 601 独占 `contracts/**` + `frontend/src/api/**`、后继只读：符合兼容与所有权纪律。
- SNAP 仍 `IN_REVIEW`：发布/派发门禁属编排，不单独构成契约 ISSUE。

## 决策

`decision: REQUEST_CHANGES`

关闭 **ISSUE-API-WSC5-R1-001**、**ISSUE-API-WSC5-R1-002**（均为 P1）前，本角色 **不** APPROVE。**ISSUE-API-WSC5-R1-003**（P2）建议同轮吸收，避免 Round 2 仅清文档债。

本评审 **未** 修改计划正文、源码、`contracts/**` 或 Run `state`/`events`。
