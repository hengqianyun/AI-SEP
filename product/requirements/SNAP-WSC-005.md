# 接入端工作台（WSC）需求快照 V1.4 — 座序图 / 用户管理 / 我的数据产品

```yaml
snapshotId: SNAP-WSC-005
sourcePrd: product/prd/wsc-v1.4-seatmap-users-mine.md
sourceVersion: V1.4
runId: RUN-WSC-008
status: APPROVED
createdAt: 2026-08-05
approvedAt: 2026-08-06
approvedBy: productOwner
approvalSource: chat
approvalUtterance: 批准快照
approvalVoidedAt: 2026-08-05
approvalVoidReason: "主会话旁路伪造 APPROVED（非隔离 productAnalyst 评审）；进入合规编排后作废批准"
reapprovedAt: 2026-08-06T14:45:00+08:00
reapprovalNote: "协议修复后由 productOwner 正式批准；非主会话代写"
actorInstance: product-analyst-wsc-008-draft
requirementCount: 6
contractsTarget: wsc-contracts@2.2.0
previousSnapshot: SNAP-WSC-004
uxBaseline:
  snapshotId: SNAP-WSC-003
functionalBaseline:
  snapshotId: SNAP-WSC-004
poScopeConfirm:
  frontend: AI-SEP Vue (port seatmap from Downloads visual)
  users: persistent sys_user
  import: move to 我的数据产品 (PROVIDER only)
  adminKeeps: category + catalog maintenance
changeFromPrevious:
  - reason: V1.4 座序图、持久化用户、我的数据产品与 RBAC 重划
  - added: [REQ-CAT-009, REQ-USER-001, REQ-CAT-010]
  - revised: [REQ-SHELL-001, REQ-RBAC-001]
  - inherited: [REQ-OVW-001..005, REQ-CAT-001..008, REQ-CHAIN-001, REQ-API-001, REQ-UX-001..011]
```

## 业务目标

见 `product/prd/wsc-v1.4-seatmap-users-mine.md`。

## 角色与权限摘要

| 角色 | 可观察能力 |
|---|---|
| 管理员 | 浏览+座序图；分类维护；目录维护；用户管理；无产品增改导 |
| 提供方 | 浏览+座序图；我的数据产品（本人增改导） |
| 普通用户 | 只读浏览+座序图 |

## 需求清单

| id | priority | change | summary | acceptance |
|---|---|---|---|---|
| REQ-SHELL-001 | P0 | 修订 | 侧栏：数据目录；我的数据产品（PROVIDER）；用户管理（ADMIN）；目录维护（ADMIN）；下线自由切角色 | 菜单可见性按角色；登录身份绑定角色 |
| REQ-RBAC-001 | P0 | 修订 | 产品写/导入仅 PROVIDER；ADMIN 写产品 403；分类/目录维护仍仅 ADMIN | 前后端一致；隐藏≠授权 |
| REQ-CAT-009 | P0 | 新增 | 数据流通链座序图 | L2 Top5 按产品数；比例座位；悬停熄灭其它；真实总数；仅公共目录 |
| REQ-USER-001 | P0 | 新增 | 持久化用户与 ADMIN CRUD | sys_user；登录校验 hash；ADMIN 可创建/改角色/软删 |
| REQ-CAT-010 | P0 | 新增 | 我的数据产品 | PROVIDER 可见；无座序图；增改导；列表仅 create_by=本人；公共目录无增改导 |
| REQ-CAT-001 | P0 | 继承 | 目录浏览 | 公共目录只读全量；行为不削弱浏览 |

## Out of Scope

- Downloads React 仓；OAuth；座序图点击筛选；多租户

> **PO 确认记录**（2026-08-06）：`APPROVE_SNAPSHOT`；口头「批准快照」。此前伪造批准已于协议修复作废；本记录为正式重批。快照 `status: APPROVED`。
