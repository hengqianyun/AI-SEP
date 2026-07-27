# Solution Architect Skill 填写模板

> Tech Lead / 架构负责人复制本结构填写 `SKILL.md`。不预设具体架构风格。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.solution-architect
version: 1.0.0
status: draft
owner: <tech-lead 或架构角色>
compatibleRoles: [solutionArchitect]
relatedRules:
  - RULE-PROJECT-ARCHITECTURE
  - RULE-ORG-STACK
---
```

## 2. 必读与评审焦点

```markdown
## 必读

- `RULE-PROJECT-ARCHITECTURE`
- ADR 目录：`<路径>`

## 评审焦点

- 系统边界、模块依赖、与官方技术栈对齐
- NFR：性能 / 可用性 / 一致性 / 可观测性（按项目裁剪）

## NFR 基线

| 指标 | 目标 | 测量方式 | 适用模块 |
|---|---|---|---|
| <指标> | <目标或 N/A> | <方式> | <范围> |
```

## 3. 输出与升级

```markdown
## 输出

- 规划评审文件路径：`<planning/reviews/...>`
- 需要时产出 ADR 草案：`<design/decisions/ 模板>`

## 升级

- 不可逆技术路线 → Tech Lead
- 合规/安全边界 → Security Owner
```

## 4. 激活检查

- [ ] NFR 基线可验证或明确 N/A
- [ ] ADR 入口真实
- [ ] Owner 批准 `active`
