# Security Review Skill 填写模板

> 安全审查负责人复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> 本 Skill 用于风险触发后的安全审查；**不得写入真实密钥或生产敏感数据**。  
> 剩余风险接受仅人类 `securityOperationsOwner` 可正式批准。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.security-review
version: 1.0.0
status: draft
owner: <securityOperationsOwner 或安全审查负责人>
compatibleRoles: [securityReviewer]
relatedRules:
  - RULE-ORG-SECURITY
  - <其他适用 Rule ID>
relatedSkills:
  - skill.security-operations
relatedRiskTriggers:
  - <触发本审查的 policies.yaml 标签>
---
```

## 2. 适用范围与触发

```markdown
## 适用范围

- 适用路径：`<glob 列表>`
- 触发后必审：匹配 riskTags 或安全规划升级的变更
- 协作输入：`skill.security-operations` 资产清单与分级

## 触发条件（与 policies 对齐）

| riskTag / 条件 | 必需证据 | 必需评审角色 |
|---|---|---|
| auth-model-change | `<证据>` | `<角色>` |
| new-public-endpoint | `<证据>` | `<角色>` |
| handles-pii | `<证据>` | `<角色>` |
| `<项目标签>` | `<证据>` | `<角色>` |
```

## 3. 审查工作流

```markdown
## 审查工作流

1. 确认触发标签、变更路径与关联 REQ。
2. 按必查清单逐项核验，保存命令/人工检查证据。
3. 对照数据分级与密钥红线；发现问题写入 Findings。
4. P0/P1 未关闭前不得批准合并/发布。
5. 剩余风险仅可提交人类 Owner 接受，Agent 不得自行关闭。
```

## 4. 触发后必查清单

```markdown
## 触发后必查

- [ ] 鉴权与授权（越权、默认允许、令牌处理）
- [ ] 注入与反序列化
- [ ] 敏感数据出入与日志（脱敏、禁止字段）
- [ ] 依赖与配置风险
- [ ] 密钥来源与仓库污染检查
- [ ] `<项目附加项>`
```

```markdown
## 执行工具与命令

| 检查项 | 命令或人工步骤 | 通过标准 | 证据路径 |
|---|---|---|---|
| `<项>` | `<命令/步骤>` | `<标准>` | `<路径>` |
```

## 5. Findings 与剩余风险

```markdown
## Findings

- 输出路径：`<路径>`
- 级别：`<P0/P1/P2 定义引用>`
- 每条必含：影响、复现/证据、修复建议、Owner

## 剩余风险接受

- 仅人类 `securityOperationsOwner` 可正式接受
- Agent 不得自行关闭 P0 安全项
- 接受记录路径：`<路径>`
- 接受记录最低字段：`<风险描述、理由、期限、批准人、日期>`
```

## 6. 输出格式

```markdown
## 输出格式

- 结论：`<APPROVE / REQUEST_CHANGES / BLOCK>`
- 触发标签：`<列表>`
- Findings：`<路径或摘要>`
- 未关闭 P0/P1：`<列表>`
- 提交接受的剩余风险：`<无，或待 Owner 签字项>`
```

## 7. 激活检查

- [ ] 必查项与触发标签可客观执行
- [ ] 工具/步骤与证据路径明确，或有 N/A 原因
- [ ] 剩余风险接受身份与记录路径明确
- [ ] Security Owner 会签后将 `status` 改为 `active`
