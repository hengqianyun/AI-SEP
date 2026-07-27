# Parallel Planner Skill 填写模板

> 并行规划负责人复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> 本 Skill 只写任务拆分、写集与合并顺序做法；模块业务含义以 `modules.yaml` / 产品定义为准。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.parallel-planner
version: 1.0.0
status: draft
owner: <并行规划负责人或 Tech Lead>
compatibleRoles: [parallelPlanner]
relatedRules:
  - RULE-PROJECT-LAYOUT
  - <其他适用 Rule ID>
relatedConfigs:
  - modules.yaml
---
```

## 2. 适用范围与前置条件

```markdown
## 适用范围

- 模块与路径来源：`modules.yaml`、RULE-PROJECT-LAYOUT
- 共享契约路径：`<glob 列表>`
- 冲突升级角色：`<角色>`

## 前置条件

- 模块 `code`/`name`/`ownerRole` 已由产品侧确认
- 路径 glob 来自真实仓库，不使用假定目录
```

## 3. 规划工作流

```markdown
## 规划工作流

1. 读取需求、模块边界与共享路径规则。
2. 先契约/共享任务，后消费者实现任务。
3. 为每任务声明读写集到文件或目录粒度。
4. 检测写集冲突；有语义冲突则串行或拆分共享任务。
5. 产出 `allowModify`、依赖与 `mergeAfter` 合并顺序。
```

## 4. 模块边界与写集

```markdown
## 模块边界（写集提示）

| 模块代码 | 可修改路径 glob | 只读/共享路径 | 禁止并行写入 | Owner | 冲突升级 |
|---|---|---|---|---|---|
| `<MODULE-CODE>` | `<glob>` | `<路径或 N/A>` | `<路径或 N/A>` | `<角色>` | `<角色>` |

### 通常不能与消费者任务并行写入的路径

- `<共享契约、公共 schema、路由注册表等真实路径>`
- 修改条件：`<锁定 / 协调 / 专属任务>`
```

```yaml
sharedContracts:
  - path: "<共享契约目录/glob>"
    ownerRole: "<角色>"
    parallelWrite: "forbidden"
    changeProtocol: "<锁定/协调/评审流程>"
```

## 5. 写集冲突规则

```markdown
## 写集冲突规则

| 冲突类型 | 判定方式 | 处理 |
|---|---|---|
| 路径重叠写入 | `<同一 glob/文件出现在多个任务 allowModify>` | 串行、拆分或升级 |
| 共享契约变更 | `<匹配 sharedContracts>` | 单独任务 + 禁止无协调并行 |
| 生成文件冲突 | `<生成输出路径重叠>` | `<规则>` |
| 语义依赖未声明 | `<A 消费 B 未完成契约>` | 调整依赖边 |

- 多任务写冲突处理总则：`<串行、拆分或升级规则>`
```

## 6. 拆分启发式与合并顺序

```markdown
## 拆分启发式

1. 先契约任务，后实现任务
2. 读写集声明到文件或目录粒度
3. 有语义冲突则串行或拆分共享任务
4. 测试任务依赖其验证的实现任务完成（或明确可并行条件）

## 合并顺序

- 遵循任务包 `mergeAfter` / DAG；不以完成速度为准
- 推荐顺序模板：`<契约 → 基础实现 → 依赖方 → 集成验证>`
- 违反合并顺序的处理：`<阻塞集成审查 / 要求重排>`
```

## 7. 输出格式

```markdown
## 输出格式

- 任务列表：`<TASK-ID + 摘要>`
- 每任务 `allowModify`：`<路径列表>`
- 依赖 / `mergeAfter`：`<边列表>`
- 写集冲突检查：`<无冲突 / 冲突及处理>`
- 升级项：`<无，或事项 + Owner>`
```

## 8. 激活检查

- [ ] 模块路径与 `modules.yaml` / 布局规则一致
- [ ] 共享契约并行写入规则明确
- [ ] 冲突处理与合并顺序可被 Orchestrator / 集成审查执行
- [ ] Owner 将 `status` 改为 `active`
