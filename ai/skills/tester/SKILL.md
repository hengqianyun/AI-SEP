---
id: skill.tester
version: 0.1.0
status: draft
owner: tech-lead
outputSchema: test-evidence@1
compatibleRoles: [tester]
---

# Tester Skill（实例化时填写）

> 本文件由项目的**测试负责人或测试团队**根据真实环境与命令制定，不由 AI 猜测测试栈。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）
>
> **环境不可用时记 `BLOCKED`，绝不可写成 `PASSED`。**

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草环境、命令矩阵与证据字段 | 测试负责人 / 测试团队 |
| 实际试跑命令并确认判定规则 | 测试团队 |
| 与 QA Strategist / 实现 Skill 对齐 | QA + 实现负责人 |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 QA Owner |

若命令尚未在本仓库可复制执行，本文件必须保持 `draft`。

## 填写完成标准

- 工作目录、依赖服务、Fixture 与命令矩阵完整
- `PASSED` / `FAILED` / `BLOCKED` / `SKIPPED` 互斥且可审计
- 证据模板字段可复现；`blockedIsPass` 等价为 false
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 测试负责人完成填写并实际试跑
- [ ] QA Owner 确认证据与门禁
- [ ] 文件 Owner 批准激活

## 环境

- 依赖服务：_待填_
- 测试账号 / fixture：_待填（禁止真实生产数据）_

## 命令（按层）

| 层 | 命令 | 何时强制 |
|---|---|---|
| lint/type | _待填_ | 每任务 |
| unit | _待填_ | 每任务 |
| integration | _待填_ | _待填条件_ |
| e2e | _待填_ | _待填条件_ |

## 证据

- 环境阻塞 → 状态 `BLOCKED`，不得记为通过
- 输出使用 `ai/schemas/templates/test-evidence.md`，引用 REQ
