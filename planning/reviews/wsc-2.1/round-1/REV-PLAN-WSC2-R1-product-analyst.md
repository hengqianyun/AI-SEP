# Round 1 Review — Product Analyst

```yaml
reviewId: REV-PLAN-WSC2-R1-productAnalyst
planId: PLAN-WSC-2.1
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  相对 SNAP-WSC-002：In/Out Scope、OQ-001/004/V11-003/V11-004 口径与增量定位正确；
  CAT-007/008 有主任务；§3.1「维护条目≡产品」与 OVW E2E 必跑已吸收既有 PA 关切。
  仍有两处产品可验收缺口：101 声称 overview 可编译但 denyModify 排除 features/**，
  OVW 无明确写归属；CAT-008 模板「最小集」未冻结到契约/107 acceptance，相对 PRD 字段表不可核验。
```

## 评审范围（产品分析）

- 对照权威：`SNAP-WSC-002`；计划：`PLAN-WSC-2.1`（任务细节「同 2.0」处对照 `PLAN-WSC-2.0`）。
- 聚焦：REQ 覆盖、范围、OQ、相对 SNAP 的增量完整性；不裁定技术选型。

## 覆盖核对摘要

| 项 | 结论 |
|---|---|
| In Scope = SNAP 全部 17 REQ | 对齐（§1.1 + §9） |
| Out of Scope | 与 SNAP/PRD 一致（登记/订单/连接器完整流、真实链、多租户、支付、导入全量类型专属列） |
| 新增 CAT-007 / CAT-008 | 106 / 107 主责清晰 |
| 修订 SHELL/RBAC/CAT-001/002/006 及继承*三级适配 | 102–105 映射完整 |
| OQ-V11-003 | 下沉为 §3.1 硬约束「维护条目 ≡ 产品」；拆分须新 DEC 的 SNAP 口径可执行 |
| OQ-V11-004 | 部分成功 + 错误报告；107 acceptance/testScope 可测 |
| OQ-001 / OQ-004 | 非阻塞；保持 Out of Scope / 基础信息，未蔓延 |
| 既有 ISSUE-PA-R1-001/002（对 2.0） | 本计划 §3.1/101/§6.1 已写入对应修订；本轮不重开，但见下方新 ISSUE |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PA-R1-003 | P1 | SNAP 成功标准要求总览可用；§3.1/101 acceptance 要求 overview 可编译启动，§9 将 OVW-001..005 归 101+E2E。但 101 `denyModify` 含 `frontend/src/features/**`，`writeSet` 未授予 `overview/**`（相对 2.0「105/热修最小适配 overview」表述被删）。契约破坏性变更后前端总览可能无法在任何任务内合法修复，P0/P1 OVW 回归门禁落空。 | 在计划中指定 OVW/overview（前端+后端查询适配）的唯一写归属：扩大 101 writeSet 含 overview 最小适配，或插入串行热修任务 / 并入明确任务 writeSet；acceptance 含「overview 模块可编译」且 E2E §6.1 OVW 断言仍为必跑。 | REQ-OVW-001, REQ-OVW-002, REQ-OVW-003, REQ-OVW-004, REQ-OVW-005 |
| ISSUE-PA-R1-004 | P1 | SNAP REQ-CAT-008 acceptance 要求「模板字段最小集」；PRD `wsc-v1.1.md` 列出模板至少含产品名称/编码/类型/行业分类/来源/更新频率/个人信息/公共数据/交付方式/计费方式/价格等。PLAN-WSC-2.1 §3.1 冻结导入同步 API/DTO，107 acceptance/testScope 覆盖大小/计数/报告/上链，但未冻结或引用模板列最小集，无法按 SNAP 验收「模板是否合格」，且三级模型下「行业分类」列含义（二级标签 vs 三级路径/id）未声明。 | §3.1（或 107 acceptance）写明模板列最小集（可直接引用 PRD 列表）及三级分类列语义（须能解析到可挂载三级）；107 testScope 增加「模板列齐全」与「缺三级/错误三级」失败行用例。 | REQ-CAT-008 |

## 非异议说明（避免重复开单）

- 壳层「企业信息 / 占位未开放」：属 V1.0 继承行为；本计划以增量 + E2E「V1.0 P0 主路径回归」覆盖，本轮不单独立 ISSUE。
- OQ-V11-003 仍为 SNAP blocking 暂定：计划已硬化为可执行假设，符合「规划可据此推进；拆实体须先 DEC」。
