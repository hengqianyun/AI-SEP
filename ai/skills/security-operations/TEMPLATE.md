# Security Operations Skill 填写模板

> 安全/运维规划负责人复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> 本 Skill 用于规划期安全与运维约束；**不得写入真实密钥、生产凭据或敏感样本**。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.security-operations
version: 1.0.0
status: draft
owner: <securityOperationsOwner 或等价角色>
compatibleRoles: [securityOperations]
relatedRules:
  - RULE-ORG-SECURITY
  - <其他适用 Rule ID>
relatedRiskTriggers:
  - <policies.yaml 标签列表>
---
```

- 激活前须由 Security/Ops Owner 会签；未会签保持 `draft`。

## 2. 适用范围与前置条件

```markdown
## 适用范围

- 适用路径：`<glob 列表>`
- 触发 riskTags：`<与 policies.yaml 一致的标签>`
- 规划期关注：威胁面、密钥与配置、监控与回滚
- 冲突处理：与 RULE-ORG-SECURITY 冲突 → `BLOCK` 或升级 Owner

## 前置条件

- 敏感资产仅写标识与类别，不写秘密值
- P0 剩余风险仅人类 `securityOperationsOwner` 可接受
```

## 3. 规划期工作流

```markdown
## 规划期检查工作流

1. 识别新入口、权限模型变化、第三方依赖与数据处理。
2. 对照敏感资产清单与数据分级。
3. 核对密钥来源、配置注入、监控与回滚是否可执行。
4. 匹配 riskTags，声明必需评审与证据。
5. 输出规划结论；红线冲突则 `BLOCK`。
```

## 4. 敏感资产与数据分级

```markdown
## 本项目敏感资产

| 资产标识 | 类别 | 允许位置/处理 | 禁止行为 | Owner |
|---|---|---|---|---|
| `<表/接口/密钥名标识>` | `<类别>` | `<方式>` | `<禁止>` | `<角色>` |
```

```markdown
| 数据/能力类别 | 分级 | 允许位置/处理方式 | 禁止行为 | 自动/人工检查 | 触发安全评审条件 |
|---|---|---|---|---|---|
| `<类别>` | `<级别>` | `<方式>` | `<禁止>` | `<命令/流程>` | `<条件>` |
```

## 5. 密钥、配置与运维

```yaml
secrets:
  approvedSources: ["<来源>"]
  forbiddenLocations: ["<仓库路径/产物类型>"]
  rotationAndRevocation: "<流程>"
ops:
  configInjection: "<方式；无真实值>"
  monitoringEntry: "<仪表盘/告警入口或 N/A>"
  rollbackAuthority: "<角色>"
  emergencyProcedure: "<路径或流程>"
```

## 6. 必查清单与工具

```markdown
## 必查清单

- [ ] 鉴权/授权模型变化已识别
- [ ] 注入面与反序列化风险已评估（规划级）
- [ ] 日志与配置不含密钥/敏感明文策略已声明
- [ ] 依赖与第三方集成风险已标注
- [ ] 监控、告警与回滚责任明确

## 执行工具与命令

| 目的 | 命令或流程 | 触发条件 | 证据路径 |
|---|---|---|---|
| `<扫描/检查>` | `<命令或 N/A>` | `<条件>` | `<路径>` |
```

```yaml
securityReviewTriggers:
  - id: "<稳定标识>"
    condition: "<可判定的变更条件>"
    requiredReviewer: "<角色>"
    requiredEvidence: ["<证据类型>"]
```

## 7. Findings 与剩余风险

```markdown
## Findings 输出

- Findings 输出路径：`<路径>`
- 严重级别定义：`<引用或简表>`
- P0 剩余风险批准身份：`securityOperationsOwner`
- Agent 不得自行关闭 P0 安全项或接受剩余风险
```

## 8. 输出格式

```markdown
## 输出格式

- 结论：`<PASS / REQUEST_CHANGES / BLOCK>`
- 威胁面摘要：`<列表>`
- 必需 riskTags / 评审：`<列表>`
- Findings 路径：`<路径或无>`
- 运维缺口：`<监控/回滚/密钥等>`
```

## 9. 激活检查

- [ ] Security/Ops Owner 已会签
- [ ] 无真实密钥或生产敏感示例
- [ ] 触发标签与 `policies.yaml` 一致
- [ ] Owner 将 `status` 改为 `active`
