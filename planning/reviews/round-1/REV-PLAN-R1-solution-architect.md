# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-R1-solutionArchitect
planId: PLAN-WSC-1.0
round: 1
role: solutionArchitect
snapshotIdAtReview: SNAP-WSC-001
decision: REQUEST_CHANGES
summary: |
  候选计划 PLAN-WSC-1.0 与 RULE-ORG-STACK / RULE-PROJECT-LAYOUT / DEC-WSC-001..003 总体对齐：
  技术栈硬门禁完整；契约唯一所有权归 TASK-WSC-001；DAG 无环且 W3 写集路径互斥；
  模拟存证适配层（ChainAttestationPort）与业务层解耦符合 DEC-WSC-002；005 串行于 004+006 正确处理同事务耦合。
  主要缺口：catalog 004/005 的 writeSet 与 allowModify 边界表述不一致，易导致 SCOPE_VIOLATION 争议；
  共享 API client（frontend/src/api/）未纳入唯一所有权矩阵；005 的 schema-migration 风险标签与 denyModify 迁移禁令口径冲突。
  架构骨架可执行，但边界与所有权须修订后方可批准。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 架构边界（模块写集） | 部分缺口 — W3 互斥通过；CAT 串行任务边界表述不清，见 ISSUE |
| 契约所有权 | 大体通过 — OpenAPI/RBAC/Flyway/路由/存证口归属清晰；共享 API 生成物缺位 |
| DAG 依赖 | 通过 — 无环；`001→002→{003,004,006}→005`；运行时事务耦合已串行 |
| 适配层（DEC-WSC-002） | 通过 — 契约冻结端口 + 006 实现 + 005 只读注入，禁止业务直连链 SDK |
| 风险标签 | 部分缺口 — `handles-pii` / 事务回滚有缓解；`schema-migration` 与 005 写权限口径冲突 |
| 技术栈硬门禁 | 通过 — §1.3 与 RULE-ORG-STACK 一致，禁止第二套框架/真实链 SDK |

## 架构核对摘要

### 契约与模块边界

- `TASK-WSC-001` 作为 `contracts/openapi|rbac|chain`、Flyway 初版、根路由清单的唯一写者，后继任务 `denyModify: contracts/**` 与 `**/db/migration/**`，符合 RULE-PROJECT-LAYOUT 共享契约前置要求。
- 模块码 SHELL / RBAC / OVW / CAT / CHAIN 与 `modules.yaml` 路径一致；W3 中 `overview/**` ∥ `catalog/**`（浏览）∥ `chain/**` 无写集交集。
- 存证：`contracts/chain/**` 冻结端口 → `backend/.../chain/**` 实现模拟适配 → catalog 写路径依赖注入调用，与 DEC-WSC-002 一致。

### DAG

- 拓扑序正确；005 同时依赖 004 与 006，避免产品提交与存证适配并行合入导致半成品版本，与计划 §4/§6 事务回滚策略一致。
- 无环自校验（§9）与本角色手工核对一致。

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | TASK-WSC-004 `writeSet` 声明整包 `frontend/src/features/catalog/**` 与 `backend/.../catalog/**`，而 `allowModify` 又限定「仅浏览读路径、不得实现写 API」。TASK-WSC-005 对同一 glob 再写详情/表单/分类维护。串行虽避免同波冲突，但 SCOPE_VIOLATION 判据依赖叙述而非路径清单，开发与 codeReview 无法机械判定越界。 | 将 004/005 的 `writeSet` 收敛为互斥路径清单（或显式文件/子目录拆分，如 `catalog/browse/**` vs `catalog/editor/**`、`catalog/admin/**`），并使 `allowModify`/`denyModify` 与之逐字一致；Orchestrator scope check 可按路径执行。 | REQ-CAT-001, REQ-CAT-002, REQ-CAT-003, REQ-CAT-004, REQ-CAT-005, REQ-CAT-006 |
| ISSUE-SA-R1-002 | P1 | RULE-PROJECT-LAYOUT 规定共享 API client 位于 `frontend/src/api/`，修改须独立任务或与契约同步；计划 §3 唯一所有权矩阵未列入该路径。TASK-WSC-001 `writeSet` 含 `frontend/` 脚手架但未声明是否生成/冻结 OpenAPI client；W3 各任务仅「只读消费」却无明确制品来源与禁止手改约定。 | 在 §3 所有权矩阵增加 `frontend/src/api/**`（建议唯一写任务 = TASK-WSC-001，或契约变更串行任务）；明确生成方式、版本与后继任务只读规则；触碰即 `SCOPE_VIOLATION`。 | REQ-SHELL-001, REQ-OVW-001, REQ-CAT-001, REQ-CHAIN-001 |
| ISSUE-SA-R1-003 | P2 | TASK-WSC-005 `riskTags` 含 `schema-migration`，同时 `denyModify` 默认禁止 `**/db/migration/**`，仅允许「经 Orchestrator 批准的串行增量 Flyway 任务」。风险标签暗示 005 可能改 schema，与「001 唯一拥有初版 + 增量另开任务」所有权模型冲突，易在实现波次误改迁移目录。 | 二选一写清：(a) 从 005 移除 `schema-migration`，改为依赖「增量迁移插入协议」引用；或 (b) 预置独立串行任务（如 TASK-WSC-005a）专属增量迁移，005 仅消费新 schema，并更新 DAG/波次。 | REQ-CAT-004, REQ-CAT-005, REQ-CAT-006 |

## 无异议项（记录）

- RULE-ORG-STACK：Vue3/TS/Vite/pnpm/Pinia/Router4/Ant Design Vue + Spring Boot 3.3/Java21 + PG16/Flyway + Vitest/JUnit/Testcontainers/Playwright + 模拟存证适配，计划 §1.3 完整引用且禁止项对齐。
- DEC-WSC-001 编码唯一索引归属 001 Flyway；DEC-WSC-002 字段进入存证接口契约与 006 acceptance；DEC-WSC-003 删除保护落在 005，无架构冲突。
- 产品提交与存证同事务失败回滚（§6）与适配层可替换性，架构上可接受；不要求本角色代批 QA/Security。
- OQ-001/OQ-004 非阻塞，不构成架构 BLOCK。
