# Runtime Adapters

将控制协议映射到具体工具。**业务规则仍在 `ai/rules/`**，Adapter 只做启动/恢复/调度翻译。

## 计划中的适配器

| 目录 | 用途 | 状态 |
|---|---|---|
| `cursor/` | Cursor Rules / Skills / 主会话=orchestrator | 待按项目填写 |
| `cli/` | `ai-flow start|resume|status|approve` | 待实现 |
| `ci/` | PR / 流水线触发 | 待实现 |
| `generic-agent/` | 其他支持多 Agent 的运行时 | 待实现 |

## Cursor 映射提示（填写时）

1. 主会话只加载 orchestrator + `ai/workflow/start.md` / `resume.md`
2. 专业角色用隔离 subagent，注入 `ai/agents/{role}.md` + context-map 中的 skill/rule
3. 不要把 Organization/Project 业务规则只写在 Cursor UI 配置里而无仓库副本
