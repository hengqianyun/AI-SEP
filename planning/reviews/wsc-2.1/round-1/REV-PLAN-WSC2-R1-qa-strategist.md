# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC2-R1-qaStrategist
planId: PLAN-WSC-2.1
round: 1
role: qaStrategist
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  七任务均有 testScope；§0 对历史 ISSUE-QA-R1-001/002（107 fixture 路径与计数、104 筛选+load-more）的吸收文案满足原 closeWhen。
  §4 经「同 2.0」可取得三角色×分类/维护/增改/导入矩阵，102/103/105/106 引用可支撑 RBAC 主路径。
  仍阻塞批准：107 四件套 fixture 未覆盖 OQ-V11-004「部分成功」同文件混行场景；§6.1 E2E 缺可执行命令与独立 tester/失败不得发布条款，P0 机械关门弱于 PLAN-WSC-1.1 §5.4。
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-R1-003 | P1 | 107 `testScope` 规定 `tests/fixtures/import/` 含 full-success、code-conflict、category-mismatch、oversize 并断言 success/failure 计数，但无「同文件内成功行+失败行」的 partial-success（或等价命名）fixture 及对应 `successCount`/`failureCount` 期望。acceptance / OQ-V11-004 要求部分成功；§6.1 E2E「至少一行失败」可用全失败样例满足，不足以保证部分成功路径被测。另：相对 2.0 testScope，「必填缺失」失败行未落入命名 fixture。 | 107 `testScope`（及/或 `tests/fixtures/import/` 清单）增加 partial-success（同文件≥1 成功且≥1 失败）样例，写明期望 success/failure 计数；E2E 批量导入场景显式引用该混行 fixture（或等价）并断言计数与错误报告可下载。建议补 required-field-missing（或并入混行失败原因之一）。 | REQ-CAT-008 |
| ISSUE-QA-R1-004 | P1 | §6.1 仅列 6 条场景标题 + 报告目录/`TESTRUN-WSC-E2E-V11`；未规定 Playwright（或仓库既有）执行命令/入口、由独立 `tester` 执行、失败不得标发布就绪。§8「同 2.0」虽含角色独立性与「E2E 通过」，仍无法像 PLAN-WSC-1.1 §5.4 一样对 P0 门禁做机械判定。P0 覆盖依赖 W5 E2E，门禁条款不足则 OVW/主路径回归风险不可验收。 | 在 §6.1（或 W5/`testScope` 附件）补齐：工具与命令（或脚本路径）、报告路径（可沿用现路径）、独立 tester、失败不得发布；场景步骤与 SNAP P0（含 OVW 核心 + CAT-007/008 增量）可逐条勾选。 | REQ-SHELL-001, REQ-RBAC-001, REQ-OVW-001, REQ-OVW-002, REQ-OVW-004, REQ-CAT-001, REQ-CAT-006, REQ-CAT-007, REQ-CAT-008, REQ-CHAIN-001 |
| ISSUE-QA-R1-005 | P2 | 107 acceptance 含错误报告字段白名单与临时文件清理（§3.5），`testScope` 仅到「报告入口 / 10MB / CHAIN / 计数」，未要求对报告字段白名单（禁止完整信用代码等）做可观测断言；清理若无可测探针则应声明 tester 检查方式或降级为运维抽检并写清。 | 107 `testScope` 增加：错误报告仅含白名单字段的负例/抽样断言；临时文件清理给出可测手段（钩子/TTL 观测）或明确非自动化抽检与证据路径。 | REQ-CAT-008 |
| ISSUE-QA-R1-006 | P2 | OVW-001..005 无独立实现任务；101 仅「overview 可编译启动」，功能回归完全压到 W5 E2E。迁移后直至 E2E 前缺少 metrics/stream 等只读 API smoke，缺陷发现过晚。 | 101 或明确回归波次的 `testScope` 增加 overview 只读 smoke（至少 metrics + 最新流可达/空态友好）；或写明由某 VERIFIED 任务附带最小集成断言。 | REQ-OVW-001, REQ-OVW-002, REQ-OVW-004 |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 完整性 | 101–107 均有；104/107 相对 2.0 已加固；仍见 ISSUE-QA-R1-003/005 |
| RBAC 矩阵 | §4→2.0 表覆盖三角色×分类/维护/增改导；102 壳层+过滤器、103/106 非管理员、105 产品写回归可测 |
| Fixture | 导入路径与四类负例已定点；缺部分成功混行（003） |
| E2E / P0 | 场景方向覆盖 V1.1 增量与 OVW 指标可见；可执行门禁不足（004） |
| 历史 QA closeWhen（2.0 R1） | ISSUE-QA-R1-001/002 吸收文案满足；本轮不重开，以 003+ 跟踪新缺口 |

## 说明

- 未执行任何测试；不宣称实现或 E2E 已通过。
- 决策为 `REQUEST_CHANGES`（存在 P1）；非验收不可测所致 `BLOCK`。
