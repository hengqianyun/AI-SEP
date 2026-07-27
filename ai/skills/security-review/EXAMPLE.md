# Security Review Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认安全审查标准。以下检查项均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.security-review
version: 1.0.0
status: active
owner: security-ops
compatibleRoles: [securityReviewer]
relatedRules: [RULE-ORG-SECURITY, RULE-ORG-CODING]
---

# Security Review Skill

## 必查项

- 鉴权/授权是否经统一 `SecurityFilterChain`，有无端点漏配
- 新公网 API 是否有鉴权、限流、输入校验
- 日志/响应是否含禁止字段（密码、完整证件号、原始 Token）
- 密钥是否入仓或写进示例文件
- 前端是否把敏感数据写入 localStorage（无 ADR 则否）

## 结论级别

- `BLOCK`：P0 安全缺陷未解
- `CONDITIONAL`：可合并但须限定环境/时间窗并有 Owner
- `PASS`：无开放 P0/P1 安全项

## 输出示例

- 任务：`TASK-ORDER-014`
- 结论：`PASS`
- 说明：仅增加枚举筛选，无新端点、无 PII 变化
```
