# 试点 PRD 填写模板

> 已填写示例：[PRD-EXAMPLE.md](./PRD-EXAMPLE.md)（仅粒度参考，非默认）
> 产品人员复制本结构到 `product/prd/<version>.md`。

```markdown
# <产品名称> PRD（<版本>）

| 字段 | 值 |
|---|---|
| 状态 | DRAFT |
| 产品负责人 | <identity> |
| 关联快照 | <SNAP-ID 或待生成> |

## 问题与目标

- 要解决的问题：<...>
- 成功标准：<...>

## 范围

### In Scope
- <...>

### Out of Scope / 非目标
- <...>

## 需求

### <REQ-ID> <标题>
- 优先级：P0/P1/...
- 摘要：<...>
- 验收：<可观察条件>

## 开放问题

| id | question | owner | blocking |
|---|---|---|---|
| OQ-001 | <...> | productOwner | yes/no |
```

完成检查：
- [ ] 有范围与非目标
- [ ] P0 需求有验收
- [ ] 开放问题有 Owner
