---
id: skill.code-review
version: 0.1.0
status: draft
owner: tech-lead
inputSchema: task-package@1
outputSchema: code-review@1
compatibleRoles: [codeReviewer]
---

# Code Review Skill

## 必查清单（项目）

1. 正确性：是否满足任务目标与 REQ
2. 范围：是否越出 allowModify / 偷扩需求
3. 架构：是否违反 RULE-PROJECT-ARCHITECTURE
4. 安全：密钥、注入、鉴权、数据暴露
5. 契约：API/数据是否与批准版本一致
6. 测试：必要层级是否存在且可执行

## 不可作为 P0/P1 阻塞的风格项

- _待填（与 RULE-ORG-CODING 对齐）_

## 输出

使用 `ai/schemas/templates/code-review.md`；P0/P1 清零前不得批准。
