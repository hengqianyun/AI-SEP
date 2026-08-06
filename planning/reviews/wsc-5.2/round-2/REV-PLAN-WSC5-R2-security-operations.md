# Round 2 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC5-R2-securityOperations
planId: PLAN-WSC-5.2
round: 2
role: securityOperations
actorInstance: security-operations-wsc-008-r2
snapshotIdAtReview: SNAP-WSC-005
decision: APPROVE
summary: |
  对照 PLAN-WSC-5.2 与 Round 1 本角色 ISSUE-SEC-WSC5-R1-001..004 的 closeWhen：
  ① 602（及 601/603 相关面）已挂 auth-model-change；§1.4/§6.2 硬绑定独立
  securityReviewer，未通过不得 VERIFIED；
  ② 602 riskTags 含 schema-migration；603 条件触碰 migration 须另加该标签；
  §3.3/§6.2 硬绑定独立 migrationReviewer；
  ③ 603 acceptance/testScope 空/缺失/异主 create_by → 403 负例可测；
  ④ 602 acceptance/testScope 禁回显 password/hash，审计日志禁明文口令。
  四条 ISSUE 均满足 closeWhen，本轮全部关闭。无新增异议；不伪造高风险接受；
  不代批其他角色；不写 ai/runs/**。
closedIssues:
  - ISSUE-SEC-WSC5-R1-001
  - ISSUE-SEC-WSC5-R1-002
  - ISSUE-SEC-WSC5-R1-003
  - ISSUE-SEC-WSC5-R1-004
openIssues: []
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-5.2.md`（`CANDIDATE` / `DRAFT` / Round 2；`basedOn: PLAN-WSC-5.1`） |
| 快照 | `SNAP-WSC-005`（`snapshotIdAtReview`；文首仍声明 `IN_REVIEW`，本评审不批准 SNAP） |
| 对照 | Round 1 `REV-PLAN-WSC5-R1-security-operations.md` ISSUE-SEC-WSC5-R1-001..004 |
| policies | `ai/workflow/policies.yaml` → `riskTriggers.securityReviewer`: `auth-model-change` / `new-public-endpoint` / `handles-pii`；`migrationReviewer`: `schema-migration` / `data-backfill` |
| 本实例 | `security-operations-wsc-008-r2`；仅关闭本角色 ISSUE |

## Round 1 ISSUE 核验

| id | sev | closeWhen 核验证据（PLAN-WSC-5.2） | 本轮 |
|---|---|---|---|
| ISSUE-SEC-WSC5-R1-001 | P1 | **(1)** TASK-WSC-602 `riskTags`=`[authn, auth-model-change, handles-pii, schema-migration, rbac-breaking]`；601 亦含 `auth-model-change`；603 含 `auth-model-change`（产品写授权落地）。**(2)** §1.4 与 §6.2 第 4 条：命中 `auth-model-change`（及 policies securityReviewer 触发标签）须独立 `securityReviewer`，对照 authn/会话绑定/切角色禁用/三角色写矩阵；P0/P1 未关闭前不得 `VERIFIED`。发布门禁 §9.4 亦要求 602 securityReviewer 通过。**(3)** 602 acceptance/testScope 仍可测（真登录、410 切角色、三角色只读角色区）。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC5-R1-002 | P1 | **(1)** 602 `riskTags` 含正式名 **`schema-migration`**（不再仅用 `migration`）；§3.3：`sys_user` 迁移触发 `schema-migration`→`migrationReviewer`；603 若追加 create_by 列/索引须另加 `schema-migration` + migrationReviewer。**(2)** §6.2：`riskTags` 含 `schema-migration` **或** 写集触碰 `**/sql/migration/**` → 独立 `migrationReviewer`，未通过不得 `VERIFIED`。**(3)** 禁止 `db/migration/`、种子无生产明文密钥口径保持。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC5-R1-003 | P1 | §1.2：`create_by` 空/缺失不可被 PROVIDER 冒领。603 `acceptance`：**空归属** — `create_by` 为空/缺失/非当前用户时，PROVIDER 编辑/更新/导入归属 → **403**（或契约稳定拒绝码）。`testScope` 单元格表：本人 200；他人/空归属 **403**；并要求后端「空/异主 create_by 负例」。§8 与 PA-001 映射一致。满足 closeWhen。 | **CLOSED** |
| ISSUE-SEC-WSC5-R1-004 | P2 | §3.1 硬冻结：用户 list/get/create/update 响应禁止 `password`/`passwordHash`。602 `acceptance`：响应永不含 password/passwordHash；`AuthAuditLogger`/应用日志不记录明文口令；单向哈希族声明。`testScope`：响应无 password/hash；审计日志无明文口令（或审查清单勾选证据）。满足 closeWhen。 | **CLOSED** |

## 安全运营焦点复检（无新 ISSUE）

| 维度 | 结论 |
|---|---|
| 真登录 / 密码 hash | 通过 — 602 可测；种子禁生产明文密钥 |
| 角色切换禁用 | 通过 — API 410 + UI 结构不可达；E2E 场景 1 强制 |
| 产品写仅 PROVIDER / 隐藏≠授权 | 通过 — §4 + 603 矩阵 + 禁纯 CSS |
| create_by 含空归属 | 通过 — R1-003 已关闭 |
| 用户管理仅 ADMIN | 通过 — 602 三角色正负例 |
| riskTags → security/migration review | 通过 — R1-001/002 已关闭 |
| 凭据不回显 | 通过 — R1-004 已关闭 |
| SNAP / 预落地 | 通过方向 — 文首门禁 + §0.2 对账；本评审不批准 SNAP |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 2 无新增异议；R1 四条均已按 closeWhen 关闭 | — | — |

## 关闭清单

| 状态 | ISSUE ids |
|---|---|
| **closedIssues**（本角色） | ISSUE-SEC-WSC5-R1-001, ISSUE-SEC-WSC5-R1-002, ISSUE-SEC-WSC5-R1-003, ISSUE-SEC-WSC5-R1-004 |
| **openIssues**（本角色） | （空） |

## 无异议项 / 边界

- 不代替 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor / apiDataDesigner / uxUiPlanner / maintainer 批准。
- 不批准 SNAP-WSC-005；文首 `IN_REVIEW` 派发门禁仍由 Orchestrator/委员会其他路径处理。
- 高风险接受未伪造；不代替人类 `securityOperationsOwner` 签署残余风险。
- 实现阶段仍须实际调度 `securityReviewer` / `migrationReviewer` 并产出实证；本 APPROVE 仅针对**计划文本**对 closeWhen 的满足。
- 未写 `ai/runs/**`。

## decision

**APPROVE** — ISSUE-SEC-WSC5-R1-001..004 全部满足 closeWhen 并关闭（closedIssueCount=4，openIssueCount=0）。无新增 REQUEST_CHANGES / BLOCK 项。
