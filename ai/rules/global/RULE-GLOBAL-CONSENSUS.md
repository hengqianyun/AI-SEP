---
id: RULE-GLOBAL-CONSENSUS
version: 1.0.0
status: active
owner: platform
override: forbidden
appliesTo:
  roles: ["orchestrator", "planEditor", "*"]
reviewBy: 2027-07-01
---

# 共识与升级底线

## 必须

- Agent 无法达成一致时升级人类，不得伪造 `APPROVE`
- 异议只能由提出者确认关闭
- 失败先写入 `ai/memory/`，不得单次失败直接晋升全局 Rule
- 规划/代码/安全等专业结论须由**隔离角色实例**落盘；主会话 `orchestrator` 不得代写多角色 `APPROVE` 冒充共识
- 用户「继续」仅推进当前 Run 的 `nextAction` 一步，不得跨节点追进度伪造门禁通过

## 禁止

- Orchestrator 改写专业角色评审结论
- 同一实例兼任同一任务的 `developer` 与 `codeReviewer`
- 主会话在同一上下文中生成整套 `REV-*` 并据此将计划标为 `APPROVED`
