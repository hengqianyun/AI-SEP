# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-R1-qaStrategist
planId: PLAN-WSC-1.0
round: 1
role: qaStrategist
snapshotIdAtReview: SNAP-WSC-001
decision: REQUEST_CHANGES
summary: |
  候选计划 PLAN-WSC-1.0 为 6 个实现任务均声明了 testScope，P0/P1 REQ 有主归属与发布门禁 §7；
  RBAC 403、分类删除负例、存证适配单元与同事务回滚等关键负向场景有初步覆盖意向。
  主要缺口：§7 要求的 P0 staging E2E 成功路径无任务级/波次级可执行 testScope 与证据路径；
  空态（目录无结果等）未进入对应 testScope；上链版本号递增与 DEC-WSC-002 字段断言未写成可测条目；
  三角色×写 API 矩阵及分类删除保护（含原因文案）的可观测断言不完整。
  可测性骨架存在，但 P0 验收无法按计划机械关门，建议修订后批准。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务 testScope 完整性 | 部分缺口 — 六任务均有条目，但粒度不足以覆盖全部 P0 可观测验收 |
| P0 可测性 / E2E | 缺口 — 发布门禁有路径，缺命名场景、层级与证据归属 |
| RBAC | 部分通过 — 002 含 403 负例；提供方×分类写、跨任务矩阵需补强 |
| 空态 | 缺口 — acceptance 有、testScope 多处未显式断言 |
| 上链版本 | 部分缺口 — 回滚有；新建 v1 / 编辑递增 / 快照字段缺显式用例 |
| 分类删除保护（DEC-WSC-003） | 部分通过 — 有负例意向；原因与挂载计数断言未写清 |

## REQ → 测试覆盖核对（摘要）

| 区域 | 主任务 | testScope 现状 | QA 判定 |
|---|---|---|---|
| SHELL / RBAC | 002 | RBAC 单测/集成、403 负例、壳层占位 | 正向可测；矩阵完整性见 ISSUE |
| OVW | 003 | API 集成、图表空态、口径 fixture | OVW-002 空态较好；OVW-004 跨 Out-of-Scope 类型缺 fixture 策略 |
| CAT 浏览 | 004 | 查询集成、筛选组合、预览权限组件 | 缺「无结果空态」显式用例 |
| CAT 写 / 分类 | 005 | CRUD、编码冲突、分类删除负例、事务回滚、PII | 版本递增与删除原因断言不足 |
| CHAIN | 006 | 适配可替换 mock、列表/快照 API、页空态 | 页空态有；与 005 联产出的版本字段断言应在联测写清 |
| 契约底座 | 001 | OpenAPI lint、Flyway 空库、build、契约-REQ 覆盖 | 缺对 RBAC 矩阵/存证端口字段的契约级校验引用 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-R1-001 | P0 | SNAP 成功标准与计划 §7.2 要求 staging E2E：登录/角色 → 总览 → 目录筛选 → 详情 → 上链信息 → 新增/编辑并产生链上版本；RULE-ORG-STACK 含 Playwright。各 TASK `testScope` 仅为模块单测/集成/组件；W5 仅允许补证据、不另开实现 TASK，也未指定 E2E 场景清单、命令、报告路径与负责波次。P0 发布门禁无法按计划机械判定通过。 | 在计划中增加可执行的 P0 E2E `testScope`（可挂 W5 或独立测试策略附件）：场景步骤对齐 §7.2、工具 Playwright、通过/失败条件、报告路径；并声明由独立 tester 执行，失败不得标发布就绪。 | REQ-SHELL-001, REQ-RBAC-001, REQ-OVW-001, REQ-CAT-001, REQ-CAT-002, REQ-CAT-004, REQ-CAT-005, REQ-CHAIN-001 |
| ISSUE-QA-R1-002 | P1 | REQ-CAT-002 acceptance 要求「无结果有明确空态文案」；TASK-WSC-004 `testScope` 仅写「筛选组合用例」「预览权限可见性」，未要求空态文案可观测断言。REQ-OVW-004 / 指标卡在无数据时的空态亦未进入 003 `testScope`（003 仅明确图表空态）。 | 004 `testScope` 增加「筛选/搜索无结果空态」用例（断言文案或等价 testid）；003 补充最新流/指标在空库下的空态或占位断言（与 acceptance 一致）。 | REQ-CAT-002, REQ-OVW-001, REQ-OVW-004 |
| ISSUE-QA-R1-003 | P1 | REQ-CAT-005 / DEC-WSC-002：新建产生第 1 版上链、编辑版本号递增，记录含 metadataHash / ownerDID / timestamp / certificate.owner。TASK-WSC-005 `testScope` 写「产品 CRUD 集成」「与存证适配同事务/失败回滚」，未要求断言版本号序列与字段完整性；006 侧重适配单元与只读页，不覆盖「写路径产出版本」联测。 | 005（或 005↔006 集成）`testScope` 增加：新建 → 版本=1 且四字段齐全；编辑 → 版本严格递增且旧版本仍可读；适配失败 → 无产品半成品、无孤儿上链行。 | REQ-CAT-005, REQ-CHAIN-001 |
| ISSUE-QA-R1-004 | P1 | REQ-RBAC-001：管理员可维护分类；提供方可见新增/编辑、不可见分类维护；普通用户无写入口；写 API 无权限 403。TASK-WSC-002 有「403 负例矩阵」但未点名三角色 ×（分类维护 / 新增产品 / 编辑产品）全覆盖；005 仅「非管理员不可进」，易漏「提供方调用分类写 API 403」与「普通用户 UI+API」双层断言。 | 在 002 或 005 `testScope` 固化 RBAC 矩阵用例表（三角色 × 三类写入口/写 API，期望 可见/隐藏 + 200/403），作为该任务 tester 必跑项。 | REQ-RBAC-001, REQ-CAT-006 |
| ISSUE-QA-R1-005 | P1 | REQ-CAT-006 / DEC-WSC-003：有挂载产品的一/二级分类禁止删除，且须明确原因（如仍有 N 个产品）。TASK-WSC-005 `testScope` 仅「分类删除负例」，未规定：一级与二级均测、响应/UI 含原因与数量、无挂载时可删的对照正例。 | 005 `testScope` 写明 DEC-WSC-003 用例：挂载产品时删一/二级均失败且原因可观测；无挂载删除成功；非管理员不可达。 | REQ-CAT-006 |
| ISSUE-QA-R1-006 | P2 | REQ-OVW-004 要求最新流类型覆盖「目录登记 / 数据登记 / 交易订单」，而后两者业务流为 Out of Scope。003 `testScope`「口径 fixture 校验」未说明如何注入非本版业务产生的事件类型，测试可能无法构造三类类型或误测为空。 | 在 003 `testScope` 明确 seed/fixture 策略：允许测试数据直接写入上链动态类型枚举（含 Out-of-Scope 类型文案），并断言三类均能展示且时间倒序；禁止依赖未实现的登记/订单 API。 | REQ-OVW-004 |

## 无异议项（记录）

- 14 条 REQ 均有主实现任务；P1（OVW-003、OVW-005）纳入 §7 验收，符合「P0/P1 须有验收」门禁意图。
- 001 含 Flyway 空库应用与契约覆盖检查，利于迁移与契约回归底座。
- 006「适配单元（可替换 mock）」有利于 DEC-WSC-002 可替换性验证，方向正确。
- 本角色不代替执行测试，亦不将环境未就绪误判为质量通过；当前 decision 仅针对计划可测性。
