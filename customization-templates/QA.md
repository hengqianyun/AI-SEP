# 测试定制任务填写模板（QA-01～QA-07）

> 复制对应章节到目标交付物，替换 `<待填写>`。无法执行测试时必须标记 `BLOCKED`，不得写成 `PASSED`。

## 目录

- [QA-01](#qa-01)
- [QA-02](#qa-02)
- [QA-03](#qa-03)
- [QA-04](#qa-04)
- [QA-05](#qa-05)
- [QA-06](#qa-06)
- [QA-07](#qa-07)

## QA-01

**测试分层与强制层**

```markdown
| 层级 | 目标 | 适用范围 | 强制触发条件 | 默认命令 | Owner |
|---|---|---|---|---|---|
| 静态/类型 | <待填写> | <待填写> | 每任务 | <待填写> | <待填写> |
| 单元 | <待填写> | <待填写> | <待填写> | <待填写> | <待填写> |
| 集成 | <待填写> | <待填写> | <待填写> | <待填写> | <待填写> |
| 契约 | <待填写/N/A + 原因> | <待填写> | <待填写> | <待填写> | <待填写> |
| E2E | <待填写> | <关键路径> | <待填写> | <待填写> | <待填写> |
```

完成检查：
- [ ] 每层都有明确触发条件，不以“视情况”代替规则
- [ ] 与 FE/BE Developer Skill 中的真实命令一致
- [ ] P0 需求至少映射到一个可执行测试层

## QA-02

**Tester Skill**

```markdown
---
id: skill.tester
version: 1.0.0
status: draft
owner: <测试负责人/团队>
compatibleRoles: [tester]
---

## 环境
- 工作目录：<待填写>
- 依赖服务：<待填写>
- 环境变量样例：<待填写>
- Fixture：<待填写>

## 执行顺序
1. <静态检查命令>
2. <单元测试命令>
3. <集成/契约命令>
4. <E2E 命令及触发条件>

## 结果规则
- 退出码 0 且断言通过：PASSED
- 断言或命令失败：FAILED
- 环境/权限/依赖不可用：BLOCKED（必须写解除条件）
```

完成检查：
- [ ] 所有命令已由测试人员实际运行
- [ ] `BLOCKED`、`FAILED`、`PASSED` 判定互斥且可审计
- [ ] `status: active` 前由 QA Owner 批准

## QA-03

**覆盖与发布测试门槛**

```yaml
qualityGates:
  p0Requirements:
    requiredEvidence: <待填写>
    allowSkippedTests: false
  changedCriticalPath:
    requireE2E: <true|false>
  coverage:
    metric: <line|branch|changed-lines|N/A>
    threshold: <待填写/N/A + 原因>
  release:
    requireAllMandatoryTestsPassed: true
    blockedIsPass: false
```

完成检查：
- [ ] 门槛能由机器结果或明确证据判断
- [ ] 未执行必需测试不能进入 `COMPLETED`
- [ ] 例外批准身份与证据路径已定义

## QA-04

**测试证据模板确认**

```yaml
evidenceId: EVID-<TASK-ID>-<NNN>
taskId: <TASK-ID>
status: PASSED | FAILED | BLOCKED
environment:
  name: <待填写>
  version: <待填写>
layers:
  - name: <unit/integration/e2e/...>
    command: <实际执行命令>
    result: PASSED | FAILED | BLOCKED | SKIPPED
    relatedReqs: [<REQ-ID>]
    artifact: <日志/报告路径>
    notes: <待填写>
blockedBy: <仅 BLOCKED 时必填>
unblockWhen: <仅 BLOCKED 时必填>
```

完成检查：
- [ ] 与 `test-evidence.schema.json` 一致
- [ ] 命令、环境、REQ、结果和报告可复现
- [ ] SKIPPED 有批准依据，不被计作 PASSED

## QA-05

**集成审查测试视角**

```markdown
## 集成束测试清单
- 并行任务：<TASK-ID 列表>
- 合并顺序：<待填写>
- 跨模块契约：<路径/版本>
- 冒烟命令：<待填写>
- 契约测试命令：<待填写/N/A + 原因>
- 关键业务路径：<待填写>
- 回归范围：<待填写>
- 通过标准：<待填写>
```

完成检查：
- [ ] 覆盖跨任务交互，而非重复单任务自测
- [ ] 所有入口可执行且有证据输出路径
- [ ] 失败会回到具体责任任务

## QA-06

**规划委员会 QA 门禁**

```yaml
qaPlanningGate:
  maxRounds: <待填写>
  passWhen:
    - acceptance-criteria-testable
    - required-test-layers-defined
    - test-environment-available-or-explicitly-blocked
  blockWhen:
    - p0-requirement-has-no-verification-path
  completionRequiresEvidence: true
  exceptionApprover: <身份>
```

完成检查：
- [ ] QA 可因“不可测试”提出 `REQUEST_CHANGES`/`BLOCK`
- [ ] 不会因环境缺失伪造批准
- [ ] 与 `policies.yaml` 的轮次和发布门禁一致

## QA-07

**安全/隐私测试触发**

```markdown
| riskTag | 测试要求 | 数据要求 | 证据 | 协作角色 |
|---|---|---|---|---|
| auth-model-change | <待填写> | <待填写> | <路径> | securityReviewer |
| new-public-endpoint | <待填写> | <待填写> | <路径> | securityReviewer |
| handles-pii | <待填写> | 仅脱敏 Fixture | <路径> | securityOperationsOwner |
| <项目标签> | <待填写> | <待填写> | <路径> | <角色> |
```

完成检查：
- [ ] 与 `policies.yaml.riskTriggers` 标签完全一致
- [ ] 禁止使用真实生产敏感数据
- [ ] 剩余风险只能由具名人类 Owner 接受
