---
id: RULE-GLOBAL-E2E-GATE
version: 1.0.0
status: active
owner: qa
override: forbidden
appliesTo:
  roles: ["orchestrator", "developer", "tester", "*"]
reviewBy: 2027-08-24
---

# E2E 测试门禁硬约束

## 背景

RUN-WSC-012 中 E2E 测试（TASK-WSC-919）因前端/后端服务未启动，导致 `net::ERR_CONNECTION_REFUSED`，10 个测试中 9 个 skipped、1 个 error，但仍被标记为「已完成」。后续手动启动服务后发现大量运行时错误（表名错误、H2 兼容性、缺失端点、proxy cookie 等），说明 E2E 门禁形同虚设。

## 必须（硬约束）

### 1. E2E 报告零容忍校验

E2E 测试报告必须同时满足以下条件，否则不得标记任务为 VERIFIED / COMPLETED：

| 指标 | 要求 |
|---|---|
| `errors` | = 0 |
| `failures` | = 0 |
| `skipped` | = 0（不允许跳过任何用例） |
| 每个 `<testcase>` 状态 | 必须为 `passed`（不存在 `<skipped>` 或 `<error>` 标签） |

### 2. 环境前置检查

执行 E2E 前必须验证：

- [ ] 前端 dev server 已启动且可访问（`curl -s -o /dev/null -w '%{http_code}' $BASE_URL/login` 返回 200）
- [ ] 后端 API 已启动且健康（`curl -s $BASE_URL/api/v1/actuator/health` 返回 200 或 session 接口返回非 5xx）
- [ ] 数据库迁移已完成（后端日志包含 `Successfully applied N migrations`）

任一前置条件不满足时，**禁止执行 E2E 命令**，应先启动服务再跑测试。

### 3. 报告解析义务

子 agent / orchestrator 在标记 E2E 任务完成前，必须：

1. 读取 JUnit XML 报告文件
2. 解析 `<testsuites>` 的 `tests`、`failures`、`skipped`、`errors` 属性
3. 逐个 `<testcase>` 检查是否存在 `<skipped>` 或 `<error>` 标签
4. 仅当全部通过时才标记 COMPLETED；否则标记 BLOCKED 并附上失败原因

### 4. 禁止自欺式验收

- 禁止仅因为「命令执行了」或「报告文件存在」就标记 E2E 通过
- 禁止忽略 `skipped` 用例——每个 skipped 都意味着测试未执行
- 禁止在没有前端+后端运行的环境下执行 E2E（mock server 不算）

## 禁止

- 跳过环境前置检查直接执行 E2E
- 将 `errors > 0` 或 `skipped > 0` 的报告标记为通过
- 子 agent 自行声称 E2E 通过而不解析报告内容
- 在 orchestrator 主会话中代写 E2E 通过结论（必须由隔离 tester 角色落盘）
