# Round 1 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC8-R1-securityOperations
planId: PLAN-WSC-8.1
round: 1
role: securityOperations
actorInstance: security-operations-wsc-011-r1
snapshotIdAtReview: SNAP-WSC-008
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-008 与 PLAN-WSC-8.1：企业 scope、ADMIN 双入口（全量目录维护 vs 我的目录本企业）、
  PROVIDER 隔离、全链无企业过滤、会话 enterprise 绑定、§0.3 对 RbacMatrix/WriteAuthorizationInterceptor
  的独占写集（905）与全任务 denyModify 控制面等方向正确，§4/§6.1/905·907 acceptance 亦已有部分可测意图。
  阻断 APPROVE 的缺口：
  ① RBAC/企业 scope 实质为鉴权模型变更，905/903 riskTags 缺 auth-model-change，§6.2 未硬绑定 securityReviewer；
  ② 904 迁移标签与 migrationReviewer 门禁未对齐 policies 正式名；
  ③ mine/myCatalog/写路径未强制「scope 仅取自会话 principal」，存在客户端 enterprise 参数 IDOR 面；
  ④ PROVIDER catalogMaintenanceApi=403 与「我的目录」维护 UX 的 API 授权分裂未在 903 矩阵 / 905 interceptor / 907 任务间写清；
  ⑤ 空/缺失 create_by 与跨企业冒领负例未落入 905 acceptance/testScope。
  无证据 BLOCK 业务目标；不代批其他角色；不伪造高风险接受；不写 plan/state/events。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-8.1.md`（`CANDIDATE` / `DRAFT` / Round 1） |
| 快照 | `product/requirements/SNAP-WSC-008.md`（`status: APPROVED`；PO 2026-08-17） |
| policies | `ai/workflow/policies.yaml` → `riskTriggers.securityReviewer`: `auth-model-change` / `new-public-endpoint` / `handles-pii`；`migrationReviewer`: `schema-migration` / `data-backfill` |
| 实现现状（只读对账） | `RbacMatrix` 仍 V1.5（ADMIN 产品写 403；`canMaintainCatalog` ADMIN+PROVIDER）；`WriteAuthorizationInterceptor` 仅角色级拦截、无 enterprise/scope 维；`SessionPrincipal` 无 `enterpriseId`；`CatalogBrowseService.listProducts` mine 仍 `create_by` 过滤 |
| 本角色焦点 | REQ-RBAC-002 企业 scope；ADMIN 全量 vs 我的目录；PROVIDER 隔离；mine/myCatalog 防跨企业泄漏；会话 enterprise 绑定；security 拦截器 denyModify 与写集归属 |
| 本实例 | `security-operations-wsc-011-r1` |

## 评审范围与判定

| 维度 | 结论 |
|---|---|
| RBAC-002 企业 scope 总口径（§4 / §3.1 / SNAP REQ-RBAC-002） | **通过方向** — ADMIN mine 写开放、mine=本企业、我的目录 ADMIN=本企业/PROVIDER=本人、目录维护 ADMIN=全量、全链无企业 filter 与 PO 锁定一致 |
| ADMIN 双入口（全量维护 vs 我的目录本企业） | **通过方向** — §1.2/§4/TASK-907 参数化 maintenance；908 场景 5/7 强制；须补 API 授权分裂说明（ISSUE-004） |
| PROVIDER 隔离（无目录维护 / 深链不可达） | **通过方向** — §3.1 矩阵 PROVIDER `catalogMaintenanceUI` hidden + `catalogMaintenanceApi` 403；906/908 深链负例；与「我的目录」维护 API 分裂须澄清（ISSUE-004） |
| mine/myCatalog 防跨企业泄漏 | **部分缺口** — 905 acceptance 含「跨企业写 403」「mine 本企业」；**缺**会话独占 scope 绑定（ISSUE-003）与空 create_by 负例（ISSUE-005） |
| 会话 enterprise 绑定（REQ-USER-002） | **通过方向** — TASK-904 目标/acceptance 含 principal `enterpriseId`/`enterpriseName`；须明确 scope **不得**信客户端 enterprise 参数（ISSUE-003） |
| denyModify / 写集：RbacMatrix + WriteAuthorizationInterceptor | **通过方向** — §0.3 唯一写任务 **905**；904/907 等 denyModify catalog browse/RBAC 面；控制面 `ai/runs/**` 全任务 denyModify |
| 全链无企业过滤（REQ-CAT-018） | **通过方向** — 902/905/908 场景 3 负例；L2 distribution 不按登录企业过滤 |
| 隐藏 ≠ 授权 / 禁纯 CSS | **通过方向** — §1.4/§4.1；906 Vitest 深链负例 |
| riskTags → 审查门禁 | **缺口** — 905/903 `rbac-breaking` 有、**缺** `auth-model-change`；904 `migration` 非 `schema-migration`；§6.2 未硬绑定 security/migration review（ISSUE-001/002） |
| E2E 安全证据（908） | **通过方向** — §6.1 场景 3/4/6 覆盖全链一致性、本企业 mine、PROVIDER 隔离；依赖 905 后端负例补齐 |

## 风险对照

| 风险点 | 计划措施 | 判定 |
|---|---|---|
| mine 从 create_by 切本企业导致列表泄漏 | 905 集成测 + 908 场景 4 | 方向正确；**须**会话独占 binding（ISSUE-003） |
| 客户端传 `enterpriseId` / `scope` 篡改扩大可见面 | §3.1 OpenAPI mine/scope 语义 | **未**写「忽略或拒绝客户端 enterprise 标识、仅以 SessionPrincipal 为准」 |
| ADMIN 全量维护 vs 我的目录 scope 混淆 | 907 参数化 + ADMIN 双路由 | UI 方向正确；**维护 API** interceptor 分裂未钉死（ISSUE-004） |
| PROVIDER 误开全量 `/catalog/maintenance` | 矩阵 403 + 906 深链 | 方向正确；与 myCatalog 维护 200 路径须共存设计（ISSUE-004） |
| ADMIN 跨企业产品写 | 905「跨企业 403」 | 通过方向 |
| 预落地 matrix 2.3.2 仍 V1.5（ADMIN 写 403、PROVIDER maintenance visible） | §0.2 对账 + 903/905 纠偏 | 通过方向；实现须 903→905 串行 |
| 历史无企业用户（OQ-V16-002）默认策略致同企聚合 | 904 文档化 | 非阻塞；建议 P2 补安全验收（ISSUE-006） |
| 空/缺失 create_by 被冒领 | V1.5 既有约束 | **未**写入 905 acceptance（ISSUE-005） |
| RBAC 变更无 securityReviewer | §6.2 仅 developer→reviewer→tester | **缺口**（ISSUE-001） |
| Flyway 企业表无 migrationReviewer | §6.2 第 4 条一句提及 | 标签名/硬绑定不足（ISSUE-002） |
| 控制面伪造 REV/state | §0.2/全任务 denyModify `ai/runs/**` | 通过方向 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-WSC8-R1-001 | P1 | `policies.yaml` 将 **`auth-model-change`** 列为 `securityReviewer` 触发标签。本计划 Wave B 实质变更鉴权模型：903 矩阵 ADMIN `productWriteApi` 由 403→200（本企业）、PROVIDER `catalogMaintenanceApi` 由 200→403、新增 `myCatalogUI` 与 mine/scope 企业语义；905 改 `RbacMatrix` / `WriteAuthorizationInterceptor` 及 editor/import 企业校验；904 会话 principal 增 enterprise。903 `riskTags`=`[contracts-bump, rbac-breaking, enterprise-scope]`，905=`[rbac-breaking, data-isolation, enterprise-scope]`，**均不含** `auth-model-change`；§6.2 每任务门禁未写明命中 `riskTriggers.securityReviewer` 时须独立 `securityReviewer` 且未通过不得 `VERIFIED`（对比 PLAN-WSC-5.2 §1.4/§6.2 已修补口径）。 | （1）TASK-WSC-903（建议同步 905/601 级总述）`riskTags` 增加 **`auth-model-change`**；（2）§6.2 或 §9 发布门禁写明：命中 `auth-model-change`（及 policies securityReviewer 触发标签）须独立 `securityReviewer`，对照 enterprise scope / ADMIN 写开放 / PROVIDER 维护 403 / 三角色 mine·全链矩阵；P0/P1 未关闭前不得 `VERIFIED`；（3）既有 acceptance/testScope 可保留。 | REQ-RBAC-002, REQ-USER-002, REQ-CAT-016 |
| ISSUE-SEC-WSC8-R1-002 | P1 | `policies.yaml` 的 `migrationReviewer` 触发标签为 **`schema-migration`** / `data-backfill`。TASK-WSC-904 `writeSet` 独占 `sql/migration/**`（企业表 + `sys_user` 外键），`riskTags` 含 **`migration`**（非 policies 正式名）。§6.2 第 4 条虽写「904 命中 migration → migrationReviewer」，但未像 PLAN-WSC-5.2 那样硬绑定「未通过不得 VERIFIED」，且标签名不对齐将导致 Orchestrator 机械触发失败。 | （1）904 `riskTags` 使用 **`schema-migration`**（可保留说明性别名，须含正式名）；（2）§6.2 或 904 acceptance 声明：写 `**/sql/migration/**` 须独立 `migrationReviewer`，未通过不得 `VERIFIED`；（3）OQ-V16-002 默认企业策略写入交付说明（已有方向即可）。 | REQ-USER-002 |
| ISSUE-SEC-WSC8-R1-003 | P1 | §3.1 / TASK-905 定义 `mine=true`、可选 `scope=myCatalog` 等 **query** 表达范围，但**未**强制：列表/写/维护 scope **仅**从 `SessionPrincipal.enterpriseId`（及角色规则下的 `userId`/`create_by`）推导；**禁止**客户端通过 query/body 传入 `enterpriseId`（或等价）扩大或切换企业可见面。现有 `CatalogBrowseService` 以 `ownerUserId` 参数过滤，905 若仅加 query 解析而无「拒绝/忽略客户端 enterprise 标识」约束，存在 **IDOR**（企业 A 用户伪造参数窥视企业 B mine/myCatalog 或写操作）。OpenAPI §3.1 亦未写 negative/security note。 | （1）§3.1 OpenAPI 冻结或 §1.2 假设增加硬口径：**不得**接受客户端 `enterpriseId`（或等价）作为授权依据；mine/myCatalog/写校验 **仅**信服务端会话 principal；（2）905 `acceptance` + `testScope` 至少一条集成负例：同角色携带篡改 enterprise 参数（若有暴露面）仍仅返回/允许**本会话企业**数据，跨企业 403/空集；（3）903 OpenAPI security/description 同步。 | REQ-RBAC-002, REQ-CAT-016, REQ-CAT-017, REQ-USER-002 |
| ISSUE-SEC-WSC8-R1-004 | P1 | §3.1 矩阵：PROVIDER `catalogMaintenanceApi` **403**；REQ-CAT-017 / TASK-907 要求 PROVIDER「我的目录」复用 REQ-CAT-013 **维护 UX**（含 `/catalog/maintenance/**` 类批量/关联操作）。§0.3 指定 **905** 独占 `WriteAuthorizationInterceptor`，**907** 可写 maintenance controller/service 但 **不可**改 interceptor。当前 interceptor（V1.5）对 `MAINTENANCE` 资源仅 `canMaintainCatalog(role)`（ADMIN+PROVIDER 均 200）。计划未写明：全量目录维护 vs myCatalog 如何在 **同一或分拆 API** 上实现「PROVIDER 全量 maintenance 403、myCatalog 范围 200」；905 acceptance/testScope **未**覆盖 PROVIDER myCatalog 维护 API 正例与全量 maintenance 403 负例；907 后端测依赖尚未定义的 interceptor 行为，存在实现时要么误拦 PROVIDER 我的目录、要么为复用 UX 而削弱全量 maintenance 门控的风险。 | （1）§3.1 / 903 `matrix.yaml` 明确 `myCatalogMaintenanceApi`（或等价 scope 参数 + OpenAPI 描述）与 `catalogMaintenanceApi`（ADMIN 全量）分裂语义；（2）905 `objective`/`acceptance`/`testScope` 增加：`WriteAuthorizationInterceptor`（及/或 `RbacMatrix`）scope-aware 规则——PROVIDER 对**无 myCatalog scope** 的 maintenance 请求 403，对 myCatalog 范围 200（service 层再过滤 create_by）；ADMIN 全量 maintenance 200、myCatalog 本企业；（3）若采用分拆路径，须在 OpenAPI 写清 URI 与矩阵一行对应，且 905 在 907 之前 VERIFIED。 | REQ-RBAC-002, REQ-CAT-017, REQ-SHELL-009 |
| ISSUE-SEC-WSC8-R1-005 | P1 | PLAN-WSC-5.2 / SNAP-V1.5 已确立：`create_by` 空/缺失/异主不可被 PROVIDER 冒领（403）。本计划 905 `acceptance` 写「PROVIDER 不可维护他人 create_by / 他人企业产品」与 ADMIN 跨企业写 403，但**未**显式要求 **空/缺失 `create_by`**（或迁移后 enterprise 未绑定产品）不可被任意 PROVIDER/ADMIN（跨企业）编辑/导入/维护冒领；908 场景 6 仅测 PROVIDER 本人 scope，未钉空归属。企业 scope 切换后空归属产品更易成为跨角色冒领面。 | 905 `acceptance` 增加：空/缺失/非当前授权范围 `create_by` 的产品，PROVIDER 与 ADMIN（myCatalog/mine 路径）编辑/导入/维护 → **403**（或契约稳定拒绝码）；`testScope` 至少一条后端集成负例。策略细节可引用 V1.5 口径。 | REQ-RBAC-002, REQ-CAT-016, REQ-CAT-017 |
| ISSUE-SEC-WSC8-R1-006 | P2 | OQ-V16-002：历史无企业归属用户默认企业策略仅 904 交付说明，无安全 acceptance。若默认策略将大量 orphan 用户并入同一企业，可能导致 mine/myCatalog **非预期同企可见**（逻辑泄漏，非鉴权绕过）。 | 904 `acceptance` 或交付说明增加可审计口径：默认 enterprise 分配策略；至少一条测试或审查清单：orphan 用户不得因默认策略获得**其他企业已有产品**的 mine/myCatalog 写权限；若采用「单默认企业」，须注明仅适用于演示种子且生产须人工补录。 | REQ-USER-002 |

## 无异议项（记录）

- §0.3 **905 独占** `RbacMatrix` / `WriteAuthorizationInterceptor` / browse scope 面，904/907 等 **denyModify** _catalog RBAC 控制器与服务边界 — 写集隔离方向正确，可降低并行任务误改拦截器风险。
- 全任务 **denyModify** `ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`；文首禁止伪造 REV/APPROVE — 控制面约束可接受。
- §4 + §1.4「隐藏 ≠ 授权」+ 906/908 深链负例 — PROVIDER 目录维护 UI/API 隔离意图可测。
- REQ-CAT-018 全链无企业 filter：902/905/908 场景 3 方向正确。
- TASK-904 会话 principal 含 enterprise 供后续 scope — USER-002 主路径可接受（在 ISSUE-003 关闭后仍须 securityReviewer 实证）。
- 座序图无企业筛、浏览无 `industryCategory` — 非本版鉴权主风险；902/908 负例已覆盖。
- 903 独占契约 / 903→904→905 串行 — 降低 matrix 与实现分叉（§8 风险表已述）。
- 本评审**不**等同批准 SNAP；**不**代批 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor / apiDataDesigner / uxUiPlanner / maintainer。
- 高风险接受未伪造；不代替人类 `securityOperationsOwner` 签署残余风险。

## 升级路径

- ISSUE-SEC-WSC8-R1-001..005 若被拒绝修补 → 升级 `securityOperationsOwner`（`policies.escalation.onSecurityResidualRisk`）；**不**因此否决 SNAP 业务目标本身。
- 若修补后仍拟在无 `securityReviewer` 实证下合入 enterprise RBAC 变更 → 本角色下一轮可 `BLOCK` 并升级 Owner（本轮尚未构成 BLOCK）。

## decision

**REQUEST_CHANGES** — 待 ISSUE-SEC-WSC8-R1-001（P1）、ISSUE-SEC-WSC8-R1-002（P1）、ISSUE-SEC-WSC8-R1-003（P1）、ISSUE-SEC-WSC8-R1-004（P1）、ISSUE-SEC-WSC8-R1-005（P1）按 `closeWhen` 写入计划后，本角色可在后续 round 重评。ISSUE-SEC-WSC8-R1-006（P2）建议同轮修补，不单独构成 BLOCK。禁止代批其他角色。
