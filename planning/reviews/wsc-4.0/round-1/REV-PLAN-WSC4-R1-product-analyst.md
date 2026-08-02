# Round 1 Review — Product Analyst

```yaml
reviewId: REV-PLAN-WSC4-R1-productAnalyst
planId: PLAN-WSC-4.0
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-004
actorInstance: product-analyst-wsc-004-r1
decision: APPROVE
summary: |
  对照 SNAP-WSC-004（APPROVED）：REQ-CAT-008/004/005、REQ-API-001、REQ-RBAC-001
  均有主任务与 §7 映射；编辑/详情/导入三面覆盖 OpenAPI；v0729 列映射绑定 SNAP
  与权威模板、未见回退；旧模板文件级拒绝 + ERR_IMPORT_TEMPLATE_UNSUPPORTED 已冻结。
  In/Out Scope、已关闭 OQ-V13 口径与 UX/功能基线不回退一致。本轮无产品侧异议；
  不代批他角色结论。
```

## 评审范围（产品分析）

- 权威快照：`product/requirements/SNAP-WSC-004.md`（`snapshotId: SNAP-WSC-004`，`status: APPROVED`）。
- 候选计划：`planning/proposals/PLAN-WSC-4.0.md`（`DRAFT` / `CANDIDATE`，`round: 1`）。
- 聚焦：P0 REQ 任务覆盖、列映射未回退、旧模板拒绝、OpenAPI 三面、OQ 闭合口径吸收、范围红线。
- **不**裁定写集/DAG/契约工程细节；**不**代替 architect / apiDataDesigner / qaStrategist / security / uxUiPlanner / parallelPlanner / planEditor 等角色批准或关闭其 ISSUE。

## 覆盖核对摘要（相对 SNAP-WSC-004）

| 项 | 结论 |
|---|---|
| In Scope = 修订/新增/继承 5 条 P0 REQ | **对齐**：§1.1 + 任务 301..304 + §7 映射齐全；`requirementCount: 5` |
| Out of Scope | **与 SNAP 一致**：不重做 UX 令牌；不引入异步 Job；不扩数据登记/交易/连接器等；不改写历史 SNAP |
| 功能/UX 基线 | **未静默扩大或削弱**：`functionalBaseline: SNAP-WSC-002` / PLAN-WSC-2.2；`uxBaseline: SNAP-WSC-003` / PLAN-WSC-3.1；契约目标 `wsc-contracts@2.1.0` |
| 列映射（v0729） | **未回退**：301 读 SNAP + 权威 xlsx；acceptance 绑定列组一致、自动生成编码、接口定义行失败、异型非空列建议行失败；§3.1 冻结 typeSpecific 形状与 SNAP 建议形状一致 |
| 旧模板拒绝 | **已吸收**：§1.2 / §3.2 / 301+304；请求级 `ERR_IMPORT_TEMPLATE_UNSUPPORTED`；UI 态 ④；禁止伪装行级结果 |
| OpenAPI 三面 | **齐全**：编辑 302、详情 303、导入（301 解析 + 304 UI/E2E）；交互意图 §1.5 对齐 SNAP 参考交互 |
| OQ-V13-001..009 / OQ-V11-003 | **口径未回退**（见下表）；Blocking OQ 无 |
| 保留 OQ-001 / OQ-V11-004；修订 OQ-004 | **未重开冲突**：OQ-001 Out of Scope；部分成功不改变；OTHER `contentDescription` 按 SNAP 修订收录 |

### REQ → 任务覆盖

| REQ | priority | change | 主责任务 | 核对要点 |
|---|---|---|---|---|
| REQ-CAT-008 | P0 | 修订 | **301**（解析/模板/错误码）+ **304**（UI/E2E） | v0729 模板对齐；旧模板文件级拒绝；四态/报告白名单/同步完成态/上链不回退；无编码列自动生成（DEC-WSC-001） |
| REQ-CAT-004 | P0 | 修订 | **301**（DTO/后端）+ **303**（详情 UI） | 既有分组保留；按类型只读扩展描述；API 只读 endpoints+文档；旧 `endpoint` 可读；有权显示编辑入口 |
| REQ-CAT-005 | P0 | 修订 | **301**（持久化）+ **302**（编辑 UI） | 类型切换动态区；写 `swaggerFileContent`+`endpoints[]`；上链规则不回退；UX 不回退 REQ-UX-007 |
| REQ-API-001 | P0 | 新增 | **301**+**302**+**303**+**304** | 侧栏端点增删；参数/响应/可选 schema；Swagger 回填保留原文；详情只读等价；导入「接口定义」非空解析失败→行失败 |
| REQ-RBAC-001 | P0 | 继承 | **301..304** | §4 三角色矩阵；写仅 ADMIN+PROVIDER；USER 403；前端隐藏≠授权 |

### 列映射未回退（相对 SNAP §列 → 字段映射表）

| SNAP 映射要点 | 计划吸收位置 | 结论 |
|---|---|---|
| 分组 A–E / F–H / I–K / L–O / P 与权威 xlsx | §0 `templateAsset`；§3.1 模板列组；301 readSet/acceptance | 绑定权威资产，未另立列集 |
| 类型 A：接口定义→swagger+endpoints；字段描述/样例/时间地域 | §3.1 / §3.3；301 objective/acceptance | 形状与 SNAP 建议一致 |
| 类型 B：规模/形态/字段描述/样例 | §3.1 `typeSpecific.dataset`；301 扩展字段可读写 | 未删减 |
| 类型 C：内容描述 → report/other；OTHER 允许 contentDescription | §1.2 OQ-004；§3.1 report/other | 与 SNAP 修订口径一致 |
| 无产品编码列 → 服务端生成 | §1.2 OQ-V13-005；301 acceptance | CLOSED 口径已落地 |
| 更新频率含「不更新」 | §1.2 OQ-V13-008；§3.1 `NO_UPDATE` | 未回退 |
| 行业分类名称/路径匹配；失败行失败 | §1.2 OQ-V13-006 | 未回退 |
| 异型非空列建议行失败 | 301 acceptance「与 SNAP 建议一致」 | 未放宽为默认可吞 |

### 旧模板拒绝

| 检查项 | 计划位置 | 结论 |
|---|---|---|
| 文件级拒绝（非整单行级失败） | §3.2；§3.4 态 ④ | 对齐 OQ-V13-001 CLOSED |
| 明确错误码 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` | §3.1 / §3.2；301/304 acceptance；发布门禁 | 建议码已冻结为契约/请求级 |
| 与 FORMAT 区分 | §3.2 | 可解析但列集不匹配 → TEMPLATE_UNSUPPORTED |
| UI/E2E 可区分 | 304 acceptance + E2E 场景 3 | 可验收 |

### OpenAPI 三面（editor / detail / import）

| 面 | 任务 | SNAP / REQ-API-001 要点 | 结论 |
|---|---|---|---|
| 编辑 | 302 | 侧栏列表+增删；端点详情；Swagger 导入回填+保留原文；提交 2.1.0 形状 | 覆盖 |
| 详情 | 303 | 只读 endpoints/文档信息；扩展描述；旧 endpoint 兼容 | 覆盖 |
| 导入 | 301+304 | 「接口定义」解析写入原文+endpoints；非空失败→行失败；UI/E2E | 覆盖 |

### OQ 关闭口径吸收

| OQ | SNAP 闭合结论 | 计划吸收位置 |
|---|---|---|
| OQ-V13-001 | 文件级拒绝 + 明确错误码 | §1.2；§3.2；301/304 |
| OQ-V13-002 | swagger 文本 + endpoints[] | §1.2；§3.3 |
| OQ-V13-003 | `wsc-contracts@2.1.0` | yaml；§3.1；301 |
| OQ-V13-004 | 不引入异步 Job | Out of Scope；301；301 同步完成态 |
| OQ-V13-005 | 无编码列自动生成 | §1.2；301 |
| OQ-V13-006 | 名称/路径匹配；失败行失败 | §1.2 |
| OQ-V13-007 | 读兼容、写优先新结构 | §1.2；§3.3；302/303 |
| OQ-V13-008 | 扩枚举「不更新」 | §1.2；§3.1 |
| OQ-V13-009 | 接口定义解析失败→行失败 | §1.2；301 |
| OQ-004（修订） | OTHER 允许 contentDescription | §1.2；§3.1 |
| OQ-V11-003 | 暂定不拆实体 | §1.2；§2.2 |
| OQ-001 / OQ-V11-004 | 保留 | Out of Scope / 部分成功不改变 |

## 功能基线与范围（产品红线）

- 计划明确 V1.3 增量（契约 2.1.0 + 后端 typeSpecific/导入 + 三面前端），不改写 SNAP-001/002/003 正文——与 SNAP 权威声明一致。
- 导入四态、报告白名单、RBAC、上链、同步完成态均声明不回退——满足 SNAP 成功标准。
- UX：增量控件消费既有令牌、不强制 Element Plus、主路径质感不回退 3.1——对齐 `inheritedUxNoRegression`。
- 候选声明与 §10 明确 Round 1 独立评审尚未发生、**不伪造 APPROVE**——本角色亦不代写他角色同意。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 1 对照 SNAP-WSC-004：REQ 覆盖、列映射、旧模板拒绝、OpenAPI 三面与 OQ 吸收均满足本角色验收门槛；无新产品异议。 | — | — |

## 非异议说明（避免重复开单 / 不代他角色）

- **列映射未全文内联 A–P 表**：计划以 SNAP + 权威 xlsx 为映射权威，301 objective/acceptance 绑定列组与关键字段行为；产品侧视为未回退，不单开 ISSUE。若契约/实现角色要求计划正文复述全表，由其职责处理。
- **≤10MB**：SNAP CAT-008 明示；计划经 `ERR_IMPORT_FILE_TOO_LARGE` 与 V1.1 基线继承不回退覆盖，本轮不单开。
- **OQ-004 表内「修订待确认」措辞**：SNAP yaml/`poSnapshotConfirm` 与计划 §1.2 已按 OTHER 允许 `contentDescription` 落地；不视为计划缺口。
- **写集互斥 / DAG / 契约字段工程命名**：属 architect / apiDataDesigner / parallelPlanner 等职责，本角色不裁定、不代批。
- **他角色 ISSUE**：本文件不代关、不代批。
