# 设计决策（DEC / ADR）填写模板

> 已填写示例：[DEC-EXAMPLE.md](./DEC-EXAMPLE.md)（仅粒度参考，非默认）
> 决策 Owner 复制本结构创建 `design/decisions/DEC-<ID>.md`（或组织规定的 ADR 文件名）。尖括号内容必须替换。  
> 破坏性变更、跨模块契约偏离、技术栈例外等须先有 `APPROVED` 决策方可进入开发任务。

## 1. 元数据

```yaml
---
decisionId: DEC-<稳定编号>
title: <决策标题>
status: PROPOSED  # PROPOSED | APPROVED | SUPERSEDED | REJECTED
date: <YYYY-MM-DD>
owner: <批准身份/角色>
source: <来源，如评审记录或 human-confirmed-日期>
closes: [<开放问题 ID 列表或空>]
affects: [<REQ-ID / BR-ID / ARCH-INV 等>]
snapshotRefs: [<SNAP-ID 或空>]
supersedes: [<被替代的 DEC-ID 或空>]
---
```

## 2. 正文结构

```markdown
# <DEC-ID> <决策标题>

## 背景

`<需要固化的约束、冲突或历史原因>`

## 决策

`<已选择的方向及适用边界>`

## 未选择方案

| 方案 | 未采用原因 |
|---|---|
| `<方案>` | `<原因>` |

## 影响

- 用户/业务影响：`<影响>`
- 后续约束：`<规划和实现不得擅自改变的内容>`
- 技术/契约影响：`<路径、兼容性或 N/A>`
- 重评触发：`<何时允许重新决策>`

## 反例（可选但推荐）

- `<明确禁止的行为或状态>`

## 验收口径（可选）

- `<可观察、可判定的结果>`
```

## 3. 无既有决策时的检查记录

若盘点后确认无需迁移历史决策，可落一条检查记录而非空目录沉默：

```markdown
# 既有设计决策检查

- 结果：N/A
- 原因：`<为何当前没有需迁移或固化的既有决策>`
- 确认人：`<Product Owner / Tech Lead>`
- 确认日期：`<YYYY-MM-DD>`
- 重评触发：`<出现何种情况时重新检查>`
```

## 4. 完成检查

- [ ] `decisionId` 稳定唯一
- [ ] 状态、日期、Owner、影响范围完整
- [ ] 决策、边界、影响和重评触发可被后续角色执行
- [ ] `APPROVED` 前不得作为开发任务的唯一依据绕过未决冲突
- [ ] 无决策时已留下 N/A、原因、确认人和日期
