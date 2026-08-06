# Round 1 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC5-R1-securityOperations
planId: PLAN-WSC-5.1
round: 1
role: securityOperations
actorInstance: security-operations-wsc-008-r1
snapshotIdAtReview: SNAP-WSC-005
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-005 与 PLAN-WSC-5.1：真登录/密码 hash、角色切换禁用（API+UI 结构不可达）、
  产品写仅 PROVIDER、create_by 本人隔离、用户管理仅 ADMIN、隐藏≠授权等业务安全口径方向正确，
  且 §1.4/§4/§6.1/602·603 acceptance 已有可测意图。阻断 APPROVE 的缺口：
  ① TASK-WSC-602（及 RBAC 重划面）未挂 policies 触发标签 auth-model-change，§6.2 门禁未要求
  securityReviewer，鉴权模型变更审查任务说明不足；
  ② 602（及条件性 603）迁移标签为 migration 而非 schema-migration，未声明 migrationReviewer；
  ③ create_by 空归属「不可冒领」仅见于 §8 风险表，未写入 603 acceptance/testScope。
  另开 P2：602 handles-pii 未约束用户 API 响应/审计日志不得回显 password hash/明文口令。
  无证据 BLOCK 业务目标；不代批其他角色；不伪造高风险接受。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-5.1.md`（`CANDIDATE` / `DRAFT` / Round 1） |
| 快照 | `product/requirements/SNAP-WSC-005.md`（`status: IN_REVIEW`；审批作废后合规重评） |
| policies | `ai/workflow/policies.yaml` → `riskTriggers.securityReviewer`: `auth-model-change` / `new-public-endpoint` / `handles-pii`；`migrationReviewer`: `schema-migration` / `data-backfill` |
| context-map | `**/sql/migration/**` → `requireRoles: [migrationReviewer]` |
| 本角色焦点 | 认证模型变更、RBAC/隔离、用户管理授权、riskTags→审查门禁、迁移/凭据/PII 运维约束 |
| 本实例 | `security-operations-wsc-008-r1`；policies 已启用 `securityOperations` |

## 评审范围与判定

| 维度 | 结论 |
|---|---|
| 真登录 / 密码 hash（REQ-USER-001） | **通过方向** — 602 objective/acceptance：持久化 `sys_user`、正确/错误密码、会话角色=账号角色；禁止明文生产密钥落盘；testScope 含登录成败与软删不可登录 |
| 角色切换禁用（REQ-SHELL-001） | **通过方向** — 601 契约禁用语义 + 602 API 410/403+稳定错误码 + UI「无可点切换 / 结构不可达」；§0.3 已知 RoleSwitcher 残留纳入 602 对账 |
| 产品写仅 PROVIDER / ADMIN 403（REQ-RBAC-001） | **通过方向** — §4 矩阵 + 603 acceptance/testScope 三角色写矩阵；§1.4 禁止纯 CSS 替代鉴权 |
| create_by 隔离（REQ-CAT-010） | **部分缺口** — 本人列表/跨用户 403 已写；空归属不可冒领仅 §8，未进 603 acceptance（ISSUE-003） |
| 用户管理授权（REQ-USER-001） | **通过方向** — ADMIN CRUD；PROVIDER/USER UI 不可达 + API 403；侧栏按角色 |
| riskTags → 审查任务说明 | **缺口** — 602 有 `authn`/`migration`/`rbac-breaking`/`handles-pii`，**缺** `auth-model-change` 与 `schema-migration`；§6.2 仅 developer→codeReviewer→tester，未绑定 policies 触发角色（ISSUE-001/002） |
| 新认证保护端点（`/admin/users` 等） | **通过方向（本轮不单开 ISSUE）** — 计划要求会话鉴权 + 非 ADMIN 403；非「无 auth 公网」语义。须随 auth-model-change 的 security-review 覆盖新写面，不另以 `new-public-endpoint` 强制（除非计划声明匿名可达） |
| SNAP 门禁 / 预落地对账 | **通过方向** — 文首禁止伪称已批准；§0.2 强制 diff 对账；不因旁路代码假装验收 |
| 导入报告 PII 白名单 | **通过方向** — §1.5 / 603 不回退；本版无扩大白名单意图 |

## 风险对照

| 风险点 | 计划措施 | 判定 |
|---|---|---|
| 演示切角色 → 登录绑定角色（鉴权模型变更） | 601/602 禁用 API+UI；E2E 场景 1 | 实现口径可测；**缺** `auth-model-change` + securityReviewer 门禁 |
| 密码明文 / 弱存储 | hash 登录；种子「不得明文落盘生产密钥」 | 方向正确；算法/响应不回显 hash 未钉死（P2） |
| ADMIN 越权产品写 / 仅前端隐藏 | §4 + 603 + 纯 CSS 禁令 + E2E 3/5 | 通过方向 |
| create_by 空值被 PROVIDER 冒领 | §8「603 明确空归属策略」 | **未**落入任务 acceptance/testScope |
| `sys_user` / 可能的 create_by 迁移 | 602 独占 V6；603 条件追加；§8 migration-review 文字 | 标签名与 §6.2 未对齐 policies/`schema-migration` |
| 用户列表泄露 hash / 审计日志口令 | 602 `handles-pii` + AuthAuditLogger 写集提及 | 标签有、验收约束不足（P2） |
| 预落地安全代码漂移 | §0.2/602·603 对账 | 通过方向 |
| OAuth/公开注册蔓延 | Out of Scope | 通过方向 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-WSC5-R1-001 | P1 | `policies.yaml` 将 **`auth-model-change`** 列为 `securityReviewer` 触发标签。本计划 TASK-WSC-602 实质变更鉴权模型（持久化 hash 登录、会话角色绑定、禁用 `POST /auth/session/role`、壳层 RoleSwitcher 下线）；601 冻结角色切换禁用与 RBAC 矩阵；603 落地产品写授权。602 `riskTags` 现为 `[authn, handles-pii, migration, rbac-breaking]`，**不含** `auth-model-change`；§6.2 每任务门禁仅 `developer` → `codeReviewer` → `tester`，**未**说明匹配 `riskTriggers` 时须隔离调度 `securityReviewer` 且未通过不得 `VERIFIED`。自定义标签无法保证触发足够安全审查任务说明。 | （1）为 TASK-WSC-602（建议同步在 601 契约 RBAC/会话禁用面或计划 §6.2 总述中声明覆盖范围）增加 `riskTags` 含 **`auth-model-change`**；（2）在 §6.2（或等价发布/任务门禁）写明：命中 `policies.riskTriggers.securityReviewer` 的标签时，须独立 `securityReviewer` 审查（对照 authn/会话绑定/切角色禁用/三角色写矩阵），P0/P1 未关闭前不得将该任务标 `VERIFIED`；（3）验收/测试意图保持可测（已有部分可保留）。关闭前不得标为已接受风险。 | REQ-USER-001, REQ-SHELL-001, REQ-RBAC-001 |
| ISSUE-SEC-WSC5-R1-002 | P1 | `policies.yaml` 的 `migrationReviewer` 触发标签为 **`schema-migration`** / `data-backfill`；`context-map.yaml` 对 `**/sql/migration/**` 要求 `migrationReviewer`。602 `writeSet` 含 `sql/migration/**`（`sys_user`），`riskTags` 使用 **`migration`**（非 policies 名）；603 可在缺列时追加 migration。§6.2 **未**要求 migration-review 通过后才可 VERIFIED。§8 虽文字提及 migration-review，但任务标签与门禁未硬绑定，审查任务说明不足。 | （1）602（及若 603 触碰 migration 时）`riskTags` 使用 **`schema-migration`**（可保留说明性别名，但须含 policies 正式名）；（2）§6.2 或任务 acceptance 声明：写 `sql/migration/**` 须独立 `migrationReviewer`，未通过不得 VERIFIED；（3）与现有「禁止 `db/migration/`」「种子无生产明文密钥」一致即可。 | REQ-USER-001, REQ-CAT-010 |
| ISSUE-SEC-WSC5-R1-003 | P1 | §8 风险「create_by 历史数据为空」缓解写明「603 明确空归属策略（不可被 PROVIDER 冒领编辑）」，但 TASK-WSC-603 `acceptance`/`testScope` 仅覆盖「列表仅本人」「改他人 403」，**未**要求空/null `create_by`（或历史脏数据）不可被当前 PROVIDER 编辑/冒领，也无对应负例测试。隔离语义在空归属场景可被绕过。 | 在 603 `acceptance` 增加可测口径：空/缺失 `create_by` 的产品不得被任意 PROVIDER 成功编辑/导入覆盖（须 403 或计划书面冻结的等价拒绝）；`testScope` 至少一条后端（或集成）负例。策略细节可写在任务 objective/交付说明，但必须可测。 | REQ-CAT-010, REQ-RBAC-001 |
| ISSUE-SEC-WSC5-R1-004 | P2 | 602 已标 `handles-pii`，acceptance 强调种子「不得明文落盘生产密钥」，但未约束：`/admin/users` 列表/详情/创建响应**不得**返回 `password`/`passwordHash`；认证失败与 `AuthAuditLogger` **不得**记录明文口令或可逆密文；演示种子仅用文档化测试口令且仓库无生产密钥。存在 API/日志侧凭据泄漏面。 | 在 602 `acceptance`/`testScope` 写明：用户管理 API 响应禁止回显密码哈希与明文；审计/应用日志禁止记录明文口令；至少一条测试或契约示例负例（或审查清单勾选证据）。可附带声明单向哈希算法族（如 bcrypt/argon2 等项目已用方案）与「禁止明文落库」。 | REQ-USER-001 |

## 无异议项（记录）

- 真登录成功/失败、会话角色绑定、软删不可登录的 602 验收意图本角色接受为 USER-001 主路径基线（在 ISSUE-001/004 关闭后仍须由 securityReviewer 实证）。
- 角色切换「API 禁用 + UI 结构不可达」与 §0.3 RoleSwitcher 对账提示充分；不因预落地残留单独开 ISSUE。
- §4 产品写仅 PROVIDER、ADMIN 产品写 403、用户管理仅 ADMIN、隐藏≠授权、禁止纯 CSS 替代鉴权 — 通过方向。
- 601 独占契约 / 串行四波降低 RBAC 写集竞态 — 通过方向（并行规划域细节不代评）。
- 座序图可视化（604）在鉴权只读聚合前提下不构成独立鉴权模型异议；perf-aggregation 非本角色 P0。
- SNAP `IN_REVIEW` 文首门禁正确；本评审**不**等同批准 SNAP，亦不代批 productAnalyst。
- 高风险接受未伪造；不代替人类 `securityOperationsOwner` 签署残余风险。
- 本角色不代替 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor / apiDataDesigner / uxUiPlanner / maintainer 批准。

## 升级路径

- ISSUE-SEC-WSC5-R1-001 / 002 / 003 若被拒绝修补 → 升级 `securityOperationsOwner`（`policies.escalation.onSecurityResidualRisk`）；**不**因此否决 SNAP 业务目标本身。
- 若修补后仍拟带「无 securityReviewer」合入鉴权变更 → 本角色下一轮可 `BLOCK` 并升级 Owner（本轮尚未构成 BLOCK）。

## decision

**REQUEST_CHANGES** — 待 ISSUE-SEC-WSC5-R1-001（P1）、ISSUE-SEC-WSC5-R1-002（P1）、ISSUE-SEC-WSC5-R1-003（P1）按 `closeWhen` 写入计划后，本角色可在后续 round 重评。ISSUE-SEC-WSC5-R1-004（P2）建议同轮修补，不单独构成 BLOCK。禁止代批其他角色。
