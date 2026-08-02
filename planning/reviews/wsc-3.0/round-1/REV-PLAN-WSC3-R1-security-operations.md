# Round 1 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC3-R1-securityOperations
planId: PLAN-WSC-3.0
round: 1
role: securityOperations
actorInstance: security-operations-wsc-003-r1
snapshotIdAtReview: SNAP-WSC-003
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-003（RBAC 不改变）与 PLAN-WSC-3.0：本计划默认 deny 契约/后端/写 API，无新增不安全写路径的调度面，方向正确。
  阻断 APPROVE：① UX 任务未显式禁止「纯 CSS/样式隐藏」替代既有权限条件渲染/路由守卫，写入口可见性回归仅覆盖「看起来不可见」不足；
  ② TASK-WSC-204 重绘导入结果/报告区却未挂 handles-pii，亦未继承 PLAN-WSC-2.2 §3.5 错误报告字段白名单的前端展示约束。
  另开 P2：走查截图证据未要求脱敏/fixture，存在证据目录泄漏运营 PII 的运维风险。
  对纯视觉令牌/圆角/色板项本角色 ABSTAIN（见适用性说明）。无证据 BLOCK 业务；不代批其他角色。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-3.0.md` |
| 快照 | `product/requirements/SNAP-WSC-003.md`（`status: APPROVED`；权限矩阵与 SNAP-WSC-002 **相同**） |
| 功能/导入安全基线 | SNAP-WSC-002 + 已批准 `PLAN-WSC-2.2` §3.5 / §4（本计划只读继承，不得静默弱化） |
| 本角色焦点 | 权限可见性 / 仅前端隐藏；导入弹窗与 PII 展示样式变更；无新增不安全写路径 |
| 纯视觉适用性 | **ABSTAIN**：令牌色值、圆角、阴影、字号阶梯、图表纯 CSS 观感等不构成安全/运维否决依据（见下文） |

## 评审范围与判定

| 维度 | 结论 |
|---|---|
| 无新增不安全写路径 | **通过方向** — §1.1 Out of Scope + 全任务 `denyModify`：`contracts/**`、`frontend/src/api/**`、`backend/**`、`**/sql/**`；206 禁止新增业务 API / 改 RBAC 逻辑；无 Flyway/写后端任务 |
| RBAC 矩阵是否被计划改写 | **通过方向** — SNAP-WSC-003 声明不改变；计划 Out of Scope「RBAC 矩阵」；204/205 acceptance 写明权限行为不变 |
| UX 是否削弱权限可见性 / 仅前端隐藏 | **缺口** — 有「可见性不回退」与 E2E 场景 6，但未禁止以 CSS 隐藏替代结构权限控制（ISSUE-SEC-UX-R1-001） |
| 导入弹窗样式 vs 报告/PII 展示 | **缺口** — 204 含结果反馈区视觉改造，缺 `handles-pii` 与 §3.5 白名单展示继承（ISSUE-SEC-UX-R1-002） |
| 详情/编辑敏感字段截断 | **通过方向** — 205 `riskTags: [handles-pii]` + acceptance「不得扩大明文回显」+ §7 回滚项；仍依赖 CR，非本轮异议 |
| 鉴权/会话模型 | **无新风险面** — 202 默认禁止改 `auth/store|composables|api`；本 UX Run 不调度改后端会话；不因未重述 PLAN-WSC-1.1 §3.2 开 ISSUE |
| 走查截图证据中的 PII | **运维缺口（P2）** — §3.1 要求导入/详情等截图，未约定 fixture/脱敏（ISSUE-SEC-UX-R1-003） |
| 纯视觉项 | **ABSTAIN**（适用性见下） |

## 纯视觉 ABSTAIN（适用性）

对本计划中下列内容，本角色 **ABSTAIN**，不据此 `APPROVE`/`REQUEST_CHANGES`/`BLOCK`：

- §2.1 设计令牌色值/阴影/字体栈、卡片圆角约 12px、侧栏宽 232px 等观感对齐；
- AntDV 主题覆写以达到「像原型」的纯样式选择（OQ-UX-002），**除非**覆写改变控件可达性/焦点陷阱导致权限入口不可测或误触写操作（该情形归入 ISSUE-SEC-UX-R1-001）；
- 总览图表 CSS/配置级视觉收敛、空态装饰性文案语气（非安全语义）。

ABSTAIN **不**覆盖：角色切换区/写入口条件渲染、导入结果与错误报告字段展示、敏感字段截断、路由/API 写路径、证据制品中的真实 PII。

## 风险对照

| 风险点 | 计划措施 | 判定 |
|---|---|---|
| UX 顺手改写 API / 后端写路径 | 全任务 denyModify + §8 门禁 diff 不含 contracts/backend | 通过方向 |
| 仅 CSS 隐藏特权入口，权限条件被删/旁路 | 201/204/205 acceptance「可见性/不可达不变」；206 E2E「写入口可见性不回退」；**未**写明禁止 CSS 替代结构权限 | 缺口（ISSUE-001） |
| 导入弹窗结果区样式变更扩大 PII 回显 | 204 四态可区分 + 权限矩阵不变；**未**继承 §3.5 白名单展示约束；无 handles-pii | 缺口（ISSUE-002） |
| 详情敏感字段截断回退明文 | 205 riskTag + acceptance + §7 | 通过方向 |
| 走查截图入库含真实 PII | §3.1/206 要求截图归档；无脱敏/fixture 约束 | 缺口（ISSUE-003，P2） |
| 登录页视觉任务改会话语义 | 202 denyModify auth store/composables/api（默认） | 通过方向 |
| 范围蔓延到后端/契约 | §1.1 默认 deny；须 PO+新快照 | 通过方向 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-UX-R1-001 | P1 | SNAP-WSC-003 / SNAP-WSC-002 `REQ-RBAC-001` 禁止仅靠前端隐藏作为写权限防护；本计划 TASK-WSC-201（`RoleSwitcher`/壳层）、204（分类/导入弹窗）、205（编辑入口）、206（E2E）均触及特权 UI。计划仅要求「写入口可见性不回退 / 非管理员不可达行为不变」，**未**明确：UX 改造必须保留既有权限条件渲染与路由/菜单守卫；**禁止**以 `display:none` / `visibility` / `opacity` / `pointer-events` 等纯样式手段替代或弱化 `v-if`/路由 meta/守卫。存在「看起来像不可见、DOM/快捷键仍可达」或误删条件导致越权入口暴露的窗口。 | 在 §1.x 硬门禁或 §7 风险表增加一条显式约束：UX 任务不得以纯 CSS/样式隐藏替代既有权限条件渲染与路由/菜单守卫；特权控件对无权限角色须保持结构不可达（与 V1.1 一致）。并在 TASK-WSC-201、204、205 的 `acceptance`（及 206 §3.2 回归意图）写入可测口径：三角色下写入口/分类维护/导入/编辑的可达性与 SNAP-WSC-002 一致，且校验不得仅依赖「肉眼不可见」。关闭前不得标为已接受风险。 | REQ-RBAC-001, REQ-SHELL-001, REQ-UX-002, REQ-UX-005, REQ-UX-006, REQ-UX-007, REQ-UX-011 |
| ISSUE-SEC-UX-R1-002 | P1 | PLAN-WSC-2.2 §3.5 已冻结：错误报告字段白名单（行号、产品编码、原因码/文案；**禁止**完整信用代码、ownerDID、明文哈希）及报告鉴权。PLAN-WSC-3.0 TASK-WSC-204 目标含导入弹窗「结果反馈区」视觉对齐，`writeSet` 含 `catalog/import/**`，acceptance 强调四态可区分与权限矩阵不变，但 **未**声明前端结果/报告展示继续遵守 §3.5 白名单、不得因样式/结构改造扩大明文回显；且 204 `riskTags` 为空（对比 205 已标 `handles-pii`）。样式重构易误把原始错误 payload 整段渲染进反馈区。 | 为 TASK-WSC-204 增加 `riskTags` 含 `handles-pii`（或等价）；在 204 `acceptance`/`testScope` 写明：导入成功/失败/部分成功的结果区与错误报告入口展示字段不得超出 PLAN-WSC-2.2 §3.5 白名单；禁止扩大信用代码/ownerDID/明文哈希等回显；至少一条 Vitest 或既有断言回归覆盖「禁止字段不出现在结果 UI」。后端/契约仍 denyModify（本 ISSUE 不要求改 API）。 | REQ-CAT-008, REQ-UX-006, REQ-UX-009 |
| ISSUE-SEC-UX-R1-003 | P2 | §3.1 / TASK-WSC-206 要求将 `07-import-modal.png`、`08-product-detail.png`、`09-product-editor.png` 等截图落入 `ai/runs/RUN-WSC-003/ux-walkthrough/`。计划未要求使用 fixture/脱敏演示数据。若走查环境含真实客户/主体标识，证据目录将成为 PII 持久化面（与功能 Run 的报告白名单精神不一致）。 | 在 §3.1 或 TASK-WSC-206 `acceptance` 写明：走查截图与入库证据须使用测试 fixture 或已脱敏演示数据；禁止将含真实 PII 的截图作为发布证据提交；若误用须重拍替换后再关门禁。 | REQ-UX-011, REQ-UX-006, REQ-UX-007 |

## 无异议项（记录）

- 不因「像不像原型」、AntDV 覆写策略或桌面窄屏修复否决业务；纯视觉适用性见 ABSTAIN。
- 无新增后端/契约/SQL 写任务；在字面 `denyModify` + Orchestrator scope-check 被遵守前提下，**无证据**表明本计划调度出新的不安全写 API 路径。
- 205 对敏感字段截断不回退的声明与 §7 缓解可接受为详情域基线；不与 204 缺口合并关闭。
- 202 对鉴权逻辑默认只读的边界正确；本轮不要求 UX 计划重述会话吊销模型全文。
- 高风险接受未伪造；本评审不代替人类 Security/Operations Owner 签署残余风险。
- 本角色不代替 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor 或 maintainer 批准；仅就安全/运维域给出 `REQUEST_CHANGES`。
- 升级路径：若 ISSUE-SEC-UX-R1-001/002 被拒绝修补 → 升级 securityOperationsOwner；不因此否决 SNAP-WSC-003 UX 业务目标本身。

## decision

**REQUEST_CHANGES** — 待 ISSUE-SEC-UX-R1-001（P1）与 ISSUE-SEC-UX-R1-002（P1）按 `closeWhen` 写入计划后，本角色可在后续 round 重评。ISSUE-SEC-UX-R1-003（P2）建议同轮一并修补，不单独构成 BLOCK。
