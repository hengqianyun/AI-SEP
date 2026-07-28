# Round 1 Review — Security / Operations

```yaml
reviewId: REV-PLAN-R1-securityOperations
planId: PLAN-WSC-1.0
round: 1
role: securityOperations
snapshotIdAtReview: SNAP-WSC-001
decision: REQUEST_CHANGES
summary: |
  计划要求后端 RBAC 与写 API 403（禁止仅前端隐藏），denyModify 排除密钥与生产配置，handles-pii 与日志脱敏、Flyway 串行与回滚演练、存证适配可替换且无真实链 SDK，方向正确。
  主要缺口：登录态/会话或令牌模型及角色变更后权限立即生效的失效机制未写入任务验收；可观测性（审计日志/指标/告警）无最低要求；
  PII/敏感标识在 API 响应最小化与落库范围未成契约门禁；发布门禁提及 staging/回滚但无运维运行手册或任务级交付物路径。
  无证据表明必须否决业务范围；不构成 BLOCK，但上述运行与安全约束关闭前不能 APPROVE。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 认证授权 | 部分 — 角色矩阵与 403 有；会话/撤销模型缺失 |
| 敏感数据 / 隐私 | 部分 — 有 riskTag 与脱敏意图；缺字段级约束 |
| 可观测性 | 缺口 — 无日志/指标/告警最低集 |
| 部署 / 迁移 / 回滚 | 框架可接受 — §6/§7 有缓解；缺可执行运维制品 |

## 风险对照

| 风险点 | 计划措施 | 判定 |
|---|---|---|
| 写权限绕过 | TASK-WSC-002 后端 403 + 负例矩阵 | 通过方向 |
| 密钥落库 | denyModify env 与 prod yml | 通过 |
| PII / 信用代码 | 005 handles-pii；日志脱敏；UI 可读 | 需收紧 API/落库（ISSUE） |
| schema 损坏 | 001 唯一初版；增量串行；回滚演练 | 通过方向 |
| 半成品上链版本 | 005 与 006 事务回滚 | 通过 |
| 真实链暴露 | DEC-WSC-002 模拟适配 | 通过 |
| 登录滥用 / 会话残留 | 未定义 | 缺口 |
| 运维盲飞 | §7 要 staging E2E；无观测与 runbook | 缺口 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-R1-001 | P0 | SNAP/计划要求登录态鉴权且角色变更后写入口立即更新，并禁止仅靠前端隐藏；TASK-WSC-002 未定义认证机制（会话/JWT 等）、令牌或会话在登出/角色变更后的失效或强制刷新策略，以及对应可测验收。存在权限变更后旧凭证仍可调写 API 的窗口。 | 在 TASK-WSC-001/002（契约或 acceptance）写明 V1 认证与会话模型、角色/权限变更后的失效或刷新行为，及至少一条「权限撤销/变更后写 API 拒绝」集成测试；关闭前不得将本项标为已接受风险。 | REQ-RBAC-001, REQ-SHELL-001 |
| ISSUE-SEC-R1-002 | P1 | TASK-WSC-005 标记 handles-pii，§6 要求 API 最小化暴露与日志脱敏，但 TASK-WSC-001 OpenAPI/schema 验收未列出敏感字段清单（涉及个人信息标记、供应商信用代码、DID、哈希）的落库范围、响应可见性与日志禁止明文规则。 | 在契约冻结验收中增加敏感字段表（存储/API/日志三列策略）；005 testScope 含 PII 读写与日志脱敏断言或等价检查清单。 | REQ-CAT-004, REQ-CAT-005, REQ-CHAIN-001 |
| ISSUE-SEC-R1-003 | P1 | 安全/运维职责要求可观测性最低集；计划无任何任务要求审计事件（登录失败、403、产品提交、分类删除拒绝）、指标或告警，发布后难追溯越权与写操作。 | 在计划 §6/§7 或独立运维约束中定义 V1 最低审计/日志事件集与保留口径，并映射到 TASK-WSC-002/005 的 acceptance 或 testScope（至少结构化安全日志字段约定）。 | REQ-RBAC-001, REQ-CAT-005, REQ-CAT-006 |
| ISSUE-SEC-R1-004 | P1 | §7 要求 Flyway 干净应用与回滚演练证据，但无部署/备份/回滚 runbook 路径或任务交付物；§6 写「保留 down 脚本」与常见 Flyway 正向迁移实践不一致，易造成虚假回滚安全感。 | 明确 V1 回滚方式（备份恢复和/或 forward-fix 迁移）及证据落盘路径；staging 发布检查清单引用该路径；删除或改写不准确的 down 脚本表述。 | — |

## 无异议项（记录）

- 不因模拟上链或 Out of Scope 菜单占位否决业务；DEC-WSC-002 降低真实链合规成本，可接受。
- 多租户/支付等非目标不引入额外攻击面，与 SNAP 一致。
- 高风险接受未伪造；本评审不代替人类 Security/Operations Owner 签署残余风险。
- 无 P0「不可接受且无法通过修订关闭」项，故不 BLOCK；升级路径：若 ISSUE-SEC-R1-001 被拒绝修补则升级 securityOperationsOwner。
