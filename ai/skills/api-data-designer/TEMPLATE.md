# API / Data Designer Skill 填写模板

> API/数据负责人复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> 本 Skill 只写契约与数据设计“怎么做”；技术选型与版本边界引用 Organization Rule。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.api-data-designer
version: 1.0.0
status: draft
owner: <API/数据负责人或团队>
compatibleRoles: [apiDataDesigner]
appliesTo:
  paths:
    - <契约与数据设计路径 glob>
relatedRules:
  - RULE-ORG-CODING
  - RULE-PROJECT-DOMAIN
---
```

## 2. API 约定执行

```markdown
## API

- 风格与错误结构：引用 `<RULE-ID 章节>`
- 契约落盘路径：`<例如 planning/api-design/**>`
- 版本与兼容策略：`<破坏性变更流程>`
- 鉴权/分页/幂等：`<约定或范例路径>`
- 评审输出路径：`<planning/reviews/...>`
```

## 3. 数据与迁移

```markdown
## 数据

- DB / 存储：引用 `<RULE-ORG-STACK>`
- 迁移工具与文件路径：`<工具 + 路径>`
- 命名、软删、审计字段：`<约定>`
- 触发 migrationReviewer 的条件：与 `policies.riskTriggers` 对齐
- 回滚剧本最低要求：`<要点>`
```

## 4. 工作流与禁止事项

```markdown
## 工作流

1. 读取需求快照与候选计划
2. 产出/修订契约与数据模型草案
3. 标明兼容影响与迁移需求
4. 输出规划评审决策（枚举合法）

## 禁止

- 不得在未批准契约上假定实现细节已冻结
- 不得跳过破坏性变更的 ADR/兼容说明
- 不得在契约中写入真实密钥或生产连接串
```

## 5. 激活检查

- [ ] 路径与命令/工具真实存在或有初始化步骤
- [ ] 与 CODING / DOMAIN / policies 无冲突
- [ ] Owner 批准后 `status: active`
