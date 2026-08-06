# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC5-R1-solutionArchitect
planId: PLAN-WSC-5.1
round: 1
role: solutionArchitect
actorInstance: solution-architect-wsc-008-r1
snapshotIdAtReview: SNAP-WSC-005
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-005（status=IN_REVIEW）与 PLAN-WSC-5.1：任务边界总体按
  契约→用户/会话→我的产品/RBAC→座序图切分，601 独占 contracts@2.2.0 +
  frontend/src/api/**，串行 DAG 601→602→603→604 无环且与 repos.yaml /
  RULE-PROJECT-LAYOUT 按类型分层、预落地对账（SNAP 为验收权威）架构可接受。
  阻断调度的是多处「同路径双任务写 + 散文级方法/片段例外」：603∥604 共享
  catalog/browse 控制器与服务；602∥603 共享 RbacMatrix /
  WriteAuthorizationInterceptor；602 writeSet 的 model/** 过宽可改目录模型。
  另记录壳层 WorkbenchLayout/routes 串行双写为 P2。本轮 REQUEST_CHANGES；
  不代写他角色结论；不以预落地代码代替共识。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务边界 vs SNAP 六条 P0 | 条件通过 — §7 映射完整；实现归属 601 契约 / 602 用户 / 603 我的产品+去写 / 604 座序图合理。共享路径例外见 ISSUE |
| 写集冲突 / 互斥 | **不通过** — 虽禁并行，但 602↔603、603↔604 对同一字面 glob 双写，依赖不可检的方法/片段散文；见 ISSUE-SA-R1-001/002/003/004 |
| 分层约束（DEC-WSC-005 / LAYOUT / CLAUDE.md） | 通过 — §0.2/§1.3 禁止新建域顶层包；写集落在 `controller|service|repository|entity|model|config|common`；预落地残留须在任务写集内收敛 |
| 预落地对账架构可接受性 | 通过 — §0.2 以 SNAP+acceptance 为权威、代码须 diff 对账；控制面 deny `ai/runs/**`；不把预落地当共识 |
| DAG 601→604 | 通过 — 无环；契约先于实现；会话先于 create_by 隔离；browse `mine`/去写先于座序图挂载；无同波并行对 |
| 契约 / 实现边界 | 通过 — §3.2：601 唯一写 `contracts/**` + `frontend/src/api/**`；602..604 deny；禁止静默再 bump |
| UX / 令牌路径（架构） | 通过 — styles/theme/components 全任务 deny；不构成本角色视觉批准 |
| SNAP 门禁 | 记录 — `IN_REVIEW`；派发须正式批准或委员会确认范围（文首）；非本角色伪造批准 |

## 架构抽查（证据）

### 契约与实现边界

- `contractsTarget: wsc-contracts@2.2.0` 与 SNAP 一致；§3.1 冻结 RBAC / mine / l2-distribution / admin users / 角色切换禁用；601 acceptance 含 VERSION、matrix、codes 硬同步与 client typecheck。
- 后继任务只读消费契约与生成 client；业务任务私改 contracts/api 被 deny — 边界可调度。

### DAG 与波次

```text
W1 601 → W2 602 → W3 603 → W4 604
```

- 依赖理由成立：真登录/会话（602）后方可稳定验收 create_by 与 PROVIDER 写（603）；603 改公共目录去写与 `mode=mine` 后，604 挂座序图才不与写入口/挂载点互相覆盖。
- §6 声明禁止任意两两并行 — 避免 browse/layout/RBAC 路径交集在同波冲突。**串行消除的是并行竞态，并不消除「同路径双作者 + 不可检例外」的 scope/CR 问题**（见 ISSUE）。

### 预落地对账

- §0.2 按波次列出迹象与对账要求；验收明确「不以代码已在代替测试证据」— 架构上可接受。
- 包结构：要求符合按类型分层；禁止新建域顶层包 — 与 `RULE-PROJECT-LAYOUT` / `backend/CLAUDE.md` 一致。预落地若已合入同文件多能力（如 browse 同时含 `mine` 与 `l2-distribution`），更凸显 ISSUE-SA-R1-001 的拆分/独占必要。

### 写集交叉（字面）

| 共享路径 | 任务 | 计划声称的例外 | 架构判定 |
|---|---|---|---|
| `controller/catalog/browse/**`、`service/catalog/browse/**` | 603 + 604 | 603 可改同文件但不得改 L2 算法；L2 归 604 | **不可字面 scope-check** → ISSUE-SA-R1-001 |
| `common/security/RbacMatrix.java`、`WriteAuthorizationInterceptor.java` | 602（散文允许写「用户管理相关部分」）+ 603（产品写） | 「产品写最终以 603 为准」 | **同文件双作者** → ISSUE-SA-R1-002 |
| `model/**` | 602 整树 | 注释意图仅用户/会话 DTO | **glob 过宽** → ISSUE-SA-R1-003 |
| `WorkbenchLayout.vue`、`router/routes.ts` | 602 + 603 | 串行；603「仅改 my-products 片段」 | 可容忍串行但片段不可检 → ISSUE-SA-R1-004（P2） |

### repos / 多仓

- 写集落在 `frontend/`、`backend/`、控制面 `contracts/`；state 仅 Orchestrator — 与 `repos.yaml` isolation 一致。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | PLAN §5 TASK-WSC-603/604：二者 `writeSet` 均含 `backend/.../controller/catalog/browse/**` 与 `service/catalog/browse/**`。603 `denyModify` 用散文规定「可改同一 Controller 挂 mine，但不得改 L2 聚合；冲突则留给 604」。Orchestrator 字面 scope-check 与 CR **无法**区分「合法改 mine」与「越权改 L2」。预落地 `CatalogBrowseController` 已同文件并存 `l2-distribution` 与 `mine`，对账期必然整文件触碰，双波次互相覆盖风险高，验收归属不可判定。 | 在计划 §3 或 §5 **明文三选一**并落到 603/604 `writeSet`/`denyModify`/`acceptance`（可单文件 scope/CR）：**(A) 拆分类文件**：L2 独立 Controller/Service（或 browse 下独立源文件）由 **604 独占**；603 对这些路径 `denyModify`，且不得改其方法体。**(B) 后端并入 603**：`GET /catalog/l2-distribution` 实现与 acceptance（真实 `totalProducts`、items 降序）并入 603；604 **denyModify** 全部 backend browse，仅写 `CirculationSeatMap` + 公共目录挂载。**(C) 单任务独占 browse 后端**：指定唯一任务拥有 `controller/catalog/browse/**` + `service/catalog/browse/**` 全文，另一任务对该 glob `denyModify`；并同步调整 dependsOn/DAG 叙述使 L2 与 mine 验收仍可检。 | REQ-CAT-009, REQ-CAT-010, REQ-CAT-001 |
| ISSUE-SA-R1-002 | P1 | PLAN §5 TASK-WSC-602 `writeSet` 含 `common/security/**`，并散文允许「对账后写矩阵中与用户管理/登录相关部分」；603 `writeSet` 显式含 `RbacMatrix.java`、`WriteAuthorizationInterceptor.java`（产品写/导入仅 PROVIDER）。同一源文件双任务可写，且「相关部分」不可字面校验；与 §1.4「writeSet 字面路径 scope-check」冲突，RBAC 实现真源在 602/603 间漂移。 | 在计划中 **明文三选一**并改写 602/603 writeSet/denyModify：**(A)** 602 **denyModify** `RbacMatrix.java` 与 `WriteAuthorizationInterceptor.java`；602 仅写 PasswordHasher / AuthAuditLogger / 会话相关 security 文件；603 独占矩阵与拦截器（含 userManage 与产品写整文件对账）。**(B)** 602 独占上述两文件全文，且 602 `acceptance`/`testScope` **必须**含 §4 产品写仅 PROVIDER / ADMIN 产品写 403 矩阵行；603 对这两文件 `denyModify`。**(C)** 拆分类型：用户管理权限与产品写权限分属不同类文件，writeSet **字面列出**各自路径，禁止整包 `common/security/**` 模糊授权。 | REQ-RBAC-001, REQ-USER-001, REQ-SHELL-001 |
| ISSUE-SA-R1-003 | P1 | PLAN §5 TASK-WSC-602 `writeSet` 为 `.../model/**`（注释意图：LoginRequest / SessionPrincipal / Role / 用户 DTO）。字面 glob 覆盖全部 model（含目录/上链/总览 DTO）。对账或「相关 DTO」名义下可合法改非用户模型，破坏 603/604 与既有基线边界；与 LAYOUT「跨域 model 变更须串行或独立任务」及本计划「602 不得借机改目录」叙述不一致。 | 将 602 `model/**` **改为显式文件列表**（或等价窄 glob，如仅会话/用户管理 DTO 文件名），并在 602 `denyModify` 中排除目录/产品/总览等非用户模型（或写明禁止修改的 model 路径清单）。603/604 若确需新增/修改响应 DTO，须在各自 `writeSet` **字面列出**对应 model 文件。 | REQ-USER-001, REQ-CAT-010, REQ-CAT-009 |
| ISSUE-SA-R1-004 | P2 | PLAN §5：602 与 603 均写 `frontend/src/layouts/WorkbenchLayout.vue` 与 `frontend/src/router/routes.ts`；603 仅称「与 602 冲突时仅改 my-products 相关片段，串行已保证」。串行避免并行竞态，但路径级 scope-check 仍允许后继任务整文件重写壳层/路由，片段边界不可检。 | 增加可检声明之一：**(a)** 为两文件分别指定 **最终独占波次**（例：导航壳层键/路由表由 603 收口，602 合入后 603 不得删除用户管理路由；或相反），并在后继任务 acceptance 列「不得回退」清单；**(b)** 拆出可独占模块（如 `navItems.ts` / `adminRoutes.ts` vs `myProductRoutes.ts`），writeSet 按文件互斥；**(c)** 保持双写但增加 CR 必检 diff 约定（允许改动的 route path / menu key 白名单写入计划正文）。 | REQ-SHELL-001, REQ-CAT-010, REQ-USER-001 |

## 无异议项（记录）

- 601 独占契约升版与 api 生成；禁止业务任务静默 bump — 契约/实现边界清晰。
- Out of Scope（Downloads React、座序图点击筛选、OAuth、多租户、异步导入 Job）与 SNAP 一致；不构成本角色范围扩张。
- `create_by` 空归属在 §8 风险表有缓解叙述；建议实现期写入 603 交付说明（观察，不单列 ISSUE；若下轮仍无 acceptance 钩子可由 QA 升格）。
- 不代替 securityOperations 做威胁建模/密钥批准；不代替 qaStrategist 冻结 E2E 证据包细节；不代替 productAnalyst 裁定 SNAP 文本。
- 不以工作树预落地通过/失败代替本评审；验收权威为 SNAP-WSC-005 + 计划 acceptance。

## 决策

`REQUEST_CHANGES` — 解决方案架构维度未关闭 ISSUE：**4**（P1×3，P2×1）。

在 ISSUE-SA-R1-001 / 002 / 003 关闭前，不建议按本候选稿字面 writeSet 派发 602+ 开发波次（同路径双作者会使 scope-check 与对账验收不可判定）。ISSUE-SA-R1-004 可与修订稿一并闭合，不单独构成 BLOCK。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-008-r1`；**不**代写其他委员会角色结论，**不**伪造他角色 `APPROVE`。
