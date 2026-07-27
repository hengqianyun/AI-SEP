# 按需任务填写模板（OPT-01～OPT-05）

> 仅在对应能力启用时填写；后置不等于永久缺失，需在豁免记录中写 Owner 与目标版本。

## 目录

- [OPT-01](#opt-01)
- [OPT-02](#opt-02)
- [OPT-03](#opt-03)
- [OPT-04](#opt-04)
- [OPT-05](#opt-05)

## OPT-01

**Security Skills**

```markdown
## Security Operations / Review
- 适用路径：<待填写>
- 触发 riskTags：<待填写>
- 敏感资产/数据分类：<仅写标识与类别，不写秘密>
- 必查清单：<鉴权/授权/注入/日志/依赖/配置等>
- 执行工具与命令：<待填写>
- Findings 输出路径：<待填写>
- P0 剩余风险批准身份：securityOperationsOwner
```

完成检查：Security Owner 会签；无真实密钥；触发标签与 policies 一致。

## OPT-02

**无前端项目豁免**

```yaml
exemption:
  scope: frontend
  skippedTasks: [FE-01, FE-02, FE-03, FE-04, FE-05, FE-06, FE-07, FE-08]
  reason: <为何项目无前端交付>
  evidence: <PRD/架构边界路径>
  approvedBy: <productOwner + techLead>
  reconsiderWhen: <触发条件>
```

完成检查：没有前端路径仍装载 `skill.frontend-developer`；未来新增 UI 时会重新实例化。

## OPT-03

**Product Analyst / Plan Editor Skill**

```yaml
decision:
  createSkills: <true|false>
  reason: <角色契约+模板是否已足够>
  proposedIds:
    - <skill.product-analyst>
    - <skill.plan-editor>
  specializedKnowledge: <新增 Skill 必须承载的项目专有做法>
  owner: <产品/协调人>
```

完成检查：不重复角色契约；只有确有专有工作流时才新增 Skill。

## OPT-04

**Learning 评测与复查**

```yaml
learningGovernance:
  evaluationSuite: <路径>
  minIndependentEvents: <数值>
  minConfidence: <0-5>
  scoreThreshold: <数值>
  reviewers: [<身份>]
  reviewInterval: <周期>
  rollbackEvidence: <路径>
  retirementCriteria: <待填写>
```

完成检查：包含反例/回归评测；晋升须人工批准；有回滚版本与复查日期。

## OPT-05

**Cursor/CLI Adapter**

```markdown
## Adapter 实装记录
- Runtime：<Cursor/CLI/CI/...>
- 支持入口：<start/resume/status/approve/cancel>
- 工作流版本：<待填写>
- 状态文件：<路径>
- 幂等键：<生成方式>
- 租约/并发保护：<机制>
- 人工审批接口：<方式>
- 演练 Run：<RUN-ID>
```

完成检查：Adapter 不包含业务规则；重复请求不重复派发；新会话可从状态恢复。
