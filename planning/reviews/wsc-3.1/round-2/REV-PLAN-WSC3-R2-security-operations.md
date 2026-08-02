# Round 2 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC3-R2-securityOperations
planId: PLAN-WSC-3.1
round: 2
role: securityOperations
actorInstance: security-operations-wsc-003-r2
snapshotIdAtReview: SNAP-WSC-003
decision: APPROVE
summary: |
  对照 Round 1 本人提出的 ISSUE-SEC-UX-R1-001/002/003，逐条核验 PLAN-WSC-3.1 修订与 closeWhen。
  §1.4 + §7 + 201/204/205/206 显式禁止纯 CSS 替代权限条件渲染，三角色可达性须结构不可达、不得仅靠肉眼不可见。
  §3.5 内联导入结果/报告前端展示白名单；TASK-WSC-204 riskTags 含 handles-pii，acceptance/testScope 含禁止字段负例。
  §3.1/206 写明走查截图须 fixture/脱敏，禁止真实 PII 证据入库。
  三条 ISSUE 的 closeWhen 均已满足，本角色 APPROVE。无新增异议；剩余未关闭 ISSUE：0。不代批其他角色。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-3.1.md` |
| 快照 | SNAP-WSC-003（`status: APPROVED`；权限矩阵与 SNAP-WSC-002 **相同**） |
| R1 对照 | `planning/reviews/wsc-3.0/round-1/REV-PLAN-WSC3-R1-security-operations.md` |
| 谱系 | `basedOn`/`lineageFrom: PLAN-WSC-3.0`；本轮为原提出者对 R1 ISSUE `closeWhen` 的确认评审 |
| 本角色焦点 | 权限禁纯 CSS；204 handles-pii / 展示白名单；截图脱敏 |
| 纯视觉适用性 | **ABSTAIN**：令牌色值、圆角、阴影、字号阶梯等观感项不构成安全/运维否决依据（同 R1） |

## closeWhen 核验（ISSUE-SEC-UX-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-3.1 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-SEC-UX-R1-001 | P1 | §1.x 或 §7 显式禁止纯 CSS/样式隐藏替代权限条件渲染与路由/菜单守卫；特权控件结构不可达；201/204/205 acceptance 与 206/§3.2 可测口径：三角色可达性不得仅依赖「肉眼不可见」；关闭前不得标为已接受风险 | §1.4 硬门禁逐条列出 `display:none` / `visibility` / `opacity:0` / `pointer-events:none` 禁止项，要求结构不可达与「不得仅依赖肉眼不可见」；§7 风险行「纯 CSS 隐藏削弱权限」缓解指向 §1.4+201/204/205/206+E2E 矩阵；201 acceptance「结构条件渲染保留；禁止纯 CSS」；204 acceptance「禁止纯 CSS 替代权限；校验不得仅靠肉眼不可见」；205 acceptance「结构不可达，禁止纯 CSS 替代」；§3.2 场景 6 PROVIDER「结构不可达，非仅 CSS 隐藏」；206 acceptance/testScope 三角色矩阵 +「可达性校验非仅肉眼不可见」。非接受风险表述 | **closeWhen 满足，可关闭** |
| ISSUE-SEC-UX-R1-002 | P1 | 204 `riskTags` 含 `handles-pii`；acceptance/testScope 写明结果区/报告入口展示不得超出白名单（信用代码/ownerDID/明文哈希禁止回显）；至少一条 Vitest/既有断言负例；后端/契约仍 denyModify | §3.5 内联允许字段与禁止回显字段（等价吸收 PLAN-WSC-2.2 §3.5 前端展示意图）；204 `riskTags: [handles-pii]`；204 acceptance「遵守 §3.5 白名单；禁止扩大信用代码/ownerDID/明文哈希回显」；204 testScope「至少一条 Vitest 或既有断言：禁止字段不出现在导入结果 UI」；204 `denyModify` 含 `contracts/**`、`frontend/src/api/**`、`backend/**`；§7「导入结果区扩大 PII 回显」缓解指向 §3.5+204+Vitest 负例 | **closeWhen 满足，可关闭** |
| ISSUE-SEC-UX-R1-003 | P2 | §3.1 或 206 acceptance：走查截图/入库证据须 fixture 或已脱敏演示数据；禁止真实 PII 截图作发布证据；误用须重拍替换后再关门禁 | §3.1「截图脱敏」行完整写明 fixture/脱敏、禁止真实 PII、误用重拍；206 acceptance「正式截图最低清单齐全（脱敏/fixture）」；206 testScope「脱敏声明」；§7/§8 发布门禁同步要求脱敏 | **closeWhen 满足，可关闭** |

## 本轮 ISSUE 表

无未关闭 ISSUE。R1 三条均确认可关闭；本轮不新增 ISSUE。

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 无异议项（记录）

- 全任务 `denyModify` 契约/后端/写 API；无新增不安全写路径调度面（同 R1 通过方向，本轮复核仍成立）。
- 205 敏感字段截断不回退 + `handles-pii` 仍可接受为详情域基线。
- 202 对 `auth/store|composables|api` 绝对 `denyModify` 边界正确。
- 纯视觉令牌/圆角/色板项本角色 **ABSTAIN**，不据此否决业务。
- 高风险接受未伪造；本评审不代替人类 Security/Operations Owner 签署残余风险。
- 本角色不代替 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor / uxUiPlanner 或 maintainer 批准；仅就安全/运维域 R1 异议关闭给出 `APPROVE`。

## decision

**APPROVE** — ISSUE-SEC-UX-R1-001（P1）、ISSUE-SEC-UX-R1-002（P1）、ISSUE-SEC-UX-R1-003（P2）的 `closeWhen` 均已在 `PLAN-WSC-3.1` 满足；本角色无剩余异议。禁止代批其他角色。
