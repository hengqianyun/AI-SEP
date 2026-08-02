# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC4-R1-solutionArchitect
planId: PLAN-WSC-4.0
round: 1
role: solutionArchitect
actorInstance: solution-architect-wsc-004-r1
snapshotIdAtReview: SNAP-WSC-004
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-004 与仓库现状，评估 PLAN-WSC-4.0 的系统边界：契约升版
  wsc-contracts@2.1.0 唯一所有权（TASK-WSC-301）、导入四态/请求级
  ERR_IMPORT_TEMPLATE_UNSUPPORTED、旧 endpoint 读兼容、DAG（301→302∥303→304）
  与前端 feature 写集互斥总体可调度；DEC-WSC-004/005 与 UX 令牌不重开对齐。
  阻断项：OpenAPI→endpoints[] 在「批量导入解析」（301/backend）与「编辑页 Swagger
  回填」（302/frontend）双路径并存，计划未冻结派生所有权、失败策略一致性及
  swaggerFileContent 与 endpoints 冲突时的真源规则，易造成同原文两端结果分叉
  与验收不可判定。另就 302∥303 OpenAPI UI 共享组件写路径作 P2 记录。
  本轮 REQUEST_CHANGES；不代写他角色结论。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 契约 `wsc-contracts@2.1.0` 所有权与升版边界 | 通过 — §3.1/§3.5：`contracts/**` + `frontend/src/api/**` 唯一写=301；302..304 deny；VERSION 2.0.0→2.1.0 与 SNAP `contractsTarget` 一致；禁止后继私改 |
| `typeSpecific.api` 存贮形状 / 旧 `endpoint` 兼容 | 条件通过 — §3.3 冻结 SNAP 建议形状（swaggerFileContent + endpoints[] + 扩展字段）；读旧/写新策略与 SNAP OQ-V13-002/007 对齐。派生边界见 ISSUE-SA-R1-001 |
| 导入解析边界（v0729 / 旧模板 / 行级） | 条件通过 — §3.2 切开 FORMAT vs TEMPLATE_UNSUPPORTED；列映射/模板下载/后端解析归 301；304 只消费 UI/E2E。OpenAPI 单元格解析与编辑 Swagger 回填的同源规则未写清 → ISSUE-SA-R1-001 |
| 模块写集 / LAYOUT / DEC | 通过 — 301 独占 import+editor（+条件 detail）后端；deny browse/admin/maintenance/chain 实现；DI 调用既有写入/存证端口与现网 `ProductImportService`→`ProductEditorService` 一致；sql 仅 §3.6 证明必需；禁 `db/migration` |
| DAG / 并行写集 | 通过 — W1=301 → W2=302∥303 → W3=304 无环；editor∥detail 无交集；304 串行依赖齐全 |
| UX / 令牌不回退 | 通过（架构路径）— styles/shell/router 全任务只读；302..304 scoped 增量；不构成本角色视觉批准 |

## 架构抽查（证据）

### 契约 2.1.0

- 仓库现状：`contracts/VERSION`=`2.0.0`；`UpdateFrequency` 无 `NO_UPDATE`；`importSemantics.requestLevel` 仅含 FILE_TOO_LARGE / FORMAT_INVALID；`TypeSpecificFields.api` 为宽松 `additionalProperties`；`ProductType` 文案仍写 OTHER 无专属字段——与 SNAP OQ-004 修订及本计划 §3.1 增量方向一致，须由 301 一次性冻结，计划所有权矩阵可执行。
- 计划将 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 纳入 requestLevel，并与态 ④ UI 绑定，不削弱既有行级/报告白名单语义——边界清晰。

### `typeSpecific.api`

- §3.3 YAML 与 SNAP「建议形状」字段对齐（含 parameters/responses/optional schemas、兼容 `endpoint`）。
- 写路径：301 改 `ProductEditorService`（现网对 OTHER 拒绝 typeSpecific、API 段透传 Map）以容纳新键与 `typeSpecific.other`——writeSet 含 `catalog/editor/**`，合法。
- 读路径：`GET /catalog/products/{id}` 现由 **browse** 提供（`catalog/detail` 仅 package-info 声明辅助）；301 denyModify browse。只要详情/编辑兼容策略为「原文透传 + 前端容忍空 endpoints」，则无需改 browse；计划 §3.3「UI 不报错」支持该模型。**观察**（非独立 ISSUE）：勿在 301 叙述中假设于 `detail/**` 改 GET 组装——真实控制器在 browse。

### 导入解析边界

- 现网：`ImportTemplateColumns` 为 V1.1 最小列集；导入经 mapper→`ProductEditorService.create`；表头不匹配尚无独立 TEMPLATE_UNSUPPORTED 码——计划 §3.2/301 acceptance 补齐，边界正确。
- v0729 多行表头（分组/列名/说明）与类型分区列映射归 301 `catalog/import/**`；304 deny backend——**解析 sole authority = 301**，UI 不得另实现列语义。
- 行业分类：OQ-V13-006 名称/路径匹配、失败行级；301 可在 import 包增强 mapper，不必写 browse。权威模板样例「建筑业」与当前种子分类（医疗卫生/金融服务…）可能不匹配——计划风险表已提示；E2E 可用种子对齐 fixture，不单独升格 ISSUE（验收须避免把「官方样例行必绿」写成未声明前提）。

### 写集与并行

| 任务 | 写集核心 | 核对 |
|---|---|---|
| 301 | contracts + api client + import + editor（+条件 detail）+ 条件 sql/migration + fixtures | 与「契约/后端底座」单一作者一致 |
| 302 ∥ 303 | `catalog/editor/**` ∥ `catalog/detail/**` | 路径无交集 |
| 304 | `catalog/import/**` + e2e/fixtures | dependsOn 301+302+303；不得改 editor/detail/contracts |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | SNAP-WSC-004 / PLAN §3.3 与 REQ-API-001 要求：（1）导入「接口定义」→ 存 `swaggerFileContent` 并解析 `endpoints[]`，非空失败→行失败（301）；（2）编辑页 Swagger 粘贴/上传→回填 `endpoints[]` 且保留原文（302）。计划未冻结 **OpenAPI→endpoints 派生所有权**与冲突规则：现网无共享 parse 端口；301 writeSet 亦未要求契约化 parse API；302 若纯前端解析则与导入后端解析易对同一原文得到不同 endpoints。§3.3 仅写「写以 swaggerFileContent+endpoints[] 为准」，未定义二者不一致时服务端真源/校验策略，导致 CR/acceptance 无法判定「同源」或「允许分叉」。 | 在计划 §3（建议 §3.3 或新增小节）**明文三选一**并落到 301/302 acceptance（可单文件 scope/CR）：**(A) 同源服务端派生**：301 提供契约化 parse 辅助（或导入与产品写共用的后端派生组件 + 可选只读 API）；302 回填必须调用该能力；同一 fixture 导入路径与编辑回填的 endpoints 关键字段（method/path/summary）一致。**(B) 双端解析但规则对称**：允许 302 前端解析，但须写明原文为归档真源、endpoints 为派生存贮；两端失败策略对称（非空失败：导入=行失败；编辑=阻断提交或等价不可保存）；并给出至少一份共享 fixture 的「关键字段一致」验收，或显式列出允许差异清单。**(C) 编辑不自动解析**：302 仅保存原文 + 手工维护 endpoints（须同步修订 SNAP/REQ-API-001 的 Swagger 回填口径，超出本候选默认可关闭范围则升 PO）。另须补一句：**当 swaggerFileContent 与 endpoints[] 均非空且不一致时**的服务端策略（以 endpoints 为准 / 拒写 / 重算其一）。 | REQ-API-001, REQ-CAT-005, REQ-CAT-008, OQ-V13-002, OQ-V13-009 |
| ISSUE-SA-R1-002 | P2 | W2 允许 302∥303，二者均需 OpenAPI 端点列表（编辑可写 / 详情只读）。`RULE-PROJECT-LAYOUT` 规定跨 feature 共享组件路径为 `frontend/src/components/**` 且须串行；本计划无任务 writeSet 纳入该路径，303 `readSet` 亦不含 editor。若实现期抽取共享控件→无合法写路径或被迫 SCOPE_VIOLATION；若各自实现→双份 UI/行为漂移风险（架构可容忍但应事先声明）。 | 计划增加可检声明之一：**(a)** 明确允许 302/303 **各自实现** OpenAPI 展示/编辑，**禁止**本 Run 写 `frontend/src/components/**`；**(b)** 指定唯一任务（及波次）拥有共享 OpenAPI 组件路径，另一任务只读消费并调整 dependsOn/并行；**(c)** 禁止 302∥303，改为串行以复用先合入方组件（须改 §6）。 | REQ-API-001, REQ-CAT-004, REQ-CAT-005 |

## 无异议项（记录）

- `contractsTarget` / 旧模板文件级拒绝 / 同步导入不引入 Job / UX 令牌不重开：与 SNAP 关闭的 OQ-V13-* 及基线 PLAN-WSC-2.2/3.1 一致。
- RBAC 矩阵与「前端隐藏≠授权」继承正确；本角色不代替 Security 做威胁建模批准。
- 枚举名 `NO_UPDATE`、OpenAPI schema 细粒度字段必填性等契约微观冻结，归 apiDataDesigner；只要 301 独占升版且 §3.1 业务形状不回退，不构成本角色额外 P0/P1。
- 不代批 productAnalyst / QA / parallelPlanner 等其他委员会角色。

## 决策

`REQUEST_CHANGES` — 解决方案架构维度未关闭 ISSUE：**2**（P1×1，P2×1）。

在 ISSUE-SA-R1-001 关闭前，不建议按本候选稿派发开发波次（派生边界会直接冲击 301/302 验收与数据一致性）。ISSUE-SA-R1-002 可与修订稿一并闭合，不单独构成 BLOCK。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-004-r1`；**不**代写其他委员会角色结论，**不**伪造他角色 `APPROVE`。
