# 安全审查报告 — TASK-WSC-903

```yaml
reviewId: SEC-REV-TASK-WSC-903
taskId: TASK-WSC-903
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: securityReviewer
actorInstance: security-reviewer-wsc-903
decision: REQUEST_CHANGES
p0Count: 0
p1Count: 1
p2Count: 2
reviewedAt: 2026-08-17T12:10:00+08:00
trigger: auth-model-change
basedOn:
  - ai/agents/security-reviewer.md
  - ai/skills/security-review/SKILL.md
  - ai/rules/global/RULE-GLOBAL-SECRETS.md
  - planning/approved/PLAN-WSC-8.3.md（§3.1、§5 TASK-WSC-903、§6.2）
  - product/requirements/SNAP-WSC-008.md（REQ-USER-002 / REQ-RBAC-002 / REQ-CAT-016 / REQ-CAT-017 / REQ-CAT-018 / REQ-SHELL-009）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-903.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-903-R2.md（codeReview APPROVE；不代本角色批准）
  - 实地：contracts/rbac/matrix.yaml、contracts/openapi/openapi.yaml、contracts/ui/state-matrix.md、frontend/src/api/**
mustDifferFrom:
  - developer-wsc-903
  - developer-wsc-903-r2
  - code-reviewer-wsc-903
  - code-reviewer-wsc-903-r2
relatedReqs:
  - REQ-USER-002
  - REQ-RBAC-002
  - REQ-CAT-016
  - REQ-CAT-017
  - REQ-CAT-018
  - REQ-SHELL-009
riskTags: [contracts-bump, rbac-breaking, enterprise-scope, auth-model-change]
note: |
  本任务为契约/client 冻结，无后端授权实现。审查对象是可测契约与 IDOR/绕过叙述，不代替 905 实现态复审，不代替 tester 宣称通过。
  未改源码 / state.yaml / events.jsonl；未 git commit；未标 VERIFIED。
```

## 结论

**REQUEST_CHANGES** — **P0=0，P1=1，P2=2**。

契约权威面（OpenAPI / `matrix.yaml` / state-matrix）与 §3.1 安全冻结整体一致：PROVIDER `catalogMaintenanceApi` `scope=full` → 403、ADMIN 产品写 `200 ownEnterprise`、`mine=true` 本企业、maintenance `scope=full|myCatalog` 硬冻结、全链无企业 filter、Session `enterpriseId` 必填、拒绝客户端 enterprise 参数作授权依据。未发现密钥入库或新的未认证公网端点。

阻塞项：**`frontend/src/api/catalog.ts` `Product.createBy` 仍写「mine=true 过滤依据」**，与已修复的 OpenAPI `Product.createBy.description`、ISSUE-API-WSC8-R1-003 / REQ-CAT-016 冲突。codeReview R2 仅关闭 OpenAPI 侧 FIND-WSC-903-R1-001，**未覆盖** 903 写集内的 client 同源叙述。905/906 若按 client JSDoc 实现，会把 mine 做成 create_by-only。

**未代 tester 宣称测试通过**；未标 VERIFIED。905 实现态仍须独立 `securityReviewer`（同 `auth-model-change`）。

## 触发与范围

| 项 | 内容 |
|---|---|
| 触发 | `auth-model-change`（policies.riskTriggers.securityReviewer；PLAN §6.2 条 5） |
| 本任务写集 | `contracts/**`、`frontend/src/api/**` |
| 不在本任务 | 后端 interceptor / Flyway / `useCanWrite`（904/905/906） |
| 本审查可批范围 | 契约可测、无 IDOR 暗示、无绕过叙述、无密钥入库 |
| 本审查不可批 | 905 运行时隔离、E2E 三角色、人类剩余风险接受 |

## 规划期 SEC 约束对照

| 约束 | 结果 | 证据 |
|---|---|---|
| matrix 与 §3.1 逐 cell：PROVIDER catalogMaintenance 403 | **PASS** | `matrix.yaml` PROVIDER `catalogMaintenanceApi: { expect: 403, code: ERR_MAINTENANCE_FORBIDDEN, scope: full }`；UI `hidden` |
| ADMIN 产品写 200 ownEnterprise | **PASS** | ADMIN `productWriteApi` / `productImportApi` `{ expect: 200, on: ownEnterprise }`；OpenAPI create/update/import description |
| myCatalogUI / myCatalogApi | **PASS** | ADMIN `visible` / `200 ownEnterprise scope=myCatalog`；PROVIDER `visible` / `200 ownCreateBy`；USER `hidden` / `403` |
| mine=true 本企业（非 create_by-only）— OpenAPI / matrix | **PASS** | OpenAPI L9 / L519 / L563；matrix notes L68；`Product.createBy` L1703 已改审计字段 |
| mine=true 本企业 — **client** | **FAIL** | 见 FIND-SEC-WSC-903-001 |
| maintenance `scope=full\|myCatalog` 契约冻结 | **PASS**（GET） | `listMaintenanceEntries` `scope` **required** enum `[full, myCatalog]`；matrix notes L69 |
| 全链无企业 filter + 安全说明 | **PASS** | OpenAPI info L9/L13–L14；listProducts L518–L523；l2-distribution L303「不按登录用户企业过滤」；matrix notes L68/L76 |
| 会话 enterpriseId 契约可测、无 IDOR 暗示 | **PASS**（OpenAPI） | `Session` / `AdminUser` **required** `enterpriseId`+`enterpriseName`；listProducts / maintenance / ProductWrite **无** enterprise 授权参数 |
| 无密钥入库 | **PASS** | 写集无真实口令/令牌/连接串；`password` 仅 schema `format: password`；`sensitive-fields.md` 禁止响应/日志回显口令 |
| 无绕过叙述（权威契约） | **PASS** | 「仅信 SessionPrincipal」「拒绝/忽略客户端 enterpriseId」多处重复；LoginRequest 无 role；`POST /auth/session/role` 410 |
| 无绕过叙述（client） | **FAIL** | FIND-SEC-WSC-903-001 |

### §3.1 矩阵逐 cell（安全相关）

| 能力键 | ADMIN | PROVIDER | USER | 实地 matrix.yaml | 结果 |
|---|---|---|---|---|---|
| catalogMaintenanceUI | visible | hidden | hidden | 一致 | **PASS** |
| catalogMaintenanceApi（scope=full） | 200 | **403** `ERR_MAINTENANCE_FORBIDDEN` | 403 | 一致 | **PASS** |
| myCatalogUI | visible | visible | hidden | 一致 | **PASS** |
| myCatalogApi（scope=myCatalog） | 200 ownEnterprise | 200 ownCreateBy | 403 | 一致 | **PASS** |
| myProductsUI | visible | visible | hidden | 一致 | **PASS** |
| productWriteUI / ImportUI | visible | visible | hidden | 一致 | **PASS** |
| productWriteApi | 200 ownEnterprise | 200 ownCreateBy | 403 | 一致 | **PASS** |
| productImportApi | 200 ownEnterprise | 200（cell 为 `on: valid`） | 403 | 见 FIND-SEC-WSC-903-003 | **P2** |

## 鉴权与授权（必查）

### 1. 授权真源（ISSUE-SEC-WSC8-R1-003）

- OpenAPI info、listProducts、createProduct、listMaintenanceEntries、matrix notes、state-matrix 实现约束均写明：mine / myCatalog / 写 / 维护 scope **仅**信服务端 `SessionPrincipal`。
- `GET /catalog/products` 参数表 **无** `enterpriseId` / `enterpriseName` query。
- `ProductWrite` / `MaintenanceAssociate` / `MaintenanceBatchAssociate` **无** enterprise 字段；create_by **不**在写模型（绑定会话 userId）。
- `frontend/src/api/catalog.ts` `ListProductsQuery` / `ListMaintenanceEntriesQuery` / `L2DistributionQuery` **无** enterprise 字段；`getL2Distribution` 只传 `l1CategoryId`。

**无 IDOR 暗示**：客户端不能通过契约内合法参数选择「查哪家企业的目录」。`AdminUserCreate` / `AdminUserUpdate` 的 `enterpriseId` 是**被管用户归属**（REQ-USER-002），不是当前会话 catalog 授权参数；904 须默认继承操作者企业，且不得用该字段覆盖操作者 `SessionPrincipal`。

### 2. maintenance scope（ISSUE-SEC-WSC8-R1-004）

- GET `scope` 硬冻结 `full | myCatalog`，required。
- PROVIDER `full` → 403、`myCatalog` → 200 ownCreateBy：契约与 matrix 一致，**可测**。
- USER 任意 maintenance → 403。
- PUT / batch **叙述**「scope 规则同 list」，但 **无** `scope` 参数 → FIND-SEC-WSC-903-002（P2，905 须按角色 fail-closed，不得另开客户端 enterprise 参数补洞）。

### 3. 三角色 mine · 全链

| 面 | 契约语义 | 结果 |
|---|---|---|
| 全链 listProducts（mine 缺省 false） | 全平台；无登录企业 filter | **PASS** |
| mine=true | 本企业（create_by → sys_user.enterprise_id 与会话 enterpriseId 比较） | OpenAPI **PASS**；client JSDoc **FAIL** |
| l2-distribution | 可选 l1CategoryId；不按企业过滤 | **PASS** |
| ADMIN 写 | ownEnterprise 200；跨企叙述为 403 | **PASS** |
| PROVIDER 写 | ownCreateBy；空归属不可冒领（OpenAPI L1703） | **PASS** |
| USER 写/维护/mine 写入口 | 403 / UI hidden | **PASS** |

### 4. 会话字段

- OpenAPI `Session.required` 含 `enterpriseId`+`enterpriseName` → **契约可测**。
- client `Session.enterpriseId?` 仍 optional（codeReview FIND-WSC-903-R1-002）：实现归 904；**905/906 缺字段必须 deny，禁止当「无企业过滤」**。不升 P1（权威 schema 已 required）。

## 密钥、敏感数据、供应链

| 检查 | 结果 |
|---|---|
| 写集密钥/生产连接串 | **无** |
| 登录/用户口令 | 仅 schema；响应禁 `password`/`passwordHash`（AdminUser description + sensitive-fields） |
| 角色由客户端选择 | **禁止**（LoginRequest 无 role；切角色 410） |
| 新公网未认证端点 | 无新增；`POST /auth/session` 既有 `security: []` |
| 导入报告白名单 | state-matrix 未扩大 PII |
| 依赖/CVE | 本任务未引入新依赖；N/A |

## Findings

| id | severity | status | 影响 | 证据 | closeWhen | Owner | relatedReqs |
|---|---|---|---|---|---|---|---|
| FIND-SEC-WSC-903-001 | **P1** | OPEN | 903 写集内 client 仍把 `createBy` 标成 mine 过滤依据，与 OpenAPI L1703 / REQ-CAT-016 本企业语义冲突；905/906 只读 `frontend/src/api/**` 时可能实现 create_by-only，造成同企可见面收缩或错误授权模型 | `frontend/src/api/catalog.ts` L148：`/** 创建者 userId；mine=true 过滤依据 */`。对照同文件 L192「true → 本企业产品」与 OpenAPI L1703。R2 称「全文件已无 mine=true 过滤依据」仅对 OpenAPI 成立 | 删除「mine=true 过滤依据」；改为与 OpenAPI L1703 同口径（审计/归属字段，非本企业 scope 依据；mine 用 enterpriseId 比较，非 create_by-only） | developer-wsc-903（writeSet 内） | REQ-CAT-016 |
| FIND-SEC-WSC-903-002 | P2 | OPEN | PUT/batch 无 `scope` 时，905 若默认 `full` 会误伤 PROVIDER myCatalog 写；若另加客户端 enterprise 参数则引入 IDOR | OpenAPI `updateMaintenanceEntry` / `batchUpdateMaintenanceEntries` 无 scope query/body；仅 description「等同 list 规则」。PLAN §3.1 GET 硬冻结；batch/put 为规则等同而非参数冻结 | 本任务可不补参数（GET 已可测 PROVIDER full→403）。905 **禁止**用客户端 enterprise 补 scope；无参 PUT/batch：USER 403，PROVIDER 按 myCatalog+ownCreateBy，ADMIN 按角色最大权或 907 后续显式传 scope（须再开契约任务） | 905（实现）/ 后续契约若要 PUT 可测 scope | REQ-CAT-017, REQ-RBAC-002 |
| FIND-SEC-WSC-903-003 | P2 | OPEN | PROVIDER `productImportApi` cell 为 `on: valid`，§3.1 字面为 `ownCreateBy`；测试若只信 cell 可能漏跨会话冒领负例 | `contracts/rbac/matrix.yaml` L41 vs §3.1「productWriteApi / ImportApi … PROVIDER 200 ownCreateBy」。OpenAPI import description 已写 PROVIDER ownCreateBy | 将 PROVIDER `productImportApi.on` 改为 `ownCreateBy`，或在 notes 钉死 `valid` ≡ 导入创建绑定会话 userId 且禁止冒领。905 测试仍须 ownCreateBy 负例 | developer（契约）或 905 tester | REQ-RBAC-002 |

## 残留风险（不构成 903 P0/P1；提交人类 Owner 前须 905 复审）

| 项 | 说明 | 接受身份 |
|---|---|---|
| 运行时隔离未实现 | mine 本企业、scope-aware interceptor、篡改 enterprise 负例、空 create_by 403 均归 **905**；本报告不批准运行时安全 | 905 独立 securityReviewer |
| Session client optional | FIND-WSC-903-R1-002：904 落地前缺 `enterpriseId` 时 905 **fail-closed** | 904/905 |
| AdminUser 跨企赋值 | Create/Update 可带 `enterpriseId`；须默认继承操作者企业。是否允许 ADMIN 把用户派到其他企业由 904 钉死，不得当作 catalog IDOR 通道 | 904 |
| 剩余风险正式接受 | Agent **不得**关闭未修 P0；本轮无 P0 | securityOperationsOwner |

## 未做事项

- 未跑 tester `testScope`（契约 lint / 矩阵交叉自动化 / typecheck）；**不**宣称测试通过。
- 未审 905 Java interceptor / 906 `useCanWrite`（denyModify / 非本任务）。
- 未改 `state.yaml` / `events.jsonl` / 源码。

## 决策依据

- P0=0。
- P1=1（FIND-SEC-WSC-903-001：client 残留 create_by-only 过滤叙述）→ **不得 APPROVE**。
- 修复后由本角色复审；P0/P1 清零方可进入 tester 门禁（仍不代替 tester）。
- codeReview R2 APPROVE **不**覆盖本安全面。
