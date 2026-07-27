# Agent 角色说明

> 来源：[`design.md`](../../design.md)（含 §5.1 默认运行模型）
>
> 本目录将架构中的角色拆成可独立装载的说明文档。运行时可将每份文档映射为 subagent、worker、独立会话或独立模型调用。
>
> **个性化不要改本目录骨架**：项目差异写入 [`../rules/`](../rules/)、[`../skills/`](../skills/)、[`../workflow/policies.yaml`](../workflow/policies.yaml)。接入勾选见 [`../../PROJECT-CUSTOMIZATION.md`](../../PROJECT-CUSTOMIZATION.md)。

## 默认运行模型

```text
用户 ↔ 唯一主会话（orchestrator）
         └── 按需 Task/subagent 装载本目录角色文档并执行
```

- 主会话只统筹：校验、调度、门禁、升级人类
- 专业角色不在主会话“换帽子”扮演；结论必须落盘
- 调度细则见 [orchestrator.md](./orchestrator.md) 的「Subagent 调度协议」
- `developer` 与 `codeReviewer` 必须是不同子实例

## 契约共性

每个角色都必须具备：

1. 唯一目标
2. 固定输入及其 Schema
3. 固定输出及其 Schema
4. 允许读写范围
5. 决策权
6. 禁止事项
7. 完成条件
8. 超时或无法判断时的升级方式

**硬约束**：同一 Agent 实例不得在同一任务中同时承担互相制衡的角色。若运行时资源有限，也必须使用隔离上下文分别执行，并保留独立输出。

## 角色总览

### 编排层

| 角色 ID | 文档 | 可否省略 |
|---|---|---|
| `orchestrator` | [orchestrator.md](./orchestrator.md) | 不可省略 |

### 规划委员会

| 角色 ID | 文档 | 可否省略 |
|---|---|---|
| `productAnalyst` | [product-analyst.md](./product-analyst.md) | 不可省略 |
| `solutionArchitect` | [solution-architect.md](./solution-architect.md) | 不可省略 |
| `uxUiPlanner` | [ux-ui-planner.md](./ux-ui-planner.md) | 可省略（须记原因） |
| `apiDataDesigner` | [api-data-designer.md](./api-data-designer.md) | 可省略（须记原因） |
| `qaStrategist` | [qa-strategist.md](./qa-strategist.md) | 不可省略 |
| `securityOperations` | [security-operations.md](./security-operations.md) | 可省略（须记原因） |
| `parallelPlanner` | [parallel-planner.md](./parallel-planner.md) | 不可省略 |
| `planEditor` | [plan-editor.md](./plan-editor.md) | 不可省略（共识循环必需） |

### 编码对抗与验证

| 角色 ID | 文档 | 何时启用 |
|---|---|---|
| `developer` | [developer.md](./developer.md) | 每个实现任务必需 |
| `codeReviewer` | [code-reviewer.md](./code-reviewer.md) | 每个实现任务必需，且不得与 developer 同实例 |
| `tester` | [tester.md](./tester.md) | 建议独立；验证必需测试 |
| `integrationReviewer` | [integration-reviewer.md](./integration-reviewer.md) | 并行束合并后必需 |
| `securityReviewer` | [security-reviewer.md](./security-reviewer.md) | 高风险任务 |
| `migrationReviewer` | [migration-reviewer.md](./migration-reviewer.md) | 含数据迁移的高风险任务 |

### 人类决策身份

| 身份 ID | 文档 | 职责摘要 |
|---|---|---|
| `productOwner` 等 | [human-owners.md](./human-owners.md) | 业务裁决、架构升级、高风险发布、最终集成与发布批准 |

## 规划评审决策枚举

规划委员会专业角色（除 Plan Editor 汇总修订外）的评审输出仅允许：

| 决策 | 含义 |
|---|---|
| `APPROVE` | 本角色责任范围内无阻塞问题 |
| `REQUEST_CHANGES` | 存在可修复问题，须列明关闭条件 |
| `BLOCK` | 前提缺失、需求冲突或风险不可接受 |
| `ABSTAIN` | 该领域不适用；须 Orchestrator 验证，不等于默认批准 |

## 协作顺序（摘要）

```text
PRD
  → productAnalyst（需求快照）
  → planEditor（候选计划）
  → 各专业角色独立评审
  → orchestrator 收集异议 / 门禁
  → parallelPlanner（任务 DAG）
  → developer ↔ codeReviewer
  → tester
  → integrationReviewer（并行束）
  → 发布门禁 / 人类批准
```

完整流程与状态机见 [`design.md`](../../design.md) 第 5.1、7、8、9、18 章。
