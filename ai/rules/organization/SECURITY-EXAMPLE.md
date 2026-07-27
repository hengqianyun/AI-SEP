# RULE-ORG-SECURITY 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认安全基线。以下流程与路径均是假设值，复制时必须按真实组织策略修改。

```markdown
---
id: RULE-ORG-SECURITY
version: 1.0.0
status: active
owner: securityOperationsOwner
override: forbidden
appliesTo:
  roles: ["*"]
reviewBy: 2026-07-15
---

# RULE-ORG-SECURITY

## 数据分级

| 级别 | 含义 | 允许位置/处理方式 | 禁止行为 | 自动/人工检查 |
|---|---|---|---|---|
| PUBLIC | 公开商品目录 | CDN、公开 API | 混入用户数据 | CI OpenAPI 扫描 |
| INTERNAL | 订单号、内部 userId | 应用库、受控日志 | 写入公开 issue | 代码审查 |
| CONFIDENTIAL | 手机号、收货地址 | 加密列或列级 ACL；传输 TLS 1.2+ | 明文日志、导出到 Slack | securityReviewer + 脱敏测试 |
| RESTRICTED | 密码、JWT 密钥、DB 凭据 | 密钥管理系统 / CI Secret | 任何 Git 路径、截图、任务包正文 | secret scan（gitleaks） |

## 必须

- 密码/凭据存储：仅 bcrypt 哈希存库；原始密码不得落盘、不得进日志
- PII / 敏感个人信息：手机号展示脱敏（`138****8000`）；导出须 maintainer + securityOperationsOwner 双批
- 支付或高敏数据：V1 无真实支付；模拟支付字段不得存卡号
- 审计日志保留：状态变更与登录失败保留 180 天；访问仅限 securityOperationsOwner 指定账号
- 密钥来源与轮换：生产 JWT 密钥来自 Vault `secret/acme-orders/prod/jwt`；轮换周期 90 天；泄露 1h 内吊销

## 密钥与秘密

- 批准来源：HashiCorp Vault（生产/预发）；本地 `backend/.env.local`（从 `backend/.env.example` 复制）
- 禁止位置：Git 仓库、`ai/runs/` 任务包正文、PR 描述、终端截图
- 本地开发：从 `backend/.env.example`、`frontend/.env.example` 创建本地文件；`.gitignore` 已覆盖；禁止提交 `*.local`
- 泄露响应：1）吊销密钥 2）轮换 3）通知 securityOperationsOwner（wang@example.com）4）记录 `temp/ai/audit/INCIDENT-<ID>.md`

## 触发 Security Reviewer 的条件

| ID | 条件 | 必需评审角色 | 必需证据 |
|---|---|---|---|
| SEC-TRIG-001 | 鉴权/授权模型变更（角色、JWT claim、guard） | securityReviewer | 威胁建模摘要 + 测试用例 |
| SEC-TRIG-002 | 新增对外公开 API（无 auth） | securityReviewer | OpenAPI 安全方案说明 |
| SEC-TRIG-003 | 新增/变更 PII 字段存储或展示 | securityReviewer | 数据分级表更新 + 脱敏截图 |
| SEC-TRIG-004 | 引入 OWASP 高风险依赖 | securityReviewer | dependency-check 报告 |
```
