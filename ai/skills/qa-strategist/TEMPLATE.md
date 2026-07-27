# QA Strategist Skill 填写模板

> QA 策略负责人复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> 本 Skill 定义测试分层、强制触发与规划门禁；具体命令细节以 Tester / 实现 Skill 为准并对齐。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.qa-strategist
version: 1.0.0
status: draft
owner: <QA 策略负责人或团队>
compatibleRoles: [qaStrategist]
relatedRules:
  - <适用 Rule ID>
relatedSkills:
  - skill.tester
---
```

- `owner` 应是可批准本 Skill 的角色/团队。
- 分层策略变更须与 Tester Skill、发布门禁同步更新。

## 2. 适用范围与前置条件

```markdown
## 适用范围

- 覆盖产品/模块：`<范围>`
- 对齐实现 Skill：`<前端 / 后端 Skill ID>`
- 证据 schema：`<test-evidence 或等价>`
- 风险标签来源：`policies.yaml` → `riskTriggers`

## 前置条件

- P0 需求必须能映射到至少一层可执行验证
- 环境缺失时规划结论可为阻塞，不得伪造“可测”
```

## 3. 可测试性检查工作流

```markdown
## 可测试性检查工作流

1. 审查验收标准是否可观测、可判定、可自动化或有明确人工证据路径。
2. 检查负向、边界与权限场景是否缺失。
3. 建立或核对 REQ → 测试层 / TEST 映射。
4. 对不可测的 P0 提出 `REQUEST_CHANGES` 或 `BLOCK`。
5. 输出分层策略与强制触发，供计划与 Tester 使用。
```

## 4. 测试分层与强制触发

```markdown
## 测试分层策略

| 层级 | 目标 | 适用范围 | 强制触发条件 | 默认命令引用 | Owner |
|---|---|---|---|---|---|
| 静态/类型 | `<目标>` | `<路径/包>` | 每任务 | `<Tester 或实现 Skill 命令>` | `<角色>` |
| 单元 | `<目标>` | `<路径/包>` | `<条件>` | `<引用>` | `<角色>` |
| 集成 | `<目标>` | `<路径/包>` | `<条件>` | `<引用>` | `<角色>` |
| 契约 | `<目标或 N/A + 原因>` | `<路径>` | `<条件>` | `<引用>` | `<角色>` |
| E2E | `<目标>` | `<关键路径>` | `<条件>` | `<引用>` | `<角色>` |
```

强制触发不得以“视情况”代替；条件须可客观判定。

## 5. 覆盖与发布门槛

```yaml
qualityGates:
  p0Requirements:
    requiredEvidence: <证据类型或路径约定>
    allowSkippedTests: false
  changedCriticalPath:
    requireE2E: <true|false>
  coverage:
    metric: <line|branch|changed-lines|N/A>
    threshold: <数值或 N/A + 原因>
  release:
    requireAllMandatoryTestsPassed: true
    blockedIsPass: false
```

```markdown
## 覆盖门槛（写入计划）

- P0 REQ：`<最低证据要求>`
- 关键路径变更：`<是否强制 E2E / 冒烟>`
- 例外批准身份：`<身份>`
- 例外证据路径：`<路径>`
```

## 6. 规划委员会 QA 门禁

```yaml
qaPlanningGate:
  maxRounds: <数值>
  passWhen:
    - acceptance-criteria-testable
    - required-test-layers-defined
    - test-environment-available-or-explicitly-blocked
  blockWhen:
    - p0-requirement-has-no-verification-path
  completionRequiresEvidence: true
  exceptionApprover: <身份>
```

## 7. 安全/隐私测试触发（协作）

```markdown
| riskTag | 测试要求 | 数据要求 | 证据 | 协作角色 |
|---|---|---|---|---|
| auth-model-change | `<要求>` | `<要求>` | `<路径>` | securityReviewer |
| new-public-endpoint | `<要求>` | `<要求>` | `<路径>` | securityReviewer |
| handles-pii | `<要求>` | 仅脱敏 Fixture | `<路径>` | securityOperationsOwner |
| `<项目标签>` | `<要求>` | `<要求>` | `<路径>` | `<角色>` |
```

标签须与 `policies.yaml.riskTriggers` 一致；禁止真实生产敏感数据。

## 8. 输出格式

```markdown
## 输出格式

- 可测试性结论：`<PASS / REQUEST_CHANGES / BLOCK>`
- REQ → 测试层映射：`<表或路径>`
- 强制层清单：`<列表>`
- 环境风险：`<无，或 BLOCKED 说明>`
- 开放问题：`<问题 + Owner + 期限>`
```

## 9. 激活检查

- [ ] 每层均有明确强制触发条件
- [ ] 命令引用与 Tester / 实现 Skill 一致
- [ ] P0 至少映射到一层可执行验证
- [ ] `blockedIsPass: false`（或等价）已确认
- [ ] Owner 将 `status` 改为 `active`
