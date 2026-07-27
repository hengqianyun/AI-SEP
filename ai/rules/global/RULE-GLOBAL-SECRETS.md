---
id: RULE-GLOBAL-SECRETS
version: 1.0.0
status: active
owner: platform
override: forbidden
appliesTo:
  roles: ["*"]
  paths: ["**"]
reviewBy: 2027-07-01
---

# 密钥与敏感信息

## 必须

- 不得将密钥、令牌、私钥、生产连接串写入仓库、日志或 Agent 输出
- 发现疑似泄露时立即 `BLOCK` / `ESCALATED`，不得仅口头提醒后继续

## 禁止

- 在示例中使用真实凭证
- 将 `.env` 中的秘密提交到 git
