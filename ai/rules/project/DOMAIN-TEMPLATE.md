# RULE-PROJECT-DOMAIN 填写模板

> 已填写示例：[DOMAIN-EXAMPLE.md](./DOMAIN-EXAMPLE.md)（仅粒度参考，非默认）
> Product Owner / 产品分析复制本结构填写 `RULE-PROJECT-DOMAIN.md`。详细条文可放在 `product/business-rules/`，本文件做索引与硬约束摘要。  
> 术语须与 `product/glossary.md` 一致；不预设具体业务领域。

## 1. 元数据

```yaml
---
id: RULE-PROJECT-DOMAIN
version: <语义化版本，如 1.0.0>
status: draft
owner: <productOwner 或产品团队>
override: allowed
appliesTo:
  roles: ["productAnalyst", "apiDataDesigner", "developer", "qaStrategist"]
reviewBy: <YYYY-MM-DD 或 TBD>
---
```

## 2. 领域不变量

违反即 P0/BLOCK（或明确更低级别及确认人）。

```markdown
## 不变量（违反即 P0/BLOCK）

### <INV-ID> <不变量名称>
- 约束：`<任何情况下必须成立的业务事实>`
- 适用范围：`<实体/流程/状态>`
- 违反后果：`<为何属于 P0，或实际严重级别>`
- 检查证据：`<可观察字段、事件或验收方式>`
- 例外：`<无；或批准条件与记录位置>`
- Owner：`<业务责任角色>`
```

若经确认不存在硬不变量：

```markdown
- 结果：当前无 P0 级领域不变量
- 确认人：`<Product Owner>`
- 确认日期：`<YYYY-MM-DD>`
- 重评触发：`<出现何种情况时重新检查>`
```

## 3. 业务规则索引

详细正文放在 `product/business-rules/`；此处仅索引。

```markdown
## 业务规则索引

| ID | 摘要 | 触发条件（一句话） | 详细文档 |
|---|---|---|---|
| BR-001 | `<摘要>` | `<何时应用>` | `product/business-rules/<文件>` |

### 业务规则正文骨架（写入 business-rules 目录时）

### <BR-ID> <规则名称>
- 触发条件：`<何时应用>`
- 输入/前置状态：`<条件>`
- 规则：`<明确、可判定的处理>`
- 输出/状态变化：`<结果>`
- 优先级/冲突顺序：`<与其他规则冲突时如何处理>`
- 例外与审批：`<无；或审批角色及证据>`
- 来源：`<政策、合同、业务决策或访谈>`
- 关联不变量：`<INV-ID 或 N/A>`
```

## 4. 与术语表

```markdown
## 与术语表

见 `product/glossary.md`。需求与代码命名须与术语表一致；冲突时以术语表 + 已批准 DEC 为准。
```

## 5. 完成检查

- [ ] 每条不变量/规则有稳定 ID
- [ ] 至少明确“违反即 P0”清单，或留下不存在的确认记录
- [ ] 条件、结果、例外可由非开发人员判断
- [ ] 索引路径真实存在或已创建对应文件
- [ ] 术语与 glossary / PRD / modules 用词一致
- [ ] Owner 将 `status` 改为 `active`
