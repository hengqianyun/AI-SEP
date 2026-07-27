# 人类决策身份（Human Owners）

> 来源：`design.md` §1 原则 8、§7.2、§18.8
>
> 控制面不会消除人类协作；Agent 无法达成一致时必须升级，不得伪造共识。
>
> 小项目可由一人承担多个身份，但**每次批准必须注明身份**。AI 的 `developer` 与 `codeReviewer` 仍不能由同一实例兼任。

## 总原则

| 规则 | 说明 |
|---|---|
| 最终决策权 | 业务范围、不可逆技术、高风险发布、最终集成与发布由人类确认 |
| 身份署名 | 批准记录须写明当时以哪个身份批准 |
| 升级触发 | `ESCALATED`、轮次耗尽、ABSTAIN 争议、P0 风险接受、控制面 `approve` 入口 |

---

## Product Owner

**身份 ID**：`productOwner`

**负责**：

- 裁决业务范围与优先级
- 关闭或授权开放问题（需求层）
- 在产品与体验/安全冲突时做业务侧取舍

**不负责**：

- 代替 Tech Lead 做不可逆架构细节
- 代替 Maintainer 做发布操作本身

**典型升级来源**：Product Analyst、UX/UI Planner、QA（验收不可测）、共识中的需求冲突。

---

## Tech Lead

**身份 ID**：`techLead`

**负责**：

- 处理架构升级与不可逆技术决策
- 审查循环超限后的拆分任务、调计划或人工处理决定
- 模块边界与技术债接受（工程侧）

**不负责**：

- 单独否决已澄清的业务优先级（应与 Product Owner 协作）

**典型升级来源**：Solution Architect、Parallel Planner、Integration Reviewer、超限的 code review。

---

## Security / Operations Owner

**身份 ID**：`securityOperationsOwner`

**负责**：

- 接受高风险发布决策（剩余风险正式接受）
- 隐私、合规、密钥与生产运维红线裁决

**不负责**：

- 无信息时的默认“全部拒绝”或“全部接受”

**典型升级来源**：Security/Operations 规划角色、Security Reviewer、Migration Reviewer（隐私数据）。

---

## Maintainer

**身份 ID**：`maintainer`

**负责**：

- 批准最终集成与发布
- 环境、流水线、仓库权限等平台阻塞的解除协调
- 发布门禁中的人工放行（若项目要求）

**不负责**：

- 在 P0 REQ 未 `VERIFIED`、审计缺失时强行发布（应拒绝）

**典型升级来源**：Orchestrator 发布门禁、Tester 环境 `BLOCKED`、Integration Reviewer 合并顺序争议。

---

## 与 Agent 的分工（摘要）

| 人类 | Agent |
|---|---|
| 范围/优先级/风险接受/最终发布 | 结构化分析与制品生成 |
| 不可逆技术与升级裁决 | 独立规划评审 |
| 身份署名的批准 | developer/codeReviewer 对抗、自动测试与追踪证据 |
| | 按 DAG 并行执行边界清晰的任务 |
