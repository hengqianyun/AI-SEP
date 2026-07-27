# 启动协议

供 Runtime Adapter / 人类操作。不要每次粘贴完整角色提示词。

## 前置检查

1. 打开 [`PROJECT-CUSTOMIZATION.md`](../../PROJECT-CUSTOMIZATION.md)，确认 §0 完成判定或明确「试点豁免」范围
2. 存在可读的 PRD 路径
3. `ai/workflow/policies.yaml` 中 `projectId` 已替换

## 启动步骤

1. 创建 `runId`：`RUN-{PROJECT}-{NNN}`
2. 在 `ai/runs/{runId}/` 写入 `manifest.yaml`、`state.yaml`（`currentNode: PRD_ANALYSIS`）
3. 主会话仅扮演 `orchestrator`，按 `definition.yaml` + `policies.yaml` 调度
4. 子角色装载：`ai/agents/{role}.md` + `context-map.yaml` 中的 rules/skills
5. 所有专业结论落盘，不依赖聊天上下文作为权威状态

## 最小用户输入

```text
启动交付流程。
PRD：product/prd/<file>.md
（可选）run 备注：...
```
