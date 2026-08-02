# Round 1 Review — Product Analyst

```yaml
reviewId: REV-PLAN-WSC3-R1-productAnalyst
planId: PLAN-WSC-3.0
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-003
actorInstance: product-analyst-wsc-003-r1
decision: APPROVE
summary: |
  对照 SNAP-WSC-003（APPROVED）与 PRD wsc-v1.2-ux：REQ-UX-001..011 均有主任务/映射；
  In/Out Scope 与功能基线 SNAP-WSC-002 + wsc-contracts@2.0.0 只读继承一致，未见静默扩 scope；
  OQ-UX-001..004 闭合口径已写入假设、令牌/AntDV 策略、视觉走查与 E2E 复用约定。
  本轮无产品侧异议；不代批他角色结论。
```

## 评审范围（产品分析）

- 权威快照：`product/requirements/SNAP-WSC-003.md`；PRD：`product/prd/wsc-v1.2-ux.md`。
- 候选计划：`planning/proposals/PLAN-WSC-3.0.md`（`DRAFT` / `CANDIDATE`，round 1）。
- 聚焦：REQ-UX-001..011 任务覆盖、功能基线未被静默扩大、已关闭 OQ 是否被计划吸收。
- **不**裁定技术选型/写集工程细节；**不**代替 architect / frontendLead / qaLead / security / techWriter / planEditor 等角色批准。

## 覆盖核对摘要（相对 SNAP-WSC-003）

| 项 | 结论 |
|---|---|
| In Scope = REQ-UX-001..011（11 条） | **对齐**：§1.1 + 任务 201..206 + §6 映射齐全 |
| Out of Scope | **与 SNAP/PRD 一致**：不改契约/后端/RBAC/领域模型；不新开业务能力；不做移动端 App/暗色主题；禁止 AntDV 默认主题凑合 |
| 功能基线 | **未静默扩大**：`functionalBaseline: SNAP-WSC-002`、契约只读；任务 `denyModify` 含 `contracts/**`、`frontend/src/api/**`、`backend/**`；继承 REQ 仅作回归约束 |
| 视觉权威 | 已声明 `design/prototypes/wsc-v1.1/index.html`；与 SNAP/PRD 一致 |
| OQ-UX-001..004 | **已吸收**（见下表） |
| 保留 OQ-001 / 004 / V11-003 / V11-004 | **未重开**；§1.2 声明本 UX 计划不裁定 |

### REQ → 任务覆盖

| REQ | 优先级 | 主责任务 | 核对要点 |
|---|---|---|---|
| REQ-UX-001 | P0 | 201（206 走查复核） | §2.1 令牌最小集与 SNAP acceptance 对齐；禁止默认主题冒充 |
| REQ-UX-002 | P0 | 201 | 侧栏/品牌/激活态/主区/企业卡/占位质感；写入口行为回归 SHELL/RBAC |
| REQ-UX-003 | P0 | 202 | 指标卡+图表视觉；数据契约/图表库默认不改；OVW 功能口径回归 |
| REQ-UX-004 | P0 | 203 | 标签/子类分节/筛选/列表/预览/加载中/空态；行为继承 CAT-001 |
| REQ-UX-005 | P0 | 204 | 页签/筛选/批量条/表格分页层次；非管理员不可达不变 |
| REQ-UX-006 | P0 | 204 | 分类弹窗≈960 / 导入≈520、footer 层级；CAT-006/008 + 导入四态可区分 |
| REQ-UX-007 | P0 | 205 | 详情/编辑/上链层次；写路径与 CAT-004/005、CHAIN-001 不改 |
| REQ-UX-008 | P1 | 201（角色区）+ 202（登录） | 拆分写集已声明，覆盖 SNAP 两项呈现 |
| REQ-UX-009 | P1 | 202/203/204/205 落地 + **206** 统一抽检 | 非单页独占模式可接受；发布门禁含主路径空态/加载/toast |
| REQ-UX-010 | P1 | 201（壳层）+ **206** 桌面破坏性抽检 | 与 SNAP「桌面为主、不要求移动完整适配」一致 |
| REQ-UX-011 | P0 | 206 | 差距清单 + 走查签字/截图 + 复用扩展 E2E；对齐 OQ-UX-003/004 |

### OQ 关闭口径吸收

| OQ | SNAP 闭合结论 | 计划吸收位置 |
|---|---|---|
| OQ-UX-001 | 允许有限全局 CSS / 令牌重构，不改业务 API | §1.2；§2；TASK-201 `writeSet`（styles/theme/App/main） |
| OQ-UX-002 | 优先覆写 AntDV；无法达标时经注明局部替换 | §1.2；§2.2；201 objective/acceptance |
| OQ-UX-003 | 人工走查签字 + 主路径截图清单为门禁；Playwright 截图可选 | §1.2；§3.1；206 acceptance；§8 发布门禁 |
| OQ-UX-004 | 复用扩展既有 E2E，不必单独同名 UX E2E Run | §1.2；§3.2；206 writeSet/testScope |

## 功能基线与范围（产品红线）

- 计划明确 **UX 增量**、不重开 SNAP-WSC-002 功能缺陷（除非本轮 UX 引入回归）——与 SNAP/PRD 一致。
- 扩 scope（后端/契约）路径：须 PO + 新快照/DEC；默认不可调度——符合「不得静默扩大」。
- 任务 acceptance 多处回引 SHELL/RBAC/OVW/CAT/CHAIN 行为不回退；206 E2E 最低场景覆盖三角色主路径——满足 REQ-UX-011 功能回归意图。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 1 对照 SNAP-WSC-003：覆盖、范围、OQ 吸收均满足本角色验收门槛；无新产品异议。 | — | — |

## 非异议说明（避免重复开单 / 不代他角色）

- **REQ-UX-003 四类图表命名**：SNAP 点名「近 30 天趋势、TOP、最新上链流、分布」；202 acceptance 以「图表卡片…对齐原型」概括。走查截图 `03-overview.png` + OVW 回归足以产品验收，本轮不单开 ISSUE；若他角色要求验收句式逐条点名，由其职责处理。
- **§6「REQ-UX-010 + 各页」**：202–205 `requirements` 未再挂 UX-010，门禁落在 201 壳层 + 206 破坏性抽检，与 SNAP 验收句式足够；不视为覆盖缺口。
- **中文用语**：SNAP 成功标准要求用语与原型/V1.1 一致；计划在 203 显式写出，其余页由「对齐原型」+ 206 走查覆盖。本轮不单开 ISSUE。
- **技术写集互斥 / DAG / AntDV 实现细节**：属 architect/frontendLead 等职责，本角色不裁定、不代批。
