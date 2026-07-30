# TESTRUN-TASK-WSC-005

```yaml
taskId: TASK-WSC-005
actorInstance: tester-wsc-005
developerInstance: developer-wsc-005
reviewerInstance: reviewer-wsc-005
reviewDecision: APPROVE
result: PASSED
evidenceId: EVID-TASK-WSC-005-1
testedAt: 2026-07-29T18:10:00+08:00
```

## 前提（MEM-ORCH-WSC-002）

审查报告 `REV-TASK-WSC-005.md` 已落盘且 `decision: APPROVE` 后执行本测试。

## 命令

```text
cd backend && mvn test
cd frontend && pnpm exec vitest run src/features/catalog/detail src/features/catalog/editor src/features/catalog/admin
```

## 结果

| 套件 | 结果 |
|---|---|
| 后端全量 Surefire | PASSED（含 ProductEditor / CategoryAdmin / SessionAuth / browse 回归） |
| 前端 detail/editor/admin vitest | 3 files / 5 tests PASSED |

## testScope 覆盖

| 项 | 证据 |
|---|---|
| 新建 version=1 + 四字段 | ProductEditorIntegrationTest#create_other… |
| 编辑递增 + 旧版可读 | #update_incrementsVersion… |
| 适配失败无半成品 | #attestationFailure… |
| OQ-004 OTHER | 负例拒绝 typeSpecific + 正例创建 |
| DEC-WSC-003 | CategoryAdminIntegrationTest 挂载禁删 reason/count；无挂载可删；非管理员 403 |
| §4.1 提供方分类 403 | SessionAuth + CategoryAdmin |
| 成功/失败反馈 | useProductEditor.spec / useCategoryAdmin.spec |

**result: PASSED**
