# 审计无明文口令 — 审查清单（TASK-WSC-602）

testScope 允许「审计日志无明文口令（或审查清单勾选证据）」。

| # | 检查项 | 结果 | 证据 |
|---|---|---|---|
| 1 | `AuthAuditLogger.loginFailed` 签名仅 username/reason/correlationId | **PASS** | `AuthAuditLogger.java` L18–24；注释「永不记录 password / passwordHash」 |
| 2 | 登录失败审计日志格式无 password 字段 | **PASS** | `01-backend-security.txt`：`event=AUTH_LOGIN_FAILED username=tmp602 reason=BAD_CREDENTIALS`（及 admin BAD_CREDENTIALS）；无口令字段 |
| 3 | 创建用户口令 `secret602` 不出现在响应体 | **PASS** | `adminUsers_crudAndSoftDelete_noPasswordLeak`：`content().string(not(containsString("secret602")))`；`$.data.password` / `passwordHash` doesNotExist |
| 4 | 列表/更新响应不含 password / passwordHash 字符串 | **PASS** | 同方法 `not(containsString("password"))` / `passwordHash` |
| 5 | 类级文档禁止明文口令 | **PASS** | `AuthAuditLogger` javadoc：「禁止记录明文口令 / passwordHash」 |

结论：勾选通过；未在 `wsc.audit` 日志中观察到明文口令落盘。
