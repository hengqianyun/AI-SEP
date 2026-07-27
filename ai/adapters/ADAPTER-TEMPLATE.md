# Runtime Adapter 填写模板

> 已填写示例：[ADAPTER-EXAMPLE.md](./ADAPTER-EXAMPLE.md)（仅粒度参考，非默认）
> 运行时负责人复制本结构创建或填写 `ai/adapters/<runtime>/` 说明文档。尖括号内容必须替换。  
> **Adapter 只映射控制协议**（start / resume / status / approve / cancel 等），不私藏业务规则；业务仍在 `ai/rules/`、`product/`、`policies.yaml`。

## 1. 元数据

```markdown
# <Runtime 名称> Adapter

| 字段 | 值 |
|---|---|
| Runtime | `<Cursor / CLI / CI / generic-agent / 其他>` |
| 状态 | `<draft / active / deprecated>` |
| Owner | `<角色或团队>` |
| 工作流版本 | `<与 ai/workflow 对齐>` |
| 演练 Run | `<RUN-ID 或待演练>` |
```

## 2. 入口映射

```markdown
## 支持入口

| 协议动作 | 本 Runtime 如何触发 | 输入 | 输出/状态落盘 |
|---|---|---|---|
| start | `<命令/会话约定>` | `<参数>` | `<路径>` |
| resume | `<命令/会话约定>` | `<参数>` | `<路径>` |
| status | `<命令/会话约定>` | `<参数>` | `<路径>` |
| approve | `<人工批准如何注入>` | `<身份 + 决策>` | `<路径>` |
| cancel | `<如何取消>` | `<参数>` | `<路径>` |
```

## 3. 状态、幂等与并发

```markdown
## 状态与安全

- 权威状态文件：`<路径；新会话仅凭落盘恢复>`
- 幂等键：`<生成方式；重复请求不重复派发>`
- 租约/并发保护：`<机制或 N/A + 风险>`
- 隔离模式引用：`<policies.yaml isolation；branch/worktree 等>`
- 人工审批接口：`<方式；须校验 owners.yaml 身份>`
```

## 4. 角色装载约定

```markdown
## 角色装载

- 主控会话：`<仅 orchestrator + start/resume 协议>`
- 专业角色：`<隔离 subagent / 进程；注入 agents + context-map>`
- 禁止：将 Organization/Project 业务规则只写在 UI 配置而无仓库副本
```

## 5. 实装记录（演练后填写）

```markdown
## Adapter 实装记录

- Runtime：`<名称>`
- 支持入口：`<已验证列表>`
- 工作流版本：`<版本>`
- 状态文件：`<路径>`
- 幂等键：`<方式>`
- 租约/并发保护：`<机制>`
- 人工审批接口：`<方式>`
- 演练 Run：`<RUN-ID>`
- 已知限制：`<限制或无>`
```

## 6. 完成检查

- [ ] Adapter 不含业务规则正文
- [ ] start / resume / status 至少经过一次演练
- [ ] 重复请求不重复派发
- [ ] 新会话可从状态文件恢复
- [ ] 与 `policies.yaml` isolation / release 批准身份一致
- [ ] Owner 将状态改为 `active`
