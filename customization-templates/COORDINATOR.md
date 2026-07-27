# 项目协调任务填写模板（C-01～C-07）

> 协调人负责收敛、校验和批准流程，不代替产品、前端、后端、测试编写专业正文。

## 目录

- [C-01](#c-01)
- [C-02](#c-02)
- [C-03](#c-03)
- [C-04](#c-04)
- [C-05](#c-05)
- [C-06](#c-06)
- [C-07](#c-07)

## C-01

**填写人类 Owner**

```yaml
projectId: REPLACE_ME
owners:
  productOwner: { name: "", contact: "", notes: "" }
  techLead: { name: "", contact: "", notes: "" }
  securityOperationsOwner: { name: "", contact: "", notes: "" }
  maintainer: { name: "", contact: "", notes: "" }
# aliases:
#   - person: ""
#     identities: []
```

完成检查：
- [ ] 四个身份均能联系到真实负责人
- [ ] 一人多身份已显式记录
- [ ] 批准记录要求填写当时身份

## C-02

**替换 projectId**

```markdown
## projectId 替换记录
- 目标 projectId：`<待填写>`
- 已检查：
  - [ ] `ai/rules/project/owners.yaml`
  - [ ] `ai/rules/project/modules.yaml`
  - [ ] `ai/rules/project/context-map.yaml`
  - [ ] `ai/workflow/policies.yaml`
- 全仓占位搜索命令/结果：`<待填写>`
- 复核人：`<待填写>`
```

完成检查：上述配置中无 `REPLACE_ME`，且 ID 规则一致。

## C-03

**合并跨职能规范**

```markdown
| 冲突 ID | 文件/章节 | 前端意见 | 后端意见 | QA/产品意见 | 裁决 | ADR/Rule 版本 |
|---|---|---|---|---|---|---|
| CONFLICT-001 | <待填写> | <待填写> | <待填写> | <待填写> | <待填写> | <路径> |
```

完成检查：
- [ ] STACK/CODING/LAYOUT 无相互矛盾条款
- [ ] 无法直接解决的冲突已形成 ADR
- [ ] 裁决未只停留在聊天记录

## C-04

**定稿 policies.yaml**

```markdown
## Policy 审批清单
- [ ] 必需角色与可选角色已确认
- [ ] `enabled: false` 的角色均有 `omitReason`
- [ ] planning/codeReview `maxRounds` 已确认
- [ ] riskTriggers 与安全/迁移/QA 清单一致
- [ ] release 人工批准身份已具名
- [ ] escalation 路由与 SLA 已确认
- [ ] isolation 模式可执行
- [ ] learning 阈值与审批人已确认

签字：产品 <...>；前端 <...>；后端 <...>；测试 <...>；Tech Lead <...>
```

## C-05

**实例化完成/豁免记录**

```yaml
customizationGate:
  checkedAt: <ISO-8601>
  checkedBy: <identity/name>
  completedSections: [<1,2,...>]
  exemptions:
    - item: <未完成项>
      reason: <原因>
      risk: <风险>
      owner: <负责人>
      due: <日期/版本>
  decision: READY | NOT_READY
```

完成检查：豁免不掩盖 P0 门禁，且所有风险都有 Owner。

## C-06

**Schema 字段评审**

```markdown
| Schema | 新增/修改字段 | 必填性 | 使用角色 | 兼容性影响 | 示例已更新 | 决策 |
|---|---|---|---|---|---|---|
| task-package@1 | <待填写> | <required/optional> | <角色> | <none/minor/major> | <yes/no> | <待填写> |
```

完成检查：
- [ ] JSON Schema 与 Markdown 模板同步
- [ ] Major 变更已升级版本并更新相关 Skill
- [ ] 至少一个正例和一个反例通过校验

## C-07

**Adapter 与隔离方式**

```yaml
runtime:
  adapter: <cursor|cli|ci|generic-agent|other>
  entrypoints:
    start: <待填写>
    resume: <待填写>
    status: <待填写>
isolation:
  mode: <branch|worktree|other>
  branchPattern: <待填写/N/A>
  workspacePattern: <待填写/N/A>
  scopeViolationCheck: <命令/机制>
  cleanupOwner: <身份>
```

完成检查：
- [ ] Adapter 只映射控制协议，不私藏业务规则
- [ ] 并行写任务物理或逻辑隔离
- [ ] start/resume/status 至少经过一次演练
