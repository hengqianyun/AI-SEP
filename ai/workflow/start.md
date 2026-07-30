# 启动协议

供 Runtime Adapter / 人类操作。不要每次粘贴完整角色提示词。

## 前置检查

1. 打开 [`PROJECT-CUSTOMIZATION.md`](../../PROJECT-CUSTOMIZATION.md)，确认 §0 完成判定或明确「试点豁免」范围
2. 存在可读的 PRD 路径
3. `ai/workflow/policies.yaml` 中 `projectId` 已替换

## 启动步骤

1. 创建 `runId`：`RUN-{PROJECT}-{NNN}`（序号递增；**勿复用已结束 Run**）
2. 在 `ai/runs/{runId}/` 写入 `manifest.yaml`、`state.yaml`（默认 `currentNode: PRD_ANALYSIS`）
3. 主会话仅扮演 `orchestrator`，按 `definition.yaml` + `policies.yaml` 调度
4. 子角色装载：`ai/agents/{role}.md` + `context-map.yaml` 中的 rules/skills
5. 所有专业结论落盘，不依赖聊天上下文作为权威状态

## 新 PRD 版本 ⇒ 新 Run（强制）

每当**明确新的 PRD 版本**（新文件如 `wsc-v1.1.md`，或元数据版本字段升级）时：

1. **必须**起草新 Run，并在 `manifest.prd` / `artifacts.prd` 指向该版本文件
2. 建议 `manifest.basedOnRun` 指向上一完成 Run（若有）
3. 新建对应需求快照（新 `SNAP-*`）；不得把旧 Run 的 `activeSnapshotId` 偷换成新快照继续跑
4. 已 `COMPLETED` 的 Run：`resume` 只报告完成态，**不**为新版本派发任务

## 最小用户输入

```text
启动交付流程。
PRD：product/prd/<file>.md
（可选）run 备注：...
```

新版本示例：

```text
启动交付流程。
PRD：product/prd/wsc-v1.1.md
（可选）基于 Run：RUN-WSC-001
```
