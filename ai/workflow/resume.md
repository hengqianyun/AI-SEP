# 恢复协议

新会话不得依赖上一会话聊天摘要。

## 步骤

1. 查找未完成的 `ai/runs/*/state.yaml`
2. 用户未指定且仅一个活动 Run → 自动选中；多个 → 只问 `runId`
3. 校验 workflow 版本、租约、制品完整性（能力具备时）
4. 读取 `currentNode`、`blockedBy`、`nextAction`
5. 不重复执行 `completedNodes`
6. 从 `nextAction` 继续；恢复事件追加到 `events.jsonl`

## 「继续」= 只做 nextAction

- 用户说「继续」时，**只执行**当前 `nextAction` 一步，然后汇报并等待。
- 禁止一次「继续」串联多个节点（例如 `PLAN_REVIEW` → 代写批准 → `TASK_DISPATCH`）。
- 若 `nextAction` 需要专业角色：主会话只负责调度隔离子实例并做门禁收回，不亲自写专业结论。

## 首条状态输出模板

```text
已恢复 {runId}。
当前节点：{currentNode}，第 {round} 轮。
阻塞问题：{n} 个。
下一动作：{role}/{action}。
```
