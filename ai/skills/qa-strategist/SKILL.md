---
id: skill.qa-strategist
version: 0.1.0
status: draft
owner: tech-lead
compatibleRoles: [qaStrategist]
---

# QA Strategist Skill（实例化时填写）

> 本文件由项目的**QA 策略负责人**根据真实分层与门禁制定，不由 AI 猜测覆盖策略。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草测试分层、强制触发与规划门禁 | QA 策略负责人 |
| 与 Tester / 实现 Skill 命令对齐 | QA + 实现负责人 |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 QA Owner |

未完成分层与强制触发前须保持 `draft`。

## 填写完成标准

- 每层均有可判定的强制触发条件（禁止“视情况”）
- P0 REQ 至少映射到一层可执行验证
- 发布门槛明确 `BLOCKED` 不计通过
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] QA 策略负责人完成填写
- [ ] Tester Skill 命令引用一致
- [ ] 文件 Owner 批准激活

## 可测试性检查

- 验收标准是否可观测、可自动化
- 是否缺少负向/边界场景
- 追踪：REQ → TEST 映射是否可建立

## 本项目测试分层策略

| 层 | 覆盖目标 | 备注 |
|---|---|---|
| unit | _待填_ | |
| integration | _待填_ | |
| e2e | _待填_ | 控制数量 |

## 覆盖门槛（建议写入计划）

- P0 REQ：_待填_
- 关键路径：_待填_
