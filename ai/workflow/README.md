# 工作流定义与策略

填写模板：[`POLICIES-TEMPLATE.yaml`](./POLICIES-TEMPLATE.yaml)（供填写 `policies.yaml`）。

| 文件 | 用途 |
|---|---|
| `definition.yaml` | 节点、角色、通过条件（机器可读骨架） |
| `policies.yaml` | **项目个性化主文件**：超时、省略角色、风险触发、学习阈值 |
| `start.md` / `resume.md` | 启动与恢复协议（供 Adapter） |

业务规则不要写进 Adapter；改本目录或 `ai/rules/`。
