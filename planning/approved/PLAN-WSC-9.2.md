# 接入端工作台（WSC）V1.7 执行计划 — 交易订单（链内一事一议简易流程）（Round 2 · APPROVED）

```yaml
planId: PLAN-WSC-9.2
status: APPROVED
planType: APPROVED
snapshotId: SNAP-WSC-009
sourcePrd: product/prd/wsc-v1.7-transaction-orders.md
runId: RUN-WSC-012
basedOn: PLAN-WSC-9.1
lineageFrom: PLAN-WSC-8.3
previousRun: RUN-WSC-011
functionalBaseline:
  snapshotId: SNAP-WSC-008
  planId: PLAN-WSC-8.3
  contracts: wsc-contracts@2.3.2
uxBaseline:
  snapshotId: SNAP-WSC-003
  planId: PLAN-WSC-3.1
contractsTarget: wsc-contracts@2.3.2
round: 2
createdAt: 2026-08-19
approvedAt: 2026-08-20
approvedBy: planningCommittee
taskCount: 9
waveCount: 4
requirementCount: 21
authorRoles: [planEditor]
actorInstance: plan-editor-wsc-012-r2
```

> **批准声明**：本文件为 `PLAN-WSC-9.2` **Round 2** 批准稿（`planType: APPROVED` / `status: APPROVED`）。规划委员会 8 位必需角色全部独立 APPROVE（2026-08-20）。16 条 R1 ISSUE 全部 CLOSED。
>
> **需求权威**：`snapshotId: SNAP-WSC-009`（`status: APPROVED`，PO 2026-08-19）。PRD `product/prd/wsc-v1.7-transaction-orders.md` 仅作叙述对照；验收以 SNAP 为准。
>
> **PO 已锁定**（SNAP `poLockedDefaults`）：成功指标=订单可追踪率；本期止于合约达成；仅平台内建单、外部回传建单不做；平台统一须知；签署附件为主；统一合约确认页；Word/PDF、20MB、一份、无限期、白名单+病毒扫描、按角色下载；人民币两位小数；含税未税同价且保留两字段；无拒绝/退回/超时态；上链 mock；按角色脱敏；列表仅分页（jumper+pageSize，档位 10/20/50/100）；状态不可回退；三未完成态三方可取消；ADMIN 可代 PROVIDER 确认订单/确认合约/取消；PROVIDER 仅本企业作为提供方；USER 仅本人需求方；ADMIN 列表全部；产品始终下单快照；数字合约主数据带入、变更出新版本；无通知 SLA；前端 `RULE-GLOBAL-FRONTEND`；波次 A/B/C。

---

> **候选声明**（历史）：本文件原为 Round 2 候选稿，经规划委员会 8 位角色隔离独立复评后全员 APPROVE。以下为完整正文。

（以下正文与 `planning/proposals/PLAN-WSC-9.2.md` 一致，仅元数据状态已更新为 APPROVED。完整正文请参见 proposals 原文。）

---

## 10. 完成检查（执行计划）

- [x] 引用 SNAP-WSC-009 / RUN-WSC-012；`planId: PLAN-WSC-9.2`；`status: APPROVED`；`planType: APPROVED`；`round: 2`；`basedOn: PLAN-WSC-9.1`
- [x] §「修订说明 / 待 R2 复评」映射 R1 全部 16 条 ISSUE（**已关闭**，原提出者 R2 复评确认）
- [x] 功能基线 SNAP-WSC-008 / PLAN-WSC-8.3 / wsc-contracts@2.3.2；UX 基线 SNAP-WSC-003 / PLAN-WSC-3.1
- [x] 任务从 TASK-WSC-911 起；未使用 901–910
- [x] 契约唯一写任务 911；Flyway 唯一写任务 912
- [x] §0 预落地对账（VERSION 2.3.3、订单占位、包分层）
- [x] §0.3 文件互斥；§6 DAG 无环；允许的并行写集无交
- [x] 每任务含 id、依赖、write/deny、testScope、REQ、可观察验收
- [x] 前端 toast / 二次确认 / 分页写入任务包
- [x] mock 上链、ADMIN 代操作、PROVIDER 本企业、USER 本人、三方取消、止于达成、无外部回传建单、附件为主、统一确认页、产品快照、CNY 两位、含税未税双字段
- [x] OQ-V17-001/002、OQ-V16-* 标 non-blocking 并给实现假设，未假装已关
- [x] 继承 V1.6 目录/壳层/用户企业且任务 deny 削弱
- [x] R2 修订吸收：全部 16 条 ISSUE 已 CLOSED
- [x] 规划委员会 Round 2 独立评审 / **全员 APPROVE**（2026-08-20）
