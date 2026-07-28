# Round 1 Review — Product Analyst

```yaml
reviewId: REV-PLAN-R1-productAnalyst
planId: PLAN-WSC-1.0
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-001
decision: REQUEST_CHANGES
summary: |
  候选计划 PLAN-WSC-1.0 正确引用 SNAP-WSC-001，In Scope / Out of Scope 与快照一致冻结；
  14 条 REQ 均有唯一主实现归属（§8），P0 与 P1（REQ-OVW-003、REQ-OVW-005）均在发布门禁 §7 要求验收通过；
  成功标准 E2E 路径、三角色 RBAC、DEC-WSC-001/002/003 及非阻塞 OQ-001/OQ-004 口径与快照对齐。
  主要缺口：OQ-004 对「其他数据产品」无专属字段区的裁决未下沉至 TASK-WSC-005 验收，存在实现与验收口径漂移风险；
  快照非功能约束（中文界面、操作反馈、桌面分辨率）未映射到任何任务 acceptance，发布门禁缺少可审计勾选项。
  综上：产品范围与优先级覆盖充分，但 2 处验收可追溯性不足，建议修订后批准。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 产品范围（In Scope） | 通过 — §1.1 与 SNAP/PRD 一致 |
| 优先级（P0/P1） | 通过 — §7 门禁含全部 P0 及 P1 REQ |
| Out of Scope | 通过 — 冻结一致；TASK-WSC-002 占位与风险 §6 有防蔓延措施 |
| 验收覆盖 | 部分缺口 — 见 ISSUE 表 |
| 开放问题 | 通过 — 无 blocking OQ；OQ-001/OQ-004 处理口径与快照一致 |

## REQ 覆盖核对

| 优先级 | 数量 | 计划主归属 | 发布门禁 |
|---|---|---|---|
| P0 | 12 | TASK-WSC-002..006 全覆盖 | §7.1 列明 |
| P1 | 2（OVW-003、OVW-005） | TASK-WSC-003 | §7.1 明确要求验收 |

- TASK-WSC-001 作为契约/脚手架二级覆盖，不改变 14 REQ 唯一主归属，可接受。
- REQ-CAT-005 运行时依赖 TASK-WSC-006 存证适配，DAG 005→006 依赖与快照 DEC-WSC-002 一致。

## Out of Scope 与 OQ 一致性

| 项 | 快照 | 计划 | 判定 |
|---|---|---|---|
| 数据登记/交易订单/连接器 | Out of Scope，菜单占位 | §1.1、TASK-WSC-002 acceptance | 一致 |
| OQ-001 | 非阻塞，保持 Out of Scope | §2.2、壳层未开放提示 | 一致 |
| OQ-004 | V1 仅基础信息 | §2.2 已声明；TASK-WSC-005 acceptance 未体现 | **缺口** |
| 真实链/移动端/多租户/支付 | Out of Scope | §1.1 冻结 | 一致 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PA-R1-001 | medium | 快照 OQ-004 与计划 §2.2 均明确「其他数据产品」V1 仅基础信息、无独立类型专属字段区；但 TASK-WSC-005 `acceptance` 仅写「类型切换专属字段区」，未排除「其他数据产品」。测试与 codeReview 可能按三类专属字段验收，与产品裁决冲突。 | TASK-WSC-005（及必要时 TASK-WSC-001 契约/schema 说明）的 `acceptance` 显式写入：产品类型为「其他数据产品」时不展示/不要求类型专属字段区，仅校验基础信息分组；testScope 含该类型负例或等价用例。 | REQ-CAT-004, REQ-CAT-005 |
| ISSUE-PA-R1-002 | low | SNAP「非功能约束」含：中文界面、关键操作成功/失败反馈、常见桌面分辨率可用、图表窗口变化可重绘；计划仅在部分任务间接涉及（如 OVW-002 空态/缩放、CHAIN 敏感标识可读），无任务级 acceptance 或 §7 发布门禁勾选项。 | 在 §7 发布门禁或相关 TASK（至少 SHELL/OVW/CAT）补充可观察 NFR 验收条目，或声明由 staging E2E 清单统一覆盖并给出清单引用路径。 | REQ-SHELL-001, REQ-OVW-002, REQ-CAT-001, REQ-CAT-002 |

## 无异议项（记录）

- 14 REQ 计数与 §8 映射自校验 PASS，无遗漏、无重复主归属。
- P0 成功标准路径（登录/角色 → 总览 → 目录 → 详情 → 上链 → 新增/编辑产生版本）在 §7.2 完整复现。
- REQ-OVW-004 要求展示「数据登记/交易订单」类型与 Out of Scope 不冲突：PRD/SNAP 允许总览流展示相关上链动态文案，计划 TASK-WSC-003 acceptance 已列三类类型字段。
- DEC-WSC-001（编码唯一/格式）、DEC-WSC-002（模拟存证字段）、DEC-WSC-003（挂载产品禁止删分类）均在对应任务 acceptance 与 testScope 中可追溯。
