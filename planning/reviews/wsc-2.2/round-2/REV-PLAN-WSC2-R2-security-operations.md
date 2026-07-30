# Round 2 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC2-R2-securityOperations
planId: PLAN-WSC-2.2
round: 2
role: securityOperations
snapshotIdAtReview: SNAP-WSC-002
decision: APPROVE
summary: |
  对照 Round 1 本人提出的 ISSUE-SEC-R1-001/002，逐条核验 PLAN-WSC-2.2 修订与 closeWhen。
  §1.2/§3.3 显式继承 PLAN-WSC-1.1 §3.2 会话/吊销模型；TASK-WSC-102 acceptance/testScope 含登出或角色变更后写 API 拒绝负例。
  §3.5/§4 与 TASK-WSC-107 acceptance/testScope 写明错误报告须已认证且仅管理员/提供方可下载，匿名/普通用户 GET → 403。
  两条 ISSUE 的 closeWhen 均已满足，本角色 APPROVE。无新增异议；剩余未关闭 ISSUE：0。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-2.2.md` |
| 快照 | SNAP-WSC-002 |
| R1 对照 | `planning/reviews/wsc-2.1/round-1/REV-PLAN-WSC2-R1-security-operations.md` |

## closeWhen 核验（ISSUE-SEC-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-2.2 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-SEC-R1-001 | P1 | §1.2 或 §3.x 写明继承 PLAN-WSC-1.1 §3.2（会话、登出吊销、角色/权限变更后旧凭证写 API 必须拒绝）；TASK-WSC-102 acceptance/testScope 含至少一条「登出或角色变更后写 API 拒绝」可测用例；关闭前不得标为已接受风险 | §1.2：「V1.1 沿用 PLAN-WSC-1.1 §3.2」；§3.3 全文内联机制/角色绑定/登出/角色变更吊销/前端不得作唯一防护；102 acceptance「沿用 §3.3…写 API 拒绝」；102 testScope「登出或角色变更后写 API 拒绝」可测用例；§7 缓解指向 §3.3+102 负例，非接受风险 | **closeWhen 满足，可关闭** |
| ISSUE-SEC-R1-002 | P1 | §3.1/§3.5 或 107 acceptance 写明错误报告鉴权+授权（登录态 + 角色/归属，普通用户 403）；testScope 含未认证或普通用户 `GET` report 负例 | §3.5：「须已认证；仅管理员或提供方…匿名/普通用户 GET → 403」；§4 普通用户行含「错误报告 GET 403」；107 acceptance「报告下载：登录态 + 管理员/提供方；普通用户/未认证 403」；107 testScope「未认证或普通用户 GET report → 403」 | **closeWhen 满足，可关闭** |

## 本轮 ISSUE 表

无未关闭 ISSUE。R1 两条均确认可关闭；本轮不新增 ISSUE。

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 无异议项（记录）

- §3.5 上传临时存储删除/TTL≤24h、错误报告字段白名单、日志禁全文；§4 RBAC 矩阵方向仍成立。
- 不因模拟上链、菜单占位或 OQ-V11-003 否决业务；无证据要求 BLOCK。
- 高风险接受未伪造；本评审不代替人类 Security/Operations Owner 签署残余风险。
- 本角色不代替必需角色批准；仅就安全/运维域 R1 异议关闭给出 APPROVE。
