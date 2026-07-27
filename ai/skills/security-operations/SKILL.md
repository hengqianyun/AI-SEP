---
id: skill.security-operations
version: 0.1.0
status: draft
owner: security-ops
compatibleRoles: [securityOperations]
---

# Security / Operations（规划）Skill（实例化时填写）

> 本文件由项目的**Security/Ops Owner**根据真实敏感资产、密钥策略与 riskTags 制定；不得由 AI 填入真实密钥。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草敏感资产、分级、密钥与运维约束 | Security/Ops Owner |
| 与 RULE-ORG-SECURITY / policies riskTriggers 对齐 | Security + Tech Lead |
| 将 `status` 从 `draft` 改为 `active` | Security/Ops Owner（会签后） |

未会签前必须保持 `draft`。

## 填写完成标准

- 敏感资产仅含标识与类别，无秘密值
- 触发标签与 `policies.yaml` 一致
- P0 剩余风险批准身份明确为人类 Owner
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] Security/Ops Owner 完成填写并会签
- [ ] 无真实密钥或生产敏感示例
- [ ] 文件 Owner 批准激活

## 规划期检查

- 威胁面：新入口、权限模型、第三方依赖
- 运维：密钥、配置、监控、回滚
- 与 RULE-ORG-SECURITY 冲突 → `BLOCK` 或升级 Owner

## 本项目敏感资产

- _待填：表、接口、密钥名（勿写真实值）_
