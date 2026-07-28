# Round 2 Review — Security / Operations

```yaml
reviewId: REV-PLAN-R2-securityOperations
planId: PLAN-WSC-1.1
round: 2
role: securityOperations
snapshotIdAtReview: SNAP-WSC-001
decision: APPROVE
summary: |
  对照 Round 1 本人提出的 ISSUE-SEC-R1-001/002/003/004，逐条核验 PLAN-WSC-1.1 修订与 closeWhen。
  §3.2 V1 会话模型与 002「权限变更后写 API 拒绝」集成用例已落地；§3.7 敏感字段三列策略与 005 PII/日志脱敏清单已闭合；
  §6.1 最低审计事件集已映射 002/005；§6/§7.6 回滚改为备份恢复/forward-fix 并指定 runbook 路径。
  四条 ISSUE 的 closeWhen 均已满足，本角色 APPROVE。无新增异议。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-1.1.md` |
| 快照 | `product/requirements/SNAP-WSC-001.md` |
| R1 对照 | `planning/reviews/round-1/REV-PLAN-R1-security-operations.md` |

## closeWhen 核验（ISSUE-SEC-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-1.1 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-SEC-R1-001 | P0 | 001/002 写明 V1 认证与会话模型、角色/权限变更后失效或刷新；至少一条「权限撤销/变更后写 API 拒绝」集成测试；不得标为已接受风险 | §3.2 会话/吊销、登出、角色变更强制刷新；001 acceptance「认证 security scheme 与 §3.2 一致」；002 acceptance「登出与角色/权限变更后旧凭证写 API 拒绝」；testScope「权限撤销/变更后写 API 拒绝」集成用例；§6 风险表「权限变更后旧会话」缓解指向 §3.2，非接受风险 | **closeWhen 满足，可关闭** |
| ISSUE-SEC-R1-002 | P1 | 契约冻结验收含敏感字段表（存储/API/日志）；005 testScope 含 PII 读写与日志脱敏断言或检查清单 | §3.7 敏感字段策略表；001 acceptance「敏感字段表（§3.7）落入契约/说明制品」；005 testScope「PII 读写与日志脱敏清单」；riskTag `handles-pii` 保留 | **closeWhen 满足，可关闭** |
| ISSUE-SEC-R1-003 | P1 | §6/§7 定义 V1 最低审计/日志事件集与保留口径，映射 002/005 acceptance 或 testScope | §6.1 事件集（登录失败、写 API 403、产品提交成功/失败、分类删除拒绝）+ 最低字段 + 归属任务；保留口径；002 acceptance「结构化安全日志含登录失败与 403」；005 acceptance「产品提交/分类保存/删除拒绝写入审计事件」 | **closeWhen 满足，可关闭** |
| ISSUE-SEC-R1-004 | P1 | 明确备份恢复和/或 forward-fix；证据落盘路径；staging 清单引用；改写不准确 down 脚本表述 | §6 回滚「备份恢复和/或 forward-fix」；「禁止依赖不可靠的 down 脚本」；§7.6 `ops/runbooks/wsc-v1-rollback.md`；001 writeSet 含 `ops/runbooks/` | **closeWhen 满足，可关闭** |

## 本轮 ISSUE 表

无未关闭 ISSUE。R1 四条均确认可关闭；本轮不新增 ISSUE。

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 无异议项（记录）

- 后端 403 + §4.1 矩阵、denyModify 排除密钥/生产配置、模拟存证无真实链 SDK 等 R1 无异议项仍成立。
- 本评审不代替人类 Security/Operations Owner 签署残余风险；不构成 BLOCK。
- 本角色不代替必需角色批准；仅就安全/运维域 R1 异议关闭给出 APPROVE。
