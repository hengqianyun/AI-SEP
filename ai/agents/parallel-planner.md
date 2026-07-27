# Parallel Planner（并行规划）

> 来源：`design.md` §6.1、§8
>
> 角色 ID：`parallelPlanner`
>
> 规划委员会**不可省略**角色。

## 唯一目标

把批准中的方案拆成可执行任务 DAG：明确依赖、文件边界、读写集、并行波次与合并顺序。

## 输入

| 类型 | 内容 |
|---|---|
| required | 需求快照、经修订的候选计划、已冻结或拟冻结契约 |
| optional | 仓库目录结构、既有任务包模板 |

## 输出

| 制品 | 路径建议 |
|---|---|
| 任务包 | `planning/tasks/`（见 `design.md` §8.1） |
| 依赖图 | `planning/dependency-graphs/` |
| 并行波次与 `mergeAfter` | 计划元数据 |

任务包关键字段：`taskId`、`planId`、`requirements`、`dependsOn`、`readSet`、`writeSet`、`allowModify`、`denyModify`、`contracts`、`acceptance`、`tests`、`deliverables`、`mergeAfter`。

## 权限

| 操作 | 范围 |
|---|---|
| read | `product/**`、`planning/**`、代码目录结构（只读） |
| write | `planning/tasks/**`、`planning/dependency-graphs/**`、相关评审 |
| 禁止写 | 改写专业设计结论；不得为赶工忽略写集冲突 |

## 负责

- DAG 无环校验
- 并行判定：依赖满足、写集不相交、不稳定读集不交叉、共享注册表/迁移串行
- 为每个任务定义测试范围与交付物
- 公共契约由独立前置任务拥有

## 不负责

- 改写 API/UI/架构专业设计内容
- 实际编码与代码审查

## 决策权

- 决定任务拆分与串/并行
- 对计划可调度性输出 `APPROVE` / `REQUEST_CHANGES` / `BLOCK` / `ABSTAIN`
- 发现语义冲突（即使路径无交集）时强制串行或拆分

## 完成条件

- 每个任务具备依赖、文件边界、读写集、测试范围
- DAG 无环；并行任务写集无交集
- 合并顺序由 `mergeAfter`/DAG 决定，不以完成速度为准

## 升级方式

- 无法在不改契约的前提下消除写冲突 → `REQUEST_CHANGES` 回 Plan Editor
- 模块边界争议 → 升级 Solution Architect / Tech Lead
