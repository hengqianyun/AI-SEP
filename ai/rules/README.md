# Rules 分层说明

> 对应 `design.md` §11。优先级：`Task > Project > Organization > Global`。  
> 低层不得覆盖高层标注为 `override: forbidden` 的安全/合规规则。  
> 填写模板：`organization/*-TEMPLATE.md`、`project/*-TEMPLATE.md` / `*-TEMPLATE.yaml`（与目标交付物同目录）。

## 目录

| 目录 | 维护者 | 内容 |
|---|---|---|
| `global/` | 平台治理 | 与组织/项目无关的工程底线 |
| `organization/` | 架构委员会 | 技术栈、编码标准、合规 |
| `project/` | 本项目团队 | 目录地图、领域规则、装载映射 |

## 规则文件约定

- 文件名：`RULE-{SCOPE}-{TOPIC}.md`（SCOPE = GLOBAL / ORG / PROJECT）
- 文首 YAML front matter 必填（见各层模板）
- 状态：`draft` → `active` → `deprecated` → `retired`
- 变更须保留版本与 `rollbackTo`；禁止仅在聊天中改规范

## 装载

Orchestrator 按角色 + REQ + 路径，结合 `project/context-map.yaml` 装载**最小必要**规则；每次 Run 记录实际加载的规则 ID 与版本。
