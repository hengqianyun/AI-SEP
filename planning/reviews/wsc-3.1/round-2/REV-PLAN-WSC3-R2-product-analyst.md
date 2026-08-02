# Round 2 Review — Product Analyst

```yaml
reviewId: REV-PLAN-WSC3-R2-productAnalyst
planId: PLAN-WSC-3.1
round: 2
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-003
actorInstance: product-analyst-wsc-003-r2
decision: APPROVE
summary: |
  对照 SNAP-WSC-003（APPROVED）与本角色 R1（PLAN-WSC-3.0 / APPROVE、无 ISSUE）：
  PLAN-WSC-3.1 仍完整覆盖 REQ-UX-001..011；In/Out Scope、功能基线只读继承、
  OQ-UX-001..004 闭合口径均未回退。载体 B 与 CSS-token-first 不破坏 SNAP 验收意图。
  本轮无新产品异议；不代批他角色结论，亦不代关他角色 ISSUE。
```

## 评审范围（产品分析）

- 权威快照：`product/requirements/SNAP-WSC-003.md`（`snapshotId: SNAP-WSC-003`）。
- 候选计划：`planning/proposals/PLAN-WSC-3.1.md`（`DRAFT` / `CANDIDATE`，`basedOn: PLAN-WSC-3.0`）。
- 本角色 R1：`planning/reviews/wsc-3.0/round-1/REV-PLAN-WSC3-R1-product-analyst.md`（`decision: APPROVE`，异议表空）。
- 聚焦：3.1 相对 3.0 修订是否破坏 REQ-UX 覆盖、范围红线或已关闭 OQ 口径。
- **不**裁定写集/DAG/控件基座工程细节；**不**代替 architect / frontendLead / qaLead / security / uxUiPlanner / planEditor 等角色批准或关闭其 ISSUE。

## closeWhen 确认（本角色 R1）

| ISSUE id | 结论 | 说明 |
|---|---|---|
| （无） | **N/A** | R1 对 PLAN-WSC-3.0 为 APPROVE，未开 ISSUE；无待确认 closeWhen。 |

## 覆盖核对摘要（相对 SNAP-WSC-003）

| 项 | 结论 |
|---|---|
| In Scope = REQ-UX-001..011（11 条） | **仍对齐**：§1.1 + 任务 201..206 + §6 映射齐全；`requirementCount: 11` |
| Out of Scope | **与 SNAP/PRD 一致**：不改契约/后端/RBAC/领域模型；不新开业务能力；不做移动端 App/暗色主题；禁止 AntDV 默认主题凑合；明确不强制分类维护迁为 browse overlay |
| 功能基线 | **未静默扩大**：`functionalBaseline: SNAP-WSC-002` + `wsc-contracts@2.0.0` 只读；任务 `denyModify` 含 `contracts/**`、`frontend/src/api/**`、`backend/**`；继承 REQ 仅作回归约束 |
| 视觉权威 | 已声明 `design/prototypes/wsc-v1.1/index.html`；与 SNAP 一致 |
| OQ-UX-001..004 | **口径未回退**（见下表） |
| 保留 OQ-001 / 004 / V11-003 / V11-004 | **未重开**；§1.2 声明本 UX 计划不裁定 |

### REQ → 任务覆盖（复核）

| REQ | 优先级 | 主责任务（3.1） | 相对 R1 / SNAP |
|---|---|---|---|
| REQ-UX-001 | P0 | 201（206 走查复核） | §2.1 令牌最小集仍对齐 SNAP acceptance；§2.2 CSS-token-first 仍禁止默认主题冒充 |
| REQ-UX-002 | P0 | 201 | 壳层/品牌/激活态/主区/企业卡/占位质感；写入口行为回归 SHELL/RBAC + §1.4 结构不可达 |
| REQ-UX-003 | P0 | 202 | 指标卡+图表视觉；数据契约/图表库默认不改；OVW 功能口径回归 |
| REQ-UX-004 | P0 | 203 | 标签/子类分节/筛选/列表/预览/加载中/空态；行为继承 CAT-001 |
| REQ-UX-005 | P0 | 204 | 页签/筛选/批量条/表格分页层次；非管理员不可达不变 |
| REQ-UX-006 | P0 | 204 | 分类 **modal 壳**≈960 / 导入≈520、footer 层级；CAT-006/008 + §3.4 四态；载体 B 见下 |
| REQ-UX-007 | P0 | 205 | 详情/编辑/上链层次；写路径与 CAT-004/005、CHAIN-001 不改 |
| REQ-UX-008 | P1 | 201（角色区）+ 202（登录） | 拆分写集已声明，覆盖 SNAP 两项呈现 |
| REQ-UX-009 | P1 | 202/203/204/205 落地 + **206** 统一抽检 | 非单页独占可接受；发布门禁含主路径空态/加载/toast |
| REQ-UX-010 | P1 | 201（壳层）+ **206** 桌面破坏性抽检（§3.1 双视口） | 与 SNAP「桌面为主、不要求移动完整适配」一致；抽检更可勾选 |
| REQ-UX-011 | P0 | 206 | 差距清单 schema + 走查签字/截图 + 复用扩展 E2E；分波草稿强化证据链，对齐 OQ-UX-003/004 |

### OQ 关闭口径吸收（3.1）

| OQ | SNAP 闭合结论 | 3.1 吸收位置 | 相对 R1 |
|---|---|---|---|
| OQ-UX-001 | 允许有限全局 CSS / 令牌重构，不改业务 API | §1.2；§2；TASK-201 writeSet | 未回退 |
| OQ-UX-002 | 优先覆写/局部样式对齐；无法达标时经注明局部替换 | §1.2；§2.2 **CSS-token-first**；AntDV 须替换清单+消费 201 令牌 | 实现路径更偏自研令牌，仍禁止裸用默认皮肤；不构成产品范围回退 |
| OQ-UX-003 | 人工走查签字 + 主路径截图清单为门禁；Playwright 截图可选 | §1.2；§3.1；§3.3 方案 A；206；§8 | 未回退；分波草稿增强可追踪性 |
| OQ-UX-004 | 复用扩展既有 E2E，不必单独同名 UX E2E Run | §1.2；§3.2；206 writeSet/testScope | 未回退；场景 6 三角色矩阵可勾选 |

## 3.1 产品向修订核验（相对 R1 APPROVE）

| 修订点 | 产品结论 |
|---|---|
| §1.5 分类维护 **载体 B**（独立路由 + 页内等价 modal 壳） | **可接受**：REQ-UX-006 验收要点是弹窗结构/层级/功能规则，非强制 browse overlay 宿主；偏差记入差距清单且非 P0，与 SNAP 成功标准不冲突 |
| §2.2 CSS-token-first | **可接受**：仍满足「不得仅以 AntDV 默认主题替代令牌对齐」；局部 AntDV 有门禁，不扩大功能 scope |
| 分波视觉草稿写权 / 差距 schema / 双视口 | **增强** REQ-UX-011/010 可验收性，未缩减覆盖 |
| §1.4 禁止纯 CSS 替代权限；§3.5 导入展示白名单；截图脱敏 | **保护**功能基线 RBAC/PII 呈现意图，未静默扩 scope |
| §3.4 导入四态内联 | **巩固** CAT-008 / OQ-V11-004 继承行为可验收性，非新开功能 |

## 功能基线与范围（产品红线）

- 计划明确 UX 增量、不重开 SNAP-WSC-002 功能缺陷（除非本轮 UX 引入回归）——与 SNAP 一致。
- 扩 scope（后端/契约）路径：须 PO + 新快照/DEC；默认不可调度——符合「不得静默扩大」。
- 206 E2E 最低场景含三角色写入口矩阵（结构不可达）——满足 REQ-UX-011 功能回归意图。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | 对照 SNAP-WSC-003 与 R1 APPROVE：3.1 未破坏 REQ 覆盖、范围或 OQ 口径；无新产品异议。 | — | — |

## 非异议说明（避免重复开单 / 不代他角色）

- **本角色 R1 无 ISSUE**：无需 closeWhen 关闭动作；本文件仅为对 PLAN-WSC-3.1 的独立 Round 2 产品评审。
- **他角色 ISSUE（SA/QA/PP/PE/UX/SEC 等）**：§0 映射仅记录吸收意图；关闭权归原提出者，本角色不代关、不代批。
- **REQ-UX-003 四类图表命名**：同 R1——走查截图 `03-overview.png` + OVW 回归足以产品验收，不单开 ISSUE。
- **§6「REQ-UX-010 + 各页」**：202–205 `requirements` 未再挂 UX-010，门禁落在 201 + 206（现含双视口），仍足够；不视为覆盖缺口。
- **技术写集互斥 / DAG / 203∥204 / AntDV 实现细节**：属 architect / frontendLead / parallelPlanner 等职责，本角色不裁定、不代批。
