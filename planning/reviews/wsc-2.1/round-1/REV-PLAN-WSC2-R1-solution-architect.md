# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC2-R1-solutionArchitect
planId: PLAN-WSC-2.1
round: 1
role: solutionArchitect
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  PLAN-WSC-2.1 相对 2.0 在 101 writeSet 收敛、107 denyModify 路径化、契约同步导入/browse 分页、
  W4「106 不得与 103 并行」等方面方向正确；DEC-WSC-005 栈与 sql/init|migration 权威、契约唯一所有权矩阵、
  W3/W4 并行写集在引用 2.0 路径时互斥且 DAG 无环。
  仍阻断批准：TASK-WSC-102..106 以「同 2.0」外挂 writeSet，候选计划本体无法按 §1.4 做字面 scope-check；
  OVW 适配义务落在 101，但 overview 模块无写集且 101 denyModify frontend features，与 LAYOUT OVW 边界冲突。
  另须补齐 101 对旧扁平 backend/src（含 db/migration） sole-authority 清理与骨架净新增 Java/测试路径。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 模块写集 | 部分缺口 — 101/107 已路径化；102..106 外挂「同 2.0」；OVW 无归属，见 ISSUE |
| 并行写集互斥 | 通过（假定内联 2.0 路径）— W3 admin∥browse；W4 maintenance∥detail/editor/chain 无交集 |
| DAG | 通过 — `101→102→{103∥104}→{105←103+104；106←103}→107`；无环；106∤103 |
| DEC-WSC-005 骨架 | 大体对齐 — Boot 2.7.18/MySQL/Redis/Flyway sql 布局与 101 独占迁移正确；旧树清理口径不足，见 ISSUE |
| 契约所有权 | 通过 — `contracts/**` 与 `frontend/src/api/**` 唯一写任务=101；后继 denyModify 含契约/API |
| 技术栈硬门禁 | 通过 — §1.3 与 RULE-ORG-STACK / DEC-WSC-005 一致 |

## 架构核对摘要

### 写集与并行互斥

- 按 PLAN-WSC-2.0 内嵌路径核对（本计划未重述）：W3 `catalog/admin/**` ∥ `catalog/browse/**`；W4 `catalog/maintenance/**` ∥ `detail|editor|chain/**`，路径无交集。
- §6 W4 明确 106 仅在 103 VERIFIED 后启动且不得与 103 并行，避免分类写与维护关联同波竞态，合理。
- TASK-WSC-107 denyModify 已改为显式路径清单（无「除非叙述」例外），与 allowModify「DI 只读调用」一致，可机械校验。

### 契约所有权

- §3.2：`contracts/**`、`frontend/src/api/**`、本轮 `sql/**`、父子 POM/启动脚手架、根路由增量均唯一归属 101；与 RULE-PROJECT-LAYOUT「共享契约须独立前置任务」一致。
- 102..107（含 107 明示）denyModify 覆盖 `contracts/**` / `frontend/src/api/**` / `**/sql/**`，后继只读消费模型成立。

### DEC-WSC-005 / LAYOUT

- 多模块目标 `backend/app/<service>/` + `sql/init` + `sql/migration`；禁止向 `**/db/migration/**` 追加新脚本，与 DEC-WSC-005 反例一致。
- 101 将写集从 2.0 的 `backend/**` 收敛为 POM/resources/启动类/config + 搬迁表，方向正确；但净新增骨架 Java、测试目录与旧扁平树退役仍有缺口（见 ISSUE）。

### DAG

- 拓扑正确；105 汇合 103+104；107 依赖 105（产品写/存证端口就绪）合理；106 不依赖 104/105 可接受（维护契约由 101 冻结，分类树由 103 提供）。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | PLAN-WSC-2.1 §5 对 TASK-WSC-102..106 仅写「同 2.0」，未在本候选计划内给出字面 `writeSet`/`denyModify`/`dependsOn`。§1.4 要求 Orchestrator 按各任务 writeSet **字面路径** scope-check；权威写集外挂于 PLAN-WSC-2.0 导致双源、漂移与无法对 2.1 单文件做机械校验。§10 勾选「101 writeSet 可机械 scope-check」未覆盖 102..106。 | 在 PLAN-WSC-2.1 §5 **内联** 102..106 的完整 `dependsOn`、`writeSet`、`denyModify`（路径 glob，禁止「同 2.0」外挂）；内联后 W3/W4 互斥可对 2.1 单独 diff 验证。 | REQ-SHELL-001, REQ-RBAC-001, REQ-CAT-001, REQ-CAT-006, REQ-CAT-007, REQ-CAT-004, REQ-CAT-005, REQ-CHAIN-001 |
| ISSUE-SA-R1-002 | P1 | §3.1/101/§9 要求 overview「可编译启动」且破坏性变更在 101 内适配，E2E 含 OVW 回归；RULE-PROJECT-LAYOUT 规定 OVW 写边界为 `frontend/src/features/overview/**` 与 `backend/.../overview/**`。101 `denyModify` 禁止 `frontend/src/features/**`；显式 Java writeSet 仅 Application+config，overview 仅能靠「搬迁+编译修复」触及后端。无任何任务声明 overview 写集，若三级 schema 迫使查询/展示超出编译修复，将无合法写路径或被迫 SCOPE_VIOLATION。 | 二选一写清并进 §3.2/§5：(a) 101 writeSet **显式**纳入 `backend/app/*/src/main/java/**/overview/**`（及必要的 frontend overview，若契约/DTO 破坏前端编译），并限定「仅 schema/契约对齐所需，禁止新业务指标」；或 (b) 增设串行任务（如 101b/108）专属 OVW 适配，dependsOn=101，写入 §6 DAG/波次与 REQ 映射。 | REQ-OVW-001, REQ-OVW-002, REQ-OVW-003, REQ-OVW-004, REQ-OVW-005 |
| ISSUE-SA-R1-003 | P1 | DEC-WSC-005：迁骨架后禁止与 V1.0 遗留 `db/migration` **混用无 sole authority**；验收要求空库走 `sql/init`→`sql/migration`。101 writeSet 未明示可删除/退役 `backend/src/**`（旧扁平树）及禁止保留双迁移权威；亦未覆盖 `backend/app/*/src/test/**` 与「非 Application\|config、非搬迁源」的骨架净新增 Java（data-chain 模板常见包）。acceptance `/health`、`/doc.html` 可靠配置/依赖满足，但测试与骨架净新增仍可能触发叙述性例外。 | 在 101：`writeSet` 增加旧树退役路径（如允许删除 `backend/src/**` 或等价「迁移完成后旧扁平源码与 db/migration 不得再作为权威」的可检查条款）；增加 `backend/app/*/src/test/**`（及必要前端测试）若 testScope 要求；对骨架净新增 Java 给出可枚举 glob（例如 `**/common/**`、`**/config/**` 已有之外的白名单）或声明「仅允许从指定骨架模板复制且列入 DEV 搬迁/引入清单」。 | DEC-WSC-005, REQ-SHELL-001 |

## 无异议项（记录）

- W3/W4 并行任务在 2.0 路径模型下写集互斥；波次表禁止 106∥103，合入禁止单 PR 混写两任务路径，架构可接受。
- 契约 `wsc-contracts@2.0.0`：维护条目≡产品、同步导入 API、browse 分页冻结，不构成本角色 BLOCK（API 细节归 apiDataDesigner）。
- OQ-V11-003 暂定同实体下沉为 §3.1 硬约束，与资源模型一致；拆实体须新 DEC，计划已声明。
- 不代替 QA/Security 批准导入 PII/临时文件策略；§3.5 有安全约定即可，本角色不越权。
