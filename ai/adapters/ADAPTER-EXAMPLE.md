# Cursor Adapter 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认 Runtime。以下命令与会话约定均是假设值，复制时必须按真实环境修改。

```markdown
# Cursor Adapter（acme-orders）

| 字段 | 值 |
|---|---|
| Runtime | Cursor IDE Agent |
| 状态 | active |
| Owner | maintainer |
| 工作流版本 | ai-software-delivery@1.0.0 |
| 演练 Run | RUN-ACME-20260725-01 |

## 支持入口

| 协议动作 | 本 Runtime 如何触发 | 输入 | 输出/状态落盘 |
|---|---|---|---|
| start | 用户在 Cursor Chat 发送「启动编排」+ 任务包路径 | `planning/tasks/TASK-<ID>.md` | `ai/runs/<runId>/state.yaml` |
| resume | 同线程发送「resume」或附带 `runId` | `runId`、可选 checkpoint | 更新 `ai/runs/<runId>/state.yaml` |
| status | 发送「status」或读取 state 文件 | `runId` | 终端回复 + `ai/runs/<runId>/state.yaml` |
| approve | Chat 中 maintainer 发送「approve release」并注明 identity | `approverIdentity=maintainer`、决策文本 | `ai/runs/<runId>/approvals.yaml` |
| cancel | 发送「cancel run <runId>」 | `runId`、原因 | `state.status=CANCELLED` |

## 状态与安全

- 权威状态文件：`ai/runs/<runId>/state.yaml`；新会话仅凭落盘恢复，不依赖 Chat 历史
- 幂等键：`runId` + `protocolAction` + `taskRevision`；重复 start 不重复派发子任务
- 租约/并发保护：同 `runId` 仅允许一个 active orchestrator 会话；二次 start 返回 `ALREADY_RUNNING`
- 隔离模式引用：`policies.yaml` → `isolation.mode=branch`；Agent 在 `feature/<TASK-ID>-*` 分支工作
- 人工审批接口：Chat 明文批准须包含 `identity`（对齐 `owners.yaml`）；maintainer 发布门控与 `release.approverIdentity` 一致

## 角色装载

- 主控会话：仅 orchestrator 处理 start/resume/status/approve/cancel
- 专业角色：Cursor subagent（Task 工具）按 `context-map.yaml` 注入 Skill + Rule；工作目录仓库根
- 禁止：将 RULE-ORG-* / RULE-PROJECT-* 正文只写在 Cursor Rules UI 而无 `ai/rules/` 仓库副本

## Adapter 实装记录

- Runtime：Cursor IDE Agent
- 支持入口：start、resume、status、approve、cancel（均已演练）
- 工作流版本：1.0.0
- 状态文件：`ai/runs/<runId>/state.yaml`
- 幂等键：`runId` + `protocolAction` + `taskRevision`
- 租约/并发保护：单 run 单 orchestrator 会话锁（文件锁 `ai/runs/<runId>/.lock`）
- 人工审批接口：Chat + `approvals.yaml` 落盘
- 演练 Run：RUN-ACME-20260725-01
- 已知限制：approve 暂无 SSO 校验，依赖 maintainer 人工核对 identity 字符串
```
