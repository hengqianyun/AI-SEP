---
id: skill.tester
version: 0.1.0
status: draft
owner: tech-lead
outputSchema: test-evidence@1
compatibleRoles: [tester]
---

# Tester Skill

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
