# 代码审查报告 — TASK-WSC-905

```yaml
reviewId: REV-TASK-WSC-905
taskId: TASK-WSC-905
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-905
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T13:25:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-8.3.md（§0.3、§3.1 矩阵、§3.2、§4、§5 TASK-WSC-905 writeSet/denyModify/acceptance/testScope；TASK-WSC-907 writeSet/acceptance）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-905.md
  - product/requirements/SNAP-WSC-008.md（REQ-RBAC-002、REQ-CAT-016、REQ-CAT-018）
  - contracts/rbac/matrix.yaml（只读对照）
  - 实地 diff：RbacMatrix、WriteAuthorizationInterceptor、EnterpriseProductScope、CatalogBrowse*、ProductEditor*、ProductImport*、相关测试
mustDifferFrom: developer-wsc-905
riskTags: [rbac-breaking, data-isolation, enterprise-scope, auth-model-change]
note: |
  命中 auth-model-change：本 REV 仅 codeReview 门禁；**不得**代 securityReviewer 批准。
  未代 tester 宣称 VERIFIED；未改源码 / state.yaml / events.jsonl。
```

## 结论

**APPROVE** — **P0=0，P1=0**。实现满足 PLAN-WSC-8.3 §5 TASK-WSC-905 在 **writeSet 可完成面** 的验收：`mine=true` 按 §3.2 `create_by → sys_user.enterprise_id` 本企业过滤；全链无登录企业 filter；ADMIN 本企业写/导入 200、跨企写 403；PROVIDER 不可写他人；USER mine/写/导入/maintenance 403；interceptor 对 maintenance `scope=full|myCatalog` 做 200/403；授权仅信 `SessionPrincipal`。`EnterpriseProductScope.java` 为 `common/security` **同包增量**，在范围内。

**ADMIN `scope=myCatalog` 结果集仍全平台**：判定为 **范围内交接 TASK-WSC-907**，**不是** 本任务 P1 验收缺口（证据见专节）。开放 **P2×2** 不阻塞进入独立 tester / securityReviewer 门禁。

## Scope 检查（denyModify / writeSet）

| 项 | 结果 | 证据 |
|---|---|---|
| `RbacMatrix` / `WriteAuthorizationInterceptor` | **PASS** | 字面 writeSet；scope-aware 重载 + query `scope` |
| `CatalogBrowseController` / `CatalogBrowseService` | **PASS** | mine 仅取会话 `enterpriseId`；全链不传企业 filter；恢复 `supplierName` |
| editor / productimport controller+service | **PASS** | 传 `SessionPrincipal`；忽略 body/query `enterpriseId`；写范围走 `EnterpriseProductScope` |
| **`EnterpriseProductScope.java` 同包增量** | **PASS** | 新文件位于已有包 `common.security`（与矩阵/拦截器同包）；browse 与 editor 共享 §3.2 判定，符合「`common/` 至少两模块共享」。未新建业务域顶层包 |
| 未改 `CatalogMaintenanceController` / `CatalogMaintenanceService` | **PASS** | git status 无 M；907 独占 BE scope 实现 |
| 未改 `contracts/**`、`frontend/src/api/**` | **PASS（905 交付）** | DEV filesChanged 未列；工作树脏文件归属 TASK-WSC-903 |
| 未改 `useCanWrite.ts` | **PASS** | git status 无 M；现文件仍 `canWriteProduct === PROVIDER`（906 独占） |
| 未改 `WorkbenchLayout.vue`、browse FE、maintenance FE | **PASS（905 交付）** | useCanWrite/layout/maintenance FE 无本任务增量；browse FE 脏树归属 901/902 |
| 未改 `L2DistributionController/Service` | **PASS（905 交付）** | DEV 未列；脏树归属 TASK-WSC-902 |
| 未改 `**/sql/**`、e2e、planning、product | **PASS（905 交付）** | V7 归属 904；planning 非本任务 |
| 未改 `state.yaml` / `events.jsonl` | **PASS** | 本实例只写本 REV；DEV 声明未改控制面 |
| 包结构 | **PASS** | 无 `catalog/`、`rbac/`、`security/` 等业务域顶层包；测试在 `catalog.browse` / `catalog.maintenance` / `rbac` |

工作树并行脏文件（901/902/903/904）不计入 905 越界。

## ADMIN `scope=myCatalog` 结果集 — 交接判定

DEV 交 907：ADMIN `scope=myCatalog` 拦截器 200，**结果集仍全平台**。审查对照计划后的独立结论：

**范围内交接，不是本任务 P1。**

| 计划条款 | 含义 |
|---|---|
| §0.3 | 905 **独占** interceptor/matrix；maintenance controller/service（scope API）**907**（BE scope 实现） |
| 905 writeSet | **不含** `CatalogMaintenanceController.java` / `CatalogMaintenanceService.java` |
| 905 denyModify | 「service 层 scope 过滤若需与 907 协调，**907 改 maintenance service**；interceptor **仅本任务**」 |
| 907 writeSet | **含** 上述 controller/service + `src/test/java/**/catalog/maintenance/**` |
| 907 acceptance / testScope | ADMIN `/my-catalog`（`scope=myCatalog`）**本企业**；命名用例 `907-my-catalog-scope-split`（全平台 N vs 本企业 M） |
| 905 objective | 点名 `RbacMatrix` / `WriteAuthorizationInterceptor` **scope-aware**（403/200），未要求本任务改 maintenance 列表过滤 |

实地代码：

- `CatalogMaintenanceService.list`（约 L72–76）：`PROVIDER` → `productsOwnedBy`；**否则（含 ADMIN）`catalog.products()` 全量**。不读 `scope`。
- `CatalogMaintenanceController.list`：无 `scope` 形参，不按企业过滤。
- `MaintenanceScopeIntegrationTest.admin_scopeFullAndMyCatalog_200`：只断言 HTTP 200，**不**断言结果集本企业；类注释写明交 907。
- 905 若在本任务改 maintenance service，将违反 §0.3 / 907 独占，构成 **范围越界**。

905 acceptance 字面有「ADMIN `scope=myCatalog` → 本企业」，与 907 acceptance **重复**。机械所有权以 **writeSet / §0.3 / denyModify 括号** 为准：905 完成授权门禁（ADMIN 双 scope 200；PROVIDER `full` 403 / `myCatalog` 200）；**结果集 ownEnterprise** 由 907 消费 905 的 interceptor 放行之后落地。

安全边界：ADMIN 已具备 `scope=full` 全平台，myCatalog 暂返回全量 **不是提权**，是「我的目录」语义未完成，归 Wave D。

## Acceptance 对照

| acceptance 项 | 结果 | 证据 |
|---|---|---|
| ADMIN/PROVIDER `mine=true` 本企业（同企异 create_by 可见；跨企不可见） | **PASS** | `EnterpriseProductScope.matchesMineEnterprise`；`CatalogBrowseService` mine 过滤；`@DisplayName("905-mine-enterprise-scope")` |
| ADMIN 本企业写/导入 200；跨企业写 403 | **PASS** | `canWriteProduct` ADMIN→本企业、PROVIDER→`userId==create_by`；`admin_writeOwnEnterprise200_crossAndEmpty403`；`admin_importOwnEnterprise_200` |
| 全链无登录企业 filter；supplierName/未分类不回退 | **PASS** | `fullChain_crossEnterpriseParity`；`ListProductsFilterSortIntegrationTest` |
| scope 仅信 SessionPrincipal；篡改 enterprise 不扩大可见面 | **PASS** | browse 忽略 query `enterpriseId`/`enterpriseName`；editor/import 注释+测试 `.param("enterpriseId","999")` 仍按会话；interceptor 只读 `scope` |
| PROVIDER `scope=full` → 403；`myCatalog` → 200（service create_by 过滤） | **PASS** | interceptor + 既有 `productsOwnedBy`；`@DisplayName("905-provider-maintenance-403")` |
| ADMIN `scope=full` → 全平台；`myCatalog` → 本企业 | **PASS（905 层：双 scope HTTP 200）**；结果集本企业 **交 907** | 见专节 |
| 空/缺失 create_by：ADMIN/PROVIDER 写 → 403 | **PASS（编辑路径）** | `canWriteProduct` 空 create_by → false；ADMIN/PROVIDER PUT 负例。ADMIN myCatalog **维护**空归属 403 随 907 service |
| USER mine/写/导入/maintenance 403 | **PASS** | `user_mineWriteImportMaintenance_403`；`user_anyScope_403` |
| `RbacMatrix` 与 §3.1 / matrix.yaml 一致 | **PASS（API 单元格）** | 见下表。UI 键归 906 |

## `RbacMatrix` 与 §3.1 / matrix.yaml 逐 cell

| 能力键 | ADMIN | PROVIDER | USER | 代码 |
|---|---|---|---|---|
| `catalogMaintenanceApi` `scope=full` | 200 | **403** | 403 | `canMaintainCatalog(role,"full")` |
| `myCatalogApi` `scope=myCatalog` | 200（ownEnterprise 结果集→907） | 200 ownCreateBy | 403 | 角色门禁本任务；PROVIDER 过滤已在 maintenance service |
| `productWriteApi` | 200 ownEnterprise | 200 ownCreateBy | 403 | 拦截器角色 + `EnterpriseProductScope` |
| `productImportApi` | 200 ownEnterprise | 200 | 403 | `canImportProduct`；create_by=会话 userId |
| `categoryWriteApi` | 200 | 403 | 403 | 未改，仍仅 ADMIN |
| UI 键（maintenance/myCatalog/myProducts/productWrite） | — | — | — | **906** `useCanWrite.ts` |

缺省空 `scope` 仍走 V1.5 遗留（ADMIN+PROVIDER 200），与「显式 `full`/`myCatalog`」单元格不冲突；契约要求带 enum 时已覆盖。

## 测试覆盖（testScope）

| 项 | 结果 |
|---|---|
| USER：mine/写/导入/maintenance 403 | **有** `user_mineWriteImportMaintenance_403`、`user_anyScope_403` |
| PROVIDER：他人 create_by 写 403 | **有** `admin_writeOwnEnterprise200_crossAndEmpty403_providerForeign403`；`CreateByOwnershipIntegrationTest.provider_updateOwn_200_emptyAndOther_403` |
| PROVIDER `scope=full` 403；myCatalog 200 | **有** `905-provider-maintenance-403`（200 仅 `hasItem` 本人，**未**断言排除他人 → P2） |
| ADMIN 本企业写 200；跨企 403 | **有** |
| ADMIN full / myCatalog **200** | **有**（结果集分离交 907 具名用例） |
| 全链两 enterprise 同一 query 结果集相同 | **有** `fullChain_crossEnterpriseParity` |
| 篡改 enterprise 参数；空 create_by；同企多 ADMIN + 跨企 seed | **有** |
| supplierName、未分类跨页；query 无 `enterpriseName` 副作用 | **有** |
| §0.2 命名用例 `905-mine-enterprise-scope`、`905-provider-maintenance-403` | **有** `@DisplayName` |

**未**代替 tester 宣称 PASS/VERIFIED。本实例未重跑 Maven。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-905-001 | P2 | OPEN | `security/SessionAuthIntegrationTest.rbacMatrix_admin_categoryMaintenanceOk_productImportForbidden` 仍期望 ADMIN POST 产品/导入 **403**（V1.5）。905 将 `canWriteProduct`/`canImportProduct` 改为 ADMIN+PROVIDER 后，该用例与矩阵不一致；路径在 `**/security/**`，**不在** 905 writeSet | 将该用例改为 ADMIN 本企业写/导入 **200**（或加 `@DisplayName` 迁入 905 测试包），避免全量 `mvn test` 红 | REQ-RBAC-002 |
| FIND-WSC-905-002 | P2 | OPEN | `MaintenanceScopeIntegrationTest` PROVIDER `myCatalog` 只 `hasItem` 本人编码，未插入他人 `create_by` 条目并断言不出现。testScope 要求「myCatalog **200 仅本人**」；service 已按 `productsOwnedBy` 过滤，缺负例 | 同测例增加非本人产品，断言 `items` 不含该编码 | REQ-RBAC-002 |

## 架构与可维护性备注（非阻塞）

- `EnterpriseProductScope` 抽在 `common.security` 正确：browse mine 与 editor 写共用同一套 create_by→enterprise_id 规则。
- 拦截器不读客户端 `enterpriseId`，企业范围下沉 Service，符合「隐藏 ≠ 授权 / 禁止 IDOR」。
- `ProductImportController` 存在重复 `import ProductImportService`（HEAD 已有，本任务仅改 Javadoc/注释），不升格。
- `CreateByOwnershipIntegrationTest.mineTrue_listsOnlyOwnCreateBy` 仍用 V1.5 异主假 id；因假 id 无法解析为企业，与本企业规则兼容，本企业正例已由 `905-mine-enterprise-scope` 覆盖。

## 决策依据

- P0=0、P1=0 → **APPROVE**，可进入独立 **tester** 与 **securityReviewer**（`auth-model-change`）门禁。
- 本 REV **不**代安全批准；**不**宣称测试门禁通过。
- ADMIN myCatalog 结果集过滤 **不得**在 905 返工改 907 独占文件；由 907 闭合。
