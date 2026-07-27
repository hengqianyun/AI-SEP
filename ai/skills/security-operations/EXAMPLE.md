# Security Operations Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认安全运营流程。以下触发条件均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.security-operations
version: 1.0.0
status: active
owner: security-ops
compatibleRoles: [securityOperations]
relatedRules: [RULE-ORG-SECURITY]
---

# Security Operations Skill

## 规划侧关注

- 是否触及鉴权模型、新公网端点、PII 存储/展示
- 密钥是否仅经批准密钥源；禁止示例中出现真实凭据
- 残留风险接受仅 `securityOperationsOwner` 可批

## 触发与升级

| 标签 | 动作 |
|---|---|
| auth-model-change | 强制 security-review + 双人确认 |
| new-public-endpoint | 强制 security-review；检查限流与鉴权 |
| handles-pii | 检查脱敏、日志禁止字段、访问审计 |

## 输出示例

- 计划评估：订单状态筛选不新增 PII 字段 → 不触发
- 若增加买家手机号展示 → 触发 `handles-pii`，要求脱敏与日志禁字段确认
```
