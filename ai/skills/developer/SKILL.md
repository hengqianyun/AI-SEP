---
id: skill.developer
version: 0.1.0
status: draft
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
---

# Developer Skill（通用基线）

> 本文件只承载所有 Developer 共用的执行底线。前端、后端等技术栈做法应拆成独立 Skill，并通过 `context-map.yaml` 按路径装载。
>
> - 前端：[`../frontend-developer/SKILL.md`](../frontend-developer/SKILL.md)
> - 后端：由下游项目按相同模式创建 `skill.backend-developer`

## 填写完成标准

- 所有命令均可从仓库根目录直接复制执行，或明确写出工作目录
- 每条实现约定都能指向真实目录、配置文件或现有代码范例
- “必须”和“禁止”可以由 reviewer/tester 客观检查，不写“保持高质量”等空泛要求
- 已覆盖安装、静态检查、类型检查、单元测试、构建和本地启动；不适用项明确写 `N/A` 及原因
- 文件 Owner 已人工复核，且不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

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
