# 安全审查报告 — TASK-WSC-905

```yaml
reviewId: SEC-REV-TASK-WSC-905
taskId: TASK-WSC-905
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: securityReviewer
actorInstance: security-reviewer-wsc-905
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T13:40:00+08:00
trigger: auth-model-change
basedOn:
  - ai/agents/security-reviewer.md
  - ai/skills/security-review/SKILL.md
  - ai/rules/global/RULE-GLOBAL-SECRETS.md
  - planning/approved/PLAN-WSC-8.3.md（§3.1、§3.2、§5 TASK-WSC-905 acceptance、ISSUE-SEC-WSC8-R1-003/004/005）
  - product/requirements/SNAP-WSC-008.md（REQ-RBAC-002 / REQ-CAT-016 / REQ-CAT-018）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-905.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-905.md（codeReview APPROVE；不代本角色批准）
  - 实地：WriteAuthorizationInterceptor、RbacMatrix、EnterpriseProductScope、CatalogBrowseController/Service、ProductEditor*、ProductImport*
  - 只读对照：contracts/rbac/matrix.yaml、contracts/openapi/openapi.yaml
mustDifferFrom:
  - developer-wsc-905
  - code-reviewer-wsc-905
relatedReqs:
  - REQ-RBAC-002
  - REQ-CAT-016
  - REQ-CAT-018
riskTags: [rbac-breaking, data-isolation, enterprise-scope, auth-model-change]
note: |
  实现态审查。结论来自实地读拦截器/矩阵/scope/浏览与写路径，不采信 DEV 自检或 codeReview APPROVE。
  未改被审源码 / state.yaml / events.jsonl；未 git commit；未标 VERIFIED；不代替 tester。
```

## 结论

**APPROVE** — **P0=0，P1=0，P2=2**。

905 写集内的授权实现与 ISSUE-SEC-WSC8-R1-003/004/005 及 §3.1/§3.2 一致：mine/写/导入 **仅**信 `SessionPrincipal`；客户端 `enterpriseId` 不能扩大可见面或写范围；`mine=true` 按 `create_by → sys_user.enterprise_id` 本企业过滤（非 create_by-only）；PROVIDER `scope=full` → 403、`myCatalog` → 200；USER 任意 maintenance/mine/写/导入 → 403；ADMIN 本企业写/导入 200、跨企写 403；空/缺失 `create_by` 产品编辑 → 403；全链 `GET /catalog/products` 无登录企业 filter。未发现密钥入库、新的未认证公网端点或注入面。

**ADMIN `scope=myCatalog` 结果集仍全平台**：独立判定为 **非越权 / 非数据隔离失败（IDOR）**。ADMIN 已具备 `scope=full` 全平台只读与维护写；多返回的数据不超出其已授权能力。结果集 ownEnterprise 属 TASK-WSC-907 独占 maintenance service（905 writeSet 不含该文件）。见专节与 FIND-SEC-WSC-905-002（P2，Owner=907）。

**未代 tester 宣称测试通过**；未标 VERIFIED。codeReview APPROVE **不**覆盖本安全面。

## 触发与范围

| 项 | 内容 |
|---|---|
| 触发 | `auth-model-change`（另含 rbac-breaking / data-isolation / enterprise-scope） |
| 本任务写集 | `RbacMatrix`、`WriteAuthorizationInterceptor`、`EnterpriseProductScope`、`CatalogBrowse*`、`ProductEditor*`、`ProductImport*` 及对应测试 |
| 不在本任务 | `CatalogMaintenanceController/Service`（907）、`useCanWrite.ts`（906）、契约（903）、Flyway（904） |
| 本审查可批范围 | 实现态 IDOR/隔离/写授权/maintenance 拦截/密钥与未认证端点 |
| 本审查不可批 | tester 门禁、907 结果集过滤、人类剩余风险接受 |

## 规划期 SEC 约束对照

| 约束 | 结果 | 证据 |
|---|---|---|
| ISSUE-SEC-WSC8-R1-003：scope 仅信 SessionPrincipal；禁客户端 enterprise IDOR | **PASS** | browse 方法签名无 `enterpriseId`；`mineEnterpriseId = principal.enterpriseId()`；interceptor `isAllowed` 只读 `scope`；editor `validateWrite` 注释忽略 body 企业字段且未写入 ValidatedWrite；import actor 仅 `principal.userId()` |
| ISSUE-SEC-WSC8-R1-004：PROVIDER full→403、myCatalog→200 | **PASS** | `RbacMatrix.canMaintainCatalog(role, scope)`；`WriteAuthorizationInterceptor` MAINTENANCE 全方法检查；测试 `905-provider-maintenance-403` 含 GET/PUT `scope=full` 403 |
| ISSUE-SEC-WSC8-R1-005：空/缺失 create_by → 403 | **PASS（编辑路径）** | `EnterpriseProductScope.canWriteProduct` 空 create_by → false；`admin_writeOwnEnterprise200_crossAndEmpty403` ADMIN PUT 403。ADMIN myCatalog **维护**空归属随 907 service（现 `requireOwnershipIfProvider` 对 ADMIN 跳过） |
| §3.2 mine 本企业（非 create_by-only） | **PASS** | `matchesMineEnterprise` 比较 owner `enterprise_id` 与会话，不要求 `userId==create_by`；`905-mine-enterprise-scope` 同企异 create_by 可见、跨企不可见 |
| REQ-CAT-018 全链无企业 filter | **PASS** | `mine` 非 true 时 `mineEnterpriseId=null`；service 仅在非空时过滤；`fullChain_crossEnterpriseParity` 两企业同一 query 结果集相同 |
| REQ-RBAC-002 ADMIN 本企业写；PROVIDER ownCreateBy | **PASS** | interceptor 角色门禁 + `canWriteProduct` 对象级；跨企/异主/空归属 403 |
| 无密钥入库 | **PASS** | 写集无真实口令/令牌/连接串；`AuthAuditLogger` 禁 password/passwordHash；productSubmit 仅 productCode |
| 无新未认证公网端点 | **PASS** | 未新增 `security: []` 路由；写/维护/导入仍走拦截器；browse 仍 `requireLogin` |

### §3.1 矩阵逐 cell（本任务 API）

| 能力键 | ADMIN | PROVIDER | USER | 实地代码 | 结果 |
|---|---|---|---|---|---|
| catalogMaintenanceApi `scope=full` | 200 | **403** | 403 | `canMaintainCatalog(role,"full")` | **PASS** |
| myCatalogApi `scope=myCatalog` | 200（结果集 ownEnterprise→907） | 200 ownCreateBy（service `productsOwnedBy`） | 403 | 拦截器 + 既有 service | **PASS（门禁）** |
| productWriteApi | 200 ownEnterprise | 200 ownCreateBy | 403 | 拦截器角色 + `EnterpriseProductScope` | **PASS** |
| productImportApi | 200 ownEnterprise（create 绑定会话 userId） | 200（create 绑定会话） | 403 | `canImportProduct`；query `enterpriseId=999999` 仍 200 且不改归属 | **PASS** |
| UI 键 | — | — | — | 906 | 不在本任务 |

## 鉴权与授权（必查）

### 1. IDOR / 授权真源（ISSUE-SEC-WSC8-R1-003）

| 面 | 行为 | 结果 |
|---|---|---|
| mine | 仅 `AuthController.currentPrincipal` → `enterpriseId`；无 query 绑定 | **PASS** |
| 篡改 `?enterpriseId=` + mine | 测试：伪造未分配企业 id **不得**把跨企产品纳入 mine | **PASS** |
| 全链 + `enterpriseId`/`enterpriseName` query | 无筛选副作用（`query_hasNoEnterpriseNameFilter`；两企业 parity） | **PASS** |
| 产品 PUT `?enterpriseId=999` | 同企产品仍按会话 200，不因 query 扩大到跨企 | **PASS** |
| 导入 `.param("enterpriseId","999999")` | ADMIN 仍 200；create_by=会话 userId（`upsertProduct(..., actorUserId)`） | **PASS** |
| body `enterpriseId` | `ValidatedWrite` 无该字段；ImportFieldMapper 无 create_by/enterprise 列 | **PASS** |
| interceptor | `maintenanceScope` 只读 `scope`；注释与代码均不读企业参数 | **PASS** |

客户端不能通过契约外 query/body 选择「查/写哪家企业」。`AdminUserCreate.enterpriseId` 是 904 被管用户归属，不在 905 catalog 授权路径。

### 2. mine 本企业（REQ-CAT-016 / §3.2）

- `EnterpriseProductScope.matchesMineEnterprise`：空 create_by、用户不存在、`enterprise_id<=0` → **不可见**（fail-closed）。
- 同 enterpriseId 即本企业，**不**要求 create_by=当前用户。
- `CatalogBrowseService` 仅在 `mineEnterpriseId` 非空时过滤；全链不过滤。
- USER `mine=true` → `canAccessMineApi` false → 403。

**无 create_by-only 回退**（与 V1.5 `productsOwnedBy` 用于 PROVIDER **maintenance** 不同：那是 §3.2 myCatalog PROVIDER 规则，不是 mine）。

### 3. maintenance 拦截（ISSUE-SEC-WSC8-R1-004）

| 调用 | 结果 | 证据 |
|---|---|---|
| PROVIDER `scope=full` GET/PUT | **403** `ERR_MAINTENANCE_FORBIDDEN` | interceptor + 测试（含 query `enterpriseId=1` 仍 403） |
| PROVIDER `scope=myCatalog` GET | **200**；service `productsOwnedBy` | 拦截器放行后既有过滤 |
| USER `full` / `myCatalog` | **403** | `user_anyScope_403` |
| ADMIN 双 scope | **200** | 拦截器；结果集见专节 |
| 未知 scope | **拒绝** | `canMaintainCatalog` 非 full/myCatalog → false |
| 缺省空 scope | 遗留默认 ADMIN+PROVIDER 200 | PROVIDER 仍被 service 限制为 ownCreateBy，**不是**全量绕过 |

PUT/batch 无 `scope` 时拦截器走遗留默认；PROVIDER 写仍 `requireOwnershipIfProvider`（空/异主 403）。与 FIND-SEC-WSC-903-002 同口径：905 **未**用客户端 enterprise 补洞。

### 4. 写授权

| 场景 | 结果 |
|---|---|
| ADMIN 同企（含他人 create_by）PUT | 200 |
| ADMIN 跨企 PUT | 403 |
| ADMIN 空 create_by PUT | 403 |
| PROVIDER 他人 create_by PUT | 403 |
| USER 写/导入 | 403 |
| 新建/导入 | `create_by` 绑定会话 `userId`，无法经 body 冒领 |
| 导入撞码 | create 路径 409，不能覆盖他人产品 |

### 5. 全链（REQ-CAT-018）

`GET /catalog/products` 在 `mine` 非 true 时不传企业 filter；登录企业不影响结果集。产品详情 GET 对已登录任意角色可见，与全链公开目录口径一致，不构成本任务 IDOR。

### 6. ADMIN `scope=myCatalog` 结果集（独立判断）

实地：

- `CatalogMaintenanceController.list` **无** `scope` 形参、**不**读 `enterpriseId`。
- `CatalogMaintenanceService.list`：`PROVIDER` → `productsOwnedBy`；**否则（含 ADMIN）`catalog.products()` 全量**。
- 905 **未改**上述文件（§0.3 / writeSet / denyModify：maintenance service **907 独占**）。
- `MaintenanceScopeIntegrationTest.admin_scopeFullAndMyCatalog_200` 只断言 HTTP 200。

| 问题 | 判断 | 理由 |
|---|---|---|
| 是否越权 / 提权？ | **否** | 同一 ADMIN 调用 `scope=full` 已合法获得全平台列表；myCatalog 多返回的条目不超出 `catalogMaintenanceApi` 已授权能力 |
| 是否 IDOR / 跨租户读失败？ | **否（对 ADMIN）** | IDOR 指超出主体授权读取他人资源。ADMIN 在本模型是平台目录维护者（REQ-RBAC-002「目录维护 ADMIN=全量」） |
| 是否 myCatalog **语义**未完成？ | **是** | REQ-CAT-017 / 矩阵 `ownEnterprise` / 907 acceptance 要求本企业结果集；属 Wave D 过滤缺口，不是 905 拦截器缺陷 |
| 905 应否改 service？ | **否** | 改则违反 §0.3 与 907 writeSet，构成范围越界 |

**安全结论**：不构成 905 P0/P1；不因 DEV/codeReview「交 907」而放水（本结论基于拦截器能力边界 + ADMIN 已有 full 授权 + 源码 list 分支）。残留：907 落地前，ADMIN 若只调 myCatalog 会看到跨企条目；**写**维护接口对 ADMIN 仍跳过归属（`requireOwnershipIfProvider`），与 full 维护能力相同，907 须同时收紧 **列表与挂载** 的 ownEnterprise（含空 create_by 403，ISSUE-SEC-WSC8-R1-005 维护路径）。

## 密钥、敏感数据、注入、供应链

| 检查 | 结果 |
|---|---|
| 写集密钥/生产连接串 | **无** |
| 审计日志 | `AuthAuditLogger` 仅 username/userId/productCode/correlationId；sanitize 去 CRLF |
| 登录口令 | 未入 905 日志；hasher 在 904 路径 |
| 注入 | 列表过滤为内存 equals/contains；用户解析走 JPA `findById` / `findByUsernameAndDelFlag`，无拼接 SQL |
| 反序列化 | 写 body 为受控 Map 字段白名单（ValidatedWrite）；非任意 Java 反序列化 |
| 新依赖/CVE | 本任务未引入新依赖；N/A |
| Cookie | `WSC_SESSION` HttpOnly（既有 `SecurityWebConfig`）；非本任务增量 |

## Findings

| id | severity | status | 影响 | 证据 | closeWhen | Owner | relatedReqs |
|---|---|---|---|---|---|---|---|
| FIND-SEC-WSC-905-001 | P2 | OPEN | 会话 `enterpriseId` 为空串时，`mine=true` 不进入过滤分支，结果集退化为全链。全链本已对登录用户开放，**不扩大读机密面**；写路径 `matchesMineEnterprise` 对空白会话仍 false（403）。缺字段未按 903 所述 deny | `SessionPrincipal` compact 将 null 规范为 `""`；`CatalogBrowseController` L107 直接赋值；`CatalogBrowseService` L139 `mineEnterpriseId != null && !isBlank()` 才过滤。`enterprise_id=0` 走过滤且 owner `<=0` → 不可见（orphan fail-closed），与空串路径不同 | `mine=true` 且会话 enterpriseId 空白 → **403**（或空集且绝不返回全链）；补一条负例 | developer-wsc-905 或后续热修 | REQ-CAT-016, REQ-USER-002 |
| FIND-SEC-WSC-905-002 | P2 | OPEN | ADMIN `GET .../maintenance/entries?scope=myCatalog` 结果集仍全平台；**非提权**。907 前「我的目录」API 语义未完成 | `CatalogMaintenanceService.list` L72–76；905 未改该文件；PLAN §0.3 / 907 writeSet / 907 acceptance「ADMIN `/my-catalog` 本企业」 | 907：ADMIN `scope=myCatalog` 仅本企业；跨企条目不出现；空 create_by 维护 403；具名用例 `907-my-catalog-scope-split` | developer-wsc-907 | REQ-CAT-017, REQ-RBAC-002 |

## 残留风险（不构成 905 P0/P1）

| 项 | 说明 | 接受身份 |
|---|---|---|
| ADMIN myCatalog 列表/挂载未 ownEnterprise | FIND-SEC-WSC-905-002；905 禁止改 maintenance service | 907 securityReviewer + 人类 Owner |
| 空 scope 维护请求 | 拦截器遗留默认；PROVIDER 对象级仍 ownCreateBy | 后续契约若要 PUT 可测 scope |
| 会话缺 enterpriseId | 904 正常登录应写入 id；001 为纵深 | 904/905 热修 |
| 剩余风险正式接受 | Agent **不得**关闭未修 P0；本轮无 P0 | securityOperationsOwner |

## 未做事项

- 未跑 tester `testScope` / Maven；**不**宣称测试通过或 VERIFIED。
- 未审 906 `useCanWrite`、907 maintenance 结果集实现（源码只读对照现状）。
- 未改 `state.yaml` / `events.jsonl` / 被审源码。

## 决策依据

- P0=0、P1=0 → **APPROVE**（有条件建议：P2 不阻塞进入独立 tester；007 前须 907 闭合 myCatalog 结果集）。
- 不可接受风险未发现 → 不 **BLOCK**。
- codeReview APPROVE **不**替代本角色；本结论与其「非提权、交 907」在 ADMIN myCatalog 上 **独立同向**，证据为 service 全量分支 + ADMIN 已有 `scope=full`。
- 修复 P2 非本轮强制；P0/P1 若后续复审出现则须 REQUEST_CHANGES。
