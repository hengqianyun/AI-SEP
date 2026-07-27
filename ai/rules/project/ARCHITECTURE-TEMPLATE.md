# RULE-PROJECT-ARCHITECTURE 填写模板

> 已填写示例：[ARCHITECTURE-EXAMPLE.md](./ARCHITECTURE-EXAMPLE.md)（仅粒度参考，非默认）
> Tech Lead / Solution Architect 复制本结构填写 `RULE-PROJECT-ARCHITECTURE.md`。尖括号内容必须替换。  
> 不预设部署形态或中间件；边界与不可破坏约定须可验证。

## 1. 元数据

```yaml
---
id: RULE-PROJECT-ARCHITECTURE
version: <语义化版本，如 1.0.0>
status: draft
owner: <techLead 或架构负责人>
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "integrationReviewer"]
reviewBy: <YYYY-MM-DD 或 TBD>
---
```

## 2. 系统边界

```markdown
## 系统边界

- 本系统负责：`<能力边界>`
- 本系统不负责：`<明确排除>`
- 外部依赖：`<系统及集成方式摘要；详细契约路径>`
- NFR 基线引用：`<路径或 N/A + 原因>`
```

## 3. 模块关系

```markdown
## 模块关系（摘要）

| 模块/边界 | 职责 | 允许依赖 | 禁止依赖 | 对外契约位置 | Owner |
|---|---|---|---|---|---|
| `<模块>` | `<职责>` | `<允许>` | `<禁止>` | `<仓库路径>` | `<角色>` |

```text
<用文本示意模块依赖；保持与上表一致>
```
```

## 4. 不可破坏约定

```markdown
## 不可破坏约定

- `ARCH-INV-<编号>`：`<必须始终成立的架构约束>`
  - 检查方式：`<测试/静态检查/评审步骤>`
  - 例外审批：`<角色与记录位置>`

示例类别（按项目取舍）：
- 同步调用禁忌 / 跨边界事务禁忌
- 数据所有权（谁可写哪类数据）
- 事件主题或消息命名
- 共享库变更协议
```

## 5. ADR 入口

```markdown
## ADR 入口

- 目录：`design/decisions/`（或组织规定的 ADR 路径：`<路径>`）
- 必须新增决策记录的条件：`<破坏性变更、跨模块契约、技术栈偏离等>`
- 新增破坏性变更必须先有 ADR，状态 `APPROVED` 后方可进开发任务
- 模板：见同目录 `DEC-TEMPLATE.md`
```

## 6. 完成检查

- [ ] 边界、模块关系和禁止依赖可由路径或检查规则验证
- [ ] 每条不可破坏约定有稳定 ID 和验证方式
- [ ] ADR 入口及触发条件明确
- [ ] 与 STACK / LAYOUT / DOMAIN 无矛盾；冲突已形成 ADR
- [ ] Owner 将 `status` 改为 `active`
