# Round 1 Review — Plan Editor（计划结构完整性）

```yaml
reviewId: REV-PLAN-WSC9-R1-plan-editor
planId: PLAN-WSC-9.1
round: 1
role: planEditor
snapshotIdAtReview: SNAP-WSC-009
actorInstance: plan-editor-wsc-012-r1
decision: APPROVE
summary: |
  作为隔离评审实例（≠起草者 plan-editor-wsc-012-r1），对照 planEditor 批准门禁
  核对 PLAN-WSC-9.1：候选声明 / status=DRAFT / planType=CANDIDATE / round=1 /
  basedOn=PLAN-WSC-8.3 齐全；§0.2 预落地对账正确识别 contracts/VERSION 工作树为 2.3.3
  （高于 contractsTarget 声明 2.3.2）并由 911 唯一合并增量；911..919 均全文内联
  requirements、dependsOn、readSet、writeSet、denyModify、acceptance、testScope、
  riskTags；DAG 无环（911 为唯一根）；§7 映射21条 REQ 与 SNAP-WSC-009 完全一致；
  §0.3 文件互斥表与各任务 writeSet/denyModify 字面路径对齐；任务编号 911 起未复用
  901–910；每任务有 dependsOn 或为 DAG 根；基线能力不回退声明分布在 §0.1/§1.1/§1.5
  及多个任务 acceptance 中，覆盖齐全。3 条非阻塞备注不构成结构缺陷。本角色不代关
  他角色 ISSUE；不改写计划 status。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **通过** — 911..919 均具备 requirements / dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags |
| 字面 writeSet / scope-check 可调度性 | **通过** — 各任务 writeSet 均为字面路径；denyModify 含 `ai/runs/**/state.yaml` / `events.jsonl`；911 独占契约、912 独占 SQL、914 独占 useCanWrite 与 WorkbenchLayout |
| denyModify 可机械判定 | **通过** — §0.3 互斥表与各任务 denyModify 逐条对齐；无叙述性/条件性软例外 |
| 验收 / 测试范围 | **通过** — 各任务有可测 acceptance + testScope；919 E2E 写集已对齐 §6.1/§9 |
| §9 发布门禁 | **通过（结构项）** — 十一项齐全；P0 21条 REQ + 919 绑定明确；P1（005/017）随 918 不单独阻塞 |
| SNAP-WSC-009 一致性 | **通过** — 21条 REQ 映射完整；PO 锁定项、Out of Scope、波次语义一致 |
| 候选声明 / DRAFT·CANDIDATE | **通过** — 文首候选声明 + YAML；§10 未勾选委员会 APPROVE |
| 预落地对账节 | **通过** — §0.2 波次表 + §0.3 文件互斥总览；VERSION 2.3.3 对账显式标注 |
| ISSUE 编号 | **无** — 本轮无阻塞级结构缺陷 |
| 伪造全员同意 / 擅标 APPROVED | **无** |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁；`REV-PLAN-WSC8-R1-plan-editor.md` 历史 PE 字面 writeSet 口径。本轮仅评 `planning/proposals/PLAN-WSC-9.1.md` **结构完整性**；**不**关闭他角色异议；**不**将计划 `status` 改为 `APPROVED`。

## 完整性自检（对照计划批准门禁 · 结构项）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | SNAP-WSC-009 21条 REQ 均有 acceptance；§5 主任务 + §7 映射可追溯 | 通过 |
| API / 数据 / UI 契约无未决冲突 | §3.1 冻结 wsc-contracts@2.3.3 工作树增量；911 唯一写任务；blocking OQ=无（§2.2） | 通过 |
| 每任务有依赖、文件边界、读写集、测试范围 | §5：911..919 字段齐全；边界字面路径可调度 | 通过 |
| DAG 无环；并行写集无交集 | §6 mermaid 无环；911 为唯一根；并行允许对（912∥914、913∥914）路径不相交 | 通过 |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.2 迁移 912 独占；§8 风险回滚；912→migrationReviewer；911/913/914/916→securityReviewer（§1.4/§6.2） | 通过 |
| 所有必需角色输出 APPROVE | Round 1 收集阶段；§10 未勾选 | **不适用本角色单点关闭** — 保持 CANDIDATE |

## SNAP-WSC-009 一致性核对

| 检查项 | SNAP-WSC-009 | PLAN-WSC-9.1 | 结论 |
|---|---|---|---|
| REQ 数量与 ID | 21条（20×P0 + 1×P1；含20条新增 + 1条修订） | §7 映射21条；YAML `requirementCount: 21` | 一致 |
| PO 锁定：订单菜单角色化 | USER=我的订单；PROVIDER/ADMIN=交易订单 | §3.1 矩阵 `ordersUI`；§4 RBAC；914 acceptance | 一致 |
| PO 锁定：ADMIN 可代 PROVIDER | adminCanActAsProvider: true | §3.1 confirm/confirmContract ADMIN=200 any；§4 | 一致 |
| PO 锁定：状态不可回退 | stateIrreversible: true | §1.2 假设；916 acceptance 逆向 POST→4xx | 一致 |
| PO 锁定：止于合约达成 | scopeEndsAt: contract-reached | §1.1 Out of Scope；§1.2 假设 | 一致 |
| PO 锁定：上链 mock | chainAttestation: mock | §1.2 假设；913/916 acceptance | 一致 |
| PO 锁定：附件规则 | word-pdf-20mb-single-unlimited-retention + whitelist-plus-virus-scan | §1.2 假设；916 acceptance | 一致 |
| PO 锁定：金额 CNY 两位 | CNY-2dp + taxInclusiveEqualsExclusive | §3.1 金额字段；916 acceptance | 一致 |
| PO 锁定：分页 jumper+pageSize | pagination: jumper-and-pageSize；tiers [10,20,50,100] | §1.5；915 acceptance | 一致 |
| PO 锁定：产品快照 | productSnapshot: at-order-time | §1.2 假设；913 acceptance | 一致 |
| PO 锁定：统一合约确认页 | unifiedContractConfirmPage: true | §1.2 假设；917 acceptance | 一致 |
| 波次 A/B/C 语义 | SNAP §交付波次 | 计划拆 A/B/C/D；D 为 E2E；语义不冲突 | 一致 |
| contractsTarget | wsc-contracts@2.3.2 | YAML + §0.1 声明2.3.2；§0.2 对账工作树2.3.3（见非阻塞备注1） | 一致（对账已标注） |
| REQ-SHELL-011 取代 SHELL-001 | superseded-behavior 明确 | §7 REQ-SHELL-011 备注「取代 SHELL-001 订单占位」 | 一致 |

## 结构抽查（通过项）

| 检查项 | 证据 |
|---|---|
| 911..919 均含 `requirements` | §5 各任务块首字段 |
| 911..919 均含 `dependsOn` | 911=[]（DAG 根）；912..919 均非空；与 §6 DAG 一致 |
| 911..919 均含 `denyModify` | 含 `ai/runs/**/state.yaml` / `events.jsonl`；919 deny V1.6 config |
| 911..919 均含 `acceptance` / `testScope` / `riskTags` | §5 内联完整 |
| 文件级互斥总览 | §0.3：contracts→911；sql→912；order BE→913→916；useCanWrite/Workbench→914；order FE→915→917→918；e2e→919 |
| E2E 写集与 §9 对齐 | 919 writeSet 含 `tests/e2e/**`；§6.1/§9 绑定 evidenceId + test:p0-v17 |
| 禁止 913∥916 | §6 并行表 + 916 dependsOn 913（串行订单 BE） |
| 禁止 914∥915 并行写 routes | 915 dependsOn 914；§6 并行表明确 |
| REQ 主任务分配 | §7 与 §5 requirements 交叉一致（21条全覆盖） |
| 任务编号不复用 | 911..919；PLAN-WSC-8.3 用 901..908；无冲突 |
| 基线能力不回退 | §0.1 功能基线声明；§1.1 Out of Scope；§1.5 UX 不回退；911/914/915 acceptance |

### 任务字段矩阵

| taskId | requirements | writeSet | denyModify | acceptance | testScope | riskTags | dependsOn |
|---|---|---|---|---|---|---|---|
| 911 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓（[]=DAG 根） |
| 912 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 913 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 914 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 915 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 916 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 917 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 918 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |
| 919 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ | ✓ |

## §9 发布门禁核对

| # | §9 条目 | 计划内锚点 | 结论 |
|---|---|---|---|
| 1 | SNAP-WSC-009 已 APPROVED + 委员会 APPROVE 后发布 | 候选声明；§9.1 | 结构完整 |
| 2 | 全部 P0 REQ VERIFIED（含 919） | §7 20条 P0 + REQ-SHELL-011；919 dependsOn | 结构完整 |
| 3 | 契约增量无未决冲突；仅 911 动 VERSION | §3.1；911 唯一写 | 结构完整 |
| 4 | Wave A 能按角色下单/看列表；toast；分页 | 911..915 acceptance | 结构完整 |
| 5 | Wave B 主路径+取消+附件+统一确认+mock 上链；securityReviewer | 916/917；§1.4/§6.2 | 结构完整 |
| 6 | Wave C 版本展示、外部跳转不建单、脱敏假设 | 918 acceptance | 结构完整 |
| 7 | UX/RBAC 无 P0 回退 | §1.5；§6.1 场景 9 | 结构完整 |
| 8 | §6.1 E2E 强制通过 + test:p0-v17 + v1.6/v1.4 共存 | 919 writeSet + §6.1 证据表 | 结构完整 |
| 9 | 912 migrationReviewer 通过 | §1.4/§6.2 | 结构完整 |
| 10 | 角色隔离 + scope-check + 预落地对账 | §1.4；§6.2；§0.2 | 结构完整 |
| 11 | maintainer 人类发布批准 | §9.11 | 结构完整 |

## 非阻塞备注（刻意不升格为异议）

### 备注 1：RBAC 矩阵键名与 OpenAPI 端点路径措辞差异

§3.1 矩阵键 `ordersSubmitContractApi` 对应端点为 `POST /orders/{orderId}/contract`——端点路径不含 "submit"。矩阵键作为标识符不影响实现正确性，但 `§3.1` 要求矩阵与 OpenAPI「逐 cell 一致」，措辞差异可能在交叉核对时产生歧义。建议 911 在冻结契约时将矩阵键改为 `ordersContractApi` 或在 OpenAPI description 中显式标注矩阵键映射。

### 备注 2：RBAC 矩阵未覆盖只读端点

`GET /orders/notices/current`（统一须知）和 `GET /orders/{orderId}/attachment`（附件下载）在 §3.1 契约表中已定义，但 RBAC 矩阵表未包含。前者所有角色均可读；后者按角色下载（§3.1 已标注越权 403）。矩阵注释要求与 OpenAPI「逐 cell 一致」——若此声明意为「所有受控端点」，建议显式排除 uniform-access 只读端点；若意为「全部端点」，则需补充两行。不阻塞调度，因端点本身已在契约中定义且 913/916 acceptance 已覆盖。

### 备注 3：数字合约版本实体/仓储归属

918 objective 要求「每次状态变更出新版本并保留历史」，需写入 `t_order_contract_version` 表（由 912 创建）。918 writeSet 仅含 `service/order/**`（后端），不含 entity/repository 路径。913 writeSet 含 `entity/**` / `repository/**`（仅订单相关），916 同。实现时数字合约版本 entity/repository 需由 913 或 916 创建，918 仅在 service 层调用——这与 913/916 的宽泛 entity 写权限兼容，但计划未显式指定归属任务。建议 916 acceptance 或 writeSet 注释明确包含合约版本 entity/repository 初始化。

## 决策

`APPROVE` — 本角色无阻塞级结构缺陷；3 条非阻塞备注。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；**不得**将 `status` 标为 `APPROVED` 或写入 `planning/approved/`；本评审**不**写 `ai/runs/**`、**不** commit。
