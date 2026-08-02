# Round 2 Review — Product Analyst

```yaml
reviewId: REV-PLAN-WSC4-R2-productAnalyst
planId: PLAN-WSC-4.1
round: 2
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-004
actorInstance: product-analyst-wsc-004-r2
decision: APPROVE
summary: |
  对照 SNAP-WSC-004（APPROVED）与本角色 R1（PLAN-WSC-4.0 / APPROVE、无 ISSUE）：
  PLAN-WSC-4.1 仍完整覆盖 REQ-CAT-008/004/005、REQ-API-001、REQ-RBAC-001；
  In/Out Scope、v0729 列映射、旧模板文件级拒绝、OpenAPI 编辑/详情/导入三面、
  已关闭 OQ-V13 口径与 UX/功能基线不回退均未削弱。他角色 ISSUE 吸收未造成产品范围回退。
  本轮无新产品异议；不代批他角色结论，亦不代关他角色 ISSUE。
```

## 评审范围（产品分析）

- 权威快照：`product/requirements/SNAP-WSC-004.md`（`snapshotId: SNAP-WSC-004`，`status: APPROVED`）。
- 候选计划：`planning/proposals/PLAN-WSC-4.1.md`（`DRAFT` / `CANDIDATE`，`basedOn`/`lineageFrom: PLAN-WSC-4.0`）。
- 本角色 R1：`planning/reviews/wsc-4.0/round-1/REV-PLAN-WSC4-R1-product-analyst.md`（`decision: APPROVE`，异议表空）。
- 聚焦：4.1 相对 4.0 / SNAP-WSC-004 是否出现 **范围回退**（REQ 覆盖缺口、列映射/旧模板/OpenAPI 三面/OQ 闭合口径削弱、Out of Scope 红线松动或静默扩 scope）。
- **不**裁定写集/DAG/双端解析工程细节/契约字段命名；**不**代替 architect / apiDataDesigner / qaStrategist / security / uxUiPlanner / parallelPlanner / planEditor 等角色批准或关闭其 ISSUE。

## closeWhen 确认（本角色 R1）

| ISSUE id | 结论 | 说明 |
|---|---|---|
| （无） | **N/A** | R1 对 PLAN-WSC-4.0 为 APPROVE，未开 ISSUE；无待确认 closeWhen。 |

## 覆盖核对摘要（相对 SNAP-WSC-004）

| 项 | 结论 |
|---|---|
| In Scope = 修订/新增/继承 5 条 P0 REQ | **仍对齐**：§1.1 + 任务 301..304 + §7 映射齐全；`requirementCount: 5` |
| Out of Scope | **与 SNAP 一致**：不重做 UX 令牌；不引入异步 Job；不扩数据登记/交易/连接器等；不改写历史 SNAP；另增「本 Run 不抽 `frontend/src/components/**`」属实现边界，**未**删减业务面 |
| 功能/UX 基线 | **未静默扩大或削弱**：`functionalBaseline: SNAP-WSC-002` / PLAN-WSC-2.2；`uxBaseline: SNAP-WSC-003` / PLAN-WSC-3.1；契约目标 `wsc-contracts@2.1.0` |
| 列映射（v0729） | **未回退**：权威 `templateAsset`；§3.1 `ImportTemplateColumns` 按列名行重冻并废止 V1.1 最小列 example；301 acceptance/testScope 绑定列组、四类型 round-trip、`NO_UPDATE`、无编码列自动生成 |
| 旧模板拒绝 | **未回退且更硬**：文件级拒绝 + `ERR_IMPORT_TEMPLATE_UNSUPPORTED`；与 `FORMAT_INVALID` 分界；零写入；UI 态 ④；E2E 场景 3 |
| OpenAPI 三面 | **齐全**：编辑 302、详情 303、导入（301 解析 + 304 UI/E2E）；§1.5 交互意图对齐 SNAP；§3.3 方案 B 仍为 swagger 原文 + endpoints |
| OQ-V13-001..009 / OQ-V11-003 | **口径未回退**（见下表）；Blocking OQ 无 |
| 保留 OQ-001 / OQ-V11-004；修订 OQ-004 | **未重开冲突**：OQ-001 Out of Scope；部分成功不改变且 304 **强制** partial-success；OTHER `contentDescription` 按 SNAP 修订收录（§3.1 废止「OTHER 必须空对象」） |

### REQ → 任务覆盖（复核）

| REQ | priority | change | 主责任务（4.1） | 相对 R1 / SNAP |
|---|---|---|---|---|
| REQ-CAT-008 | P0 | 修订 | **301**（解析/模板/错误码）+ **304**（UI/E2E） | v0729 对齐；旧模板文件级拒绝；四态/报告白名单/同步完成态/上链不回退；无编码列自动生成（DEC-WSC-001）；fixture 表含 all-fail / legacy-template / format-invalid |
| REQ-CAT-004 | P0 | 修订 | **301**（DTO/后端）+ **303**（详情 UI） | 既有分组保留；按类型只读扩展描述；API 只读 endpoints+文档；旧 `endpoint` 可读；有权显示编辑入口；303 自研于 detail feature，**不**削弱只读面 |
| REQ-CAT-005 | P0 | 修订 | **301**（持久化）+ **302**（编辑 UI） | 类型切换动态区；写 swagger+endpoints；冲突规则 §3.3；非空解析失败阻断提交；UX 不回退 REQ-UX-007 |
| REQ-API-001 | P0 | 新增 | **301**+**302**+**303**+**304** | 侧栏端点增删；参数/响应/可选 schema；Swagger 回填保留原文；详情只读等价；导入「接口定义」非空解析失败→行失败；共享 fixture 关键字段一致 |
| REQ-RBAC-001 | P0 | 继承 | **301..304** | §4 三角色矩阵；写仅 ADMIN+PROVIDER；USER 403；前端隐藏≠授权；E2E 场景 6 |

### 列映射未回退（相对 SNAP §列 → 字段映射表）

| SNAP 映射要点 | 计划吸收位置（4.1） | 结论 |
|---|---|---|
| 分组 A–E / F–H / I–K / L–O / P 与权威 xlsx | yaml `templateAsset`；§3.1 模板字节 + `ImportTemplateColumns` 列名行集合 | 绑定权威资产；列名行权威集合显式对齐 SNAP，**未另立缩减列集** |
| 类型 A：接口定义→swagger+endpoints；字段描述/样例/时间地域 | §3.1 / §3.3；301 objective/acceptance | 形状与 SNAP 建议一致；方案 B 归档真源=原文 |
| 类型 B：规模/形态/字段描述/样例 | §3.1 `typeSpecific.dataset`；301 round-trip | 未删减 |
| 类型 C：内容描述 → report/other；OTHER 允许 contentDescription | §1.2 OQ-004；§3.1 report/other + ISSUE-API 描述修订 | 与 SNAP 修订口径一致；相对「OTHER 空对象」旧叙述为**纠正对齐**，非回退 |
| 无产品编码列 → 服务端生成 | §1.2 OQ-V13-005；§3.1；301 acceptance | CLOSED 口径保留 |
| 更新频率含「不更新」 | §1.2；§3.1 `NO_UPDATE`；301 testScope | 未回退 |
| 行业分类名称/路径匹配；失败行失败 | §1.2 OQ-V13-006 | 未回退 |
| 异型非空列建议行失败 | 301 acceptance「与 SNAP 建议一致」 | 未放宽为默认可吞 |

### 旧模板拒绝

| 检查项 | 计划位置 | 结论 |
|---|---|---|
| 文件级拒绝（非整单行级失败） | §3.2；§3.4 态 ④ | 对齐 OQ-V13-001 CLOSED |
| 明确错误码 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` | §3.1 硬同步四处；§3.2；301/304；发布门禁 | 未回退；相对 4.0 更强制可检 |
| 与 FORMAT 区分 | §3.2；301 `legacy-template` vs `format-invalid` fixture | 不得互换 |
| UI/E2E 可区分 | 304 + §6.1 场景 3 | 可验收 |

### OpenAPI 三面（editor / detail / import）

| 面 | 任务 | SNAP / REQ-API-001 要点 | 结论 |
|---|---|---|---|
| 编辑 | 302 | 侧栏列表+增删；端点详情；Swagger 导入回填+保留原文；提交 2.1.0 形状 | **覆盖**；各自实现 UI 不删减编辑面能力 |
| 详情 | 303 | 只读 endpoints/文档信息；扩展描述；旧 endpoint 兼容 | **覆盖**；信息层级须对齐 §1.5（产品可验收） |
| 导入 | 301+304 | 「接口定义」解析写入原文+endpoints；非空失败→行失败；UI/E2E | **覆盖**；导入始终从单元格派生 |

### OQ 关闭口径吸收（4.1）

| OQ | SNAP 闭合结论 | 4.1 吸收位置 | 相对 R1 |
|---|---|---|---|
| OQ-V13-001 | 文件级拒绝 + 明确错误码 | §1.2；§3.2；301/304 | 未回退；硬同步增强 |
| OQ-V13-002 | swagger 文本 + endpoints[] | §1.2；§3.3 方案 B | 未回退；派生/冲突规则更可检 |
| OQ-V13-003 | `wsc-contracts@2.1.0` | yaml；§3.1；301 | 未回退 |
| OQ-V13-004 | 不引入异步 Job | Out of Scope；301 同步完成态 | 未回退 |
| OQ-V13-005 | 无编码列自动生成 | §1.2；§3.1；301 | 未回退 |
| OQ-V13-006 | 名称/路径匹配；失败行失败 | §1.2 | 未回退 |
| OQ-V13-007 | 读兼容、写优先新结构 | §1.2；§3.3；302/303 | 未回退 |
| OQ-V13-008 | 扩枚举「不更新」 | §1.2；§3.1；301 testScope | 未回退 |
| OQ-V13-009 | 接口定义解析失败→行失败 | §1.2；301；编辑侧对称阻断提交 | 未回退；对称强化 |
| OQ-004（修订） | OTHER 允许 contentDescription | §1.2；§3.1 描述修订 | 未回退 |
| OQ-V11-003 | 暂定不拆实体 | §1.2；§2.2 | 未回退 |
| OQ-001 / OQ-V11-004 | 保留 | Out of Scope / 部分成功不改变 + E2E 强制 partial | 未回退 |

## 4.1 产品向修订核验（相对 R1 APPROVE / 无范围回退）

> §0 映射他角色 ISSUE 吸收意图；关闭权归原提出者。下表仅核验：**是否削弱 SNAP 产品范围**。

| 修订点（相对 PLAN-WSC-4.0） | 产品结论 |
|---|---|
| ISSUE-SA-R1-001 → §3.3 方案 B（双端解析；冲突以客户端 endpoints 为准；导入始终派生） | **无回退**：仍满足 OQ-V13-002/009 与 REQ-API-001；可检性增强，未删减编辑/导入/详情面 |
| ISSUE-SA-R1-002 → 302/303 各自实现 OpenAPI UI；禁止本 Run 写 `components/**` | **无回退**：三面业务覆盖仍在；共享组件禁止属实现边界，非砍掉详情/编辑 OpenAPI |
| ISSUE-QA fixture / E2E 钉死（all-fail、legacy vs format、四类型 round-trip、强制 partial、evidenceId） | **增强**可验收性；未缩减 SNAP 成功标准 |
| ISSUE-API `ImportTemplateColumns` 重冻 + TEMPLATE_UNSUPPORTED 硬同步 + OTHER 描述 | **对齐并加固** SNAP 列映射 / 旧模板 / OQ-004；废止 V1.1 最小列 example **防止**导入列语义回退到旧模板 |

## 功能基线与范围（产品红线）

- 计划明确 V1.3 增量（契约 2.1.0 + 后端 typeSpecific/导入 + 三面前端），不改写 SNAP-001/002/003 正文——与 SNAP 权威声明一致。
- 导入四态、报告白名单、RBAC、上链、同步完成态均声明不回退——满足 SNAP 成功标准。
- UX：增量控件消费既有令牌、不强制 Element Plus、主路径质感不回退 3.1——对齐 `inheritedUxNoRegression`。
- 候选声明与 §10 明确对本 planId 的独立评审尚未伪造全员 APPROVE——本角色亦不代写他角色同意。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | 对照 SNAP-WSC-004 与 R1 APPROVE：4.1 未破坏 REQ 覆盖、列映射、旧模板拒绝、OpenAPI 三面或 OQ 口径；**确认无范围回退**；无新产品异议。 | — | — |

## 非异议说明（避免重复开单 / 不代他角色）

- **本角色 R1 无 ISSUE**：无需 closeWhen 关闭动作；本文件为对 PLAN-WSC-4.1 的独立 Round 2 产品评审。
- **他角色 ISSUE（SA/QA/API 等）**：§0 映射仅记录吸收意图；关闭权归原提出者，本角色不代关、不代批。
- **列映射未全文内联 A–P 表**：同 R1——计划以 SNAP + 权威 xlsx + 重冻 `ImportTemplateColumns` 为映射权威；产品侧视为未回退，不单开 ISSUE。
- **≤10MB**：SNAP CAT-008 明示；计划经 `ERR_IMPORT_FILE_TOO_LARGE` / oversize fixture 与基线继承覆盖，不单开。
- **OQ-004 表内「修订待确认」措辞**：SNAP yaml/`poSnapshotConfirm` 与计划 §1.2/§3.1 已按 OTHER 允许 `contentDescription` 落地；不视为计划缺口。
- **写集互斥 / DAG / 双端解析实现细节 / 契约工程命名**：属 architect / apiDataDesigner / parallelPlanner / qaStrategist 等职责，本角色不裁定、不代批。
