# Round 1 Review — Security / Operations

```yaml
reviewId: REV-PLAN-WSC2-R1-securityOperations
planId: PLAN-WSC-2.1
round: 1
role: securityOperations
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-002 与 PLAN-WSC-2.1：§4 RBAC 矩阵覆盖分类/目录维护/增改/导入且要求写 API 403，方向正确；
  §3.5 已约定上传临时存储删除或 TTL≤24h、错误报告字段白名单与禁止日志记全文，独立评估可关闭上轮同类缺口。
  仍阻断 APPROVE：TASK-WSC-102 可写 auth/security 却未显式声明沿用 PLAN-WSC-1.1 §3.2 会话/吊销模型及可测负例；
  错误报告 GET（按 reportId）未写明鉴权与授权边界，存在 IDOR/越权下载风险。无证据否决业务范围，不构成 BLOCK。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| RBAC（三角色 × 写入口/写 API） | 通过方向 — §4 矩阵 + 102/103/106/107 acceptance 对齐 REQ-RBAC-001 |
| 导入文件安全 | 部分 — §3.5 临时文件生命周期已约定；类型白名单/报告下载授权仍不足 |
| PII / 错误报告白名单 | 通过方向 — §3.5 白名单 + 禁止信用代码/ownerDID/明文哈希；107 acceptance 含白名单 |
| 会话鉴权沿用假设 | 缺口 — 仅隐式 basedOn V1.0，未写入 101/102 契约或 acceptance |

## 风险对照

| 风险点 | 计划措施 | 判定 |
|---|---|---|
| 仅前端隐藏绕过写权限 | §4 + 后端 403；非管理员维护不可达 | 通过方向 |
| 上传原始文件持久化泄漏 | §3.5 临时存储 + 结束后删除或 TTL≤24h；107 acceptance | 通过方向 |
| 错误报告回显 PII | §3.5 字段白名单；日志禁全文 | 通过方向 |
| 错误报告 IDOR | `GET` 按 reportId；未约定调用者鉴权/归属 | 缺口（ISSUE） |
| 权限变更后旧会话仍可写导入/维护 API | 未显式继承 1.1 §3.2；102 却改 auth/security | 缺口（ISSUE） |
| 密钥/契约越权改写 | 101/107 denyModify 含密钥与 contracts | 通过方向 |
| schema 迁移损坏 | 101 独占 + 备份演练（继承 2.0） | 通过方向（非本轮焦点） |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-R1-001 | P1 | SNAP REQ-RBAC-001 要求登录态鉴权且禁止仅靠前端隐藏；PLAN-WSC-1.1 §3.2 已冻结会话/吊销/角色变更后旧凭证写 API 拒绝。PLAN-WSC-2.1 仅写 basedOn/previousRun，未在 §1.2 假设、§3.1 契约或 TASK-WSC-102 acceptance/testScope 显式声明「V1.1 沿用 PLAN-WSC-1.1 §3.2」及回归负例；同时 102 `writeSet` 含 `auth/**`、`security/**`，存在静默弱化会话失效策略的窗口。 | 在 §1.2 或 §3.x 写明继承 PLAN-WSC-1.1 §3.2（会话机制、登出吊销、角色/权限变更后旧凭证写 API 必须拒绝）；TASK-WSC-102 acceptance/testScope 含至少一条「登出或角色变更后，分类维护/目录维护/导入等写 API 拒绝」可测用例；关闭前不得标为已接受风险。 | REQ-RBAC-001, REQ-SHELL-001, REQ-CAT-008 |
| ISSUE-SEC-R1-002 | P1 | §3.1 导入 API 含 `GET` 错误报告（按 `reportId`）；§3.5 约束报告字段白名单，但未约定：须已认证、且仅管理员/提供方（导入主体或具备导入权限角色）可下载、禁止匿名/普通用户凭 reportId 读取。同步导入下 reportId 若可枚举或泄露，构成越权读取运营失败明细。 | 在 §3.1/§3.5 或 107 acceptance 写明错误报告下载的鉴权+授权（登录态 + 角色/归属校验，普通用户 403）；testScope 含未认证或普通用户 `GET` report 负例。 | REQ-CAT-008, REQ-RBAC-001 |

## 无异议项（记录）

- 不因模拟上链、菜单占位或 OQ-V11-003「维护条目≡产品」否决业务；无证据要求 BLOCK。
- §3.5 对「原始上传临时存储 / TTL」与「错误报告禁止回显完整信用代码、ownerDID、明文哈希」的约定，独立判断满足上轮同类 closeWhen 意图；本轮不再重复开具等价 ISSUE。
- §4 RBAC 可测矩阵对管理员/提供方/普通用户与导入入口的划分与 SNAP 角色摘要一致。
- 高风险接受未伪造；本评审不代替人类 Security/Operations Owner 签署残余风险。
- 升级路径：若 ISSUE-SEC-R1-001/002 被拒绝修补 → 升级 securityOperationsOwner；不因此否决 REQ-CAT-008 业务本身。
