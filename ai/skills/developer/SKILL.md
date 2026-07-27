---
id: skill.developer
version: 0.1.0
status: draft
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
---

# Developer Skill（通用基线，实例化时填写）

> 本文件只承载所有 Developer 共用的执行底线（边界、验证、输出）。前端、后端等技术栈做法应拆成独立 Skill，并通过 `context-map.yaml` 按路径装载；**不在此填写具体框架或命令栈**。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）
> - 前端：[`../frontend-developer/SKILL.md`](../frontend-developer/SKILL.md)（模板见 [`FRONTEND-TEMPLATE.md`](../frontend-developer/FRONTEND-TEMPLATE.md)）
> - 后端：由下游项目按相同模式创建 `skill.backend-developer`

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草通用边界、验证结果规则与输出格式 | Tech Lead / 工程负责人 |
| 确认与独立实现 Skill、context-map 装载策略一致 | Tech Lead |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

## 填写完成标准

- 写边界、`BLOCKED ≠ PASSED`、输出格式完整
- 明确指向路径装载的实现 Skill，而非在此写入栈细节
- “必须”和“禁止”可由 reviewer/tester 客观检查
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] Tech Lead 完成通用基线填写
- [ ] 与 frontend/backend 等独立 Skill 分工清晰
- [ ] 文件 Owner 批准激活

## 通用执行要求

- 读取任务包、关联 REQ、冻结契约和本任务按路径装载的技术栈 Skill
- 只修改 `allowModify` 内的文件；范围不足时升级，不得自行扩大
- 执行技术栈 Skill 规定的全部适用验证命令
- 不得通过关闭检查、跳过测试、硬编码环境或提交密钥掩盖问题
- 环境阻塞必须记录为 `BLOCKED`，不得宣称验证通过

## 提交审查前自检

- [ ] 变更在 `allowModify` 内
- [ ] 每个变更声明 REQ
- [ ] 已装载与修改路径匹配的实现 Skill
- [ ] 适用的自测命令已执行并记录结果摘要
