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

## 禁止

- Orchestrator 改写专业角色评审结论
- 同一实例兼任同一任务的 `developer` 与 `codeReviewer`
