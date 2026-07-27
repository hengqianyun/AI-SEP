# ai/ — Agent 控制与知识面

| 目录 | 作用 | 个性化 |
|---|---|---|
| `agents/` | 角色契约（稳定） | 一般不改 |
| `rules/` | Global / Org / Project 规则 | **必填 Org + Project** |
| `skills/` | 按栈的可执行做法 | **必填核心 Skill** |
| `schemas/` | 制品校验与模板 | 按项目微调 |
| `workflow/` | 状态机与 policies | **必填 policies** |
| `adapters/` | 工具映射 | 后置 |
| `runs/` | 运行时状态（执行产生） | 勿手改权威历史 |
| `memory/` / `learning/` | 失败记忆与晋升 | 运行后产生 |

接入新项目：从仓库根目录 [`PROJECT-CUSTOMIZATION.md`](../PROJECT-CUSTOMIZATION.md) 勾选；各 Skill 填写见同目录 `TEMPLATE.md`（前端见 `FRONTEND-TEMPLATE.md`）。
