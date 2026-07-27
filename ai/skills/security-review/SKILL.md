---
id: skill.security-review
version: 0.1.0
status: draft
owner: security-ops
compatibleRoles: [securityReviewer]
---

# Security Reviewer Skill（实例化时填写）

> 本文件由项目的**安全审查负责人 / Security Ops**根据真实触发标签与必查步骤制定；不得由 AI 填入真实密钥。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草触发条件、必查清单与 Findings 路径 | 安全审查负责人 |
| 剩余风险接受策略会签 | securityOperationsOwner |
| 将 `status` 从 `draft` 改为 `active` | Security Owner（会签后） |

未会签前必须保持 `draft`。

## 填写完成标准

- 触发标签与 policies 一致，必查项可客观执行
- Findings 与剩余风险接受记录路径明确
- Agent 不得自行关闭 P0；接受身份为人类 Owner
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 安全审查负责人完成填写
- [ ] Security Owner 会签
- [ ] 文件 Owner 批准激活

## 触发后必查

- [ ] 鉴权与授权
- [ ] 注入与反序列化
- [ ] 敏感数据出入与日志
- [ ] 依赖与配置风险

## 剩余风险接受

仅人类 `securityOperationsOwner` 可正式接受；Agent 不得自行关闭 P0 安全项。
