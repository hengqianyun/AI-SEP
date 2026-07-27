# Tester Skill 填写模板

> 测试负责人复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> 本 Skill 只写“怎么测与怎么记证据”；技术选型应引用 Organization Rule 与各实现 Skill。  
> **环境不可用时记 `BLOCKED`，绝不可写成 `PASSED`。**  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.tester
version: 1.0.0
status: draft
owner: <测试负责人或团队>
outputSchema: test-evidence@1
compatibleRoles: [tester]
appliesTo:
  paths:
    - <适用测试路径 glob>
relatedRules:
  - <适用 Rule ID>
relatedSkills:
  - skill.qa-strategist
  - <frontend / backend developer Skill>
---
```

- `owner` 应是可批准本 Skill 的角色/团队。
- 命令须与 QA Strategist 分层策略及实现 Skill 验证矩阵一致。

## 2. 环境与前置条件

```markdown
## 环境

- 工作目录：`<仓库根目录或子目录>`
- 依赖服务：`<名称、启动方式、健康检查；无则 N/A>`
- 环境变量样例：`<示例文件路径；禁止真实密钥>`
- 测试账号 / Fixture：`<路径；禁止真实生产数据>`
- 运行时版本来源：`<约束文件或 Rule 引用>`

## 前置条件

- `<依赖安装、代码生成、种子数据等步骤>`
- 阻塞时必须记录解除条件，不得宣称通过
```

## 3. 执行工作流

```markdown
## 执行工作流

1. 读取任务包、REQ、风险标签与适用测试层。
2. 确认环境就绪；未就绪则整层或整任务记 `BLOCKED`。
3. 按命令矩阵从低成本到高成本执行。
4. 每层记录命令、退出码、产物路径与关联 REQ。
5. 输出符合 `test-evidence` 的证据；`BLOCKED ≠ PASSED`。
```

## 4. 命令矩阵

```markdown
## 命令矩阵

所有命令从 `<工作目录>` 执行。

| 层 | 命令 | 必须执行条件 | 通过标准 | 结果取值 |
|---|---|---|---|---|
| 安装依赖 | `<命令>` | 首次/锁文件变化 | 退出码 0 | PASSED/FAILED/BLOCKED |
| 静态/类型 | `<命令>` | 每任务 | 退出码 0，新增问题为 0 | PASSED/FAILED/BLOCKED |
| 单元 | `<命令，可带路径>` | 每实现任务 | 退出码 0 | PASSED/FAILED/BLOCKED |
| 集成 | `<命令>` | `<触发条件>` | 退出码 0 | PASSED/FAILED/BLOCKED |
| 契约 | `<命令或 N/A>` | `<触发条件>` | 退出码 0 | PASSED/FAILED/BLOCKED |
| E2E | `<命令>` | `<触发条件>` | 退出码 0 且关键断言通过 | PASSED/FAILED/BLOCKED |
| 清理 | `<命令或 N/A>` | 测试后 | 退出码 0 | — |
```

## 5. 结果判定规则

```markdown
## 结果规则

| 状态 | 判定条件 | 禁止事项 |
|---|---|---|
| PASSED | 退出码 0 且断言通过 | 不得在环境缺失时使用 |
| FAILED | 命令失败或断言失败 | 不得改写为 SKIPPED 掩盖 |
| BLOCKED | 环境/权限/依赖不可用 | **不得记为 PASSED**；必须写 `blockedBy` 与 `unblockWhen` |
| SKIPPED | 有书面豁免且不在强制层 | 不得计作通过证据 |
```

`qualityGates.blockedIsPass` 必须为 `false`（或等价项目策略）。

## 6. 证据约定

```markdown
## 证据约定

- 模板：`ai/schemas/templates/test-evidence.md`（或 schema 等价结构）
- 证据 ID：`EVID-<TASK-ID>-<NNN>`
- 每层必含：命令、结果、关联 REQ、产物路径
- BLOCKED 必含：阻塞原因、解除条件、Owner
- 禁止：真实生产敏感数据、密钥明文
```

证据字段骨架：

```yaml
evidenceId: EVID-<TASK-ID>-<NNN>
taskId: <TASK-ID>
status: PASSED | FAILED | BLOCKED
environment:
  name: <环境名>
  version: <版本或构建标识>
layers:
  - name: <unit/integration/e2e/...>
    command: <实际执行命令>
    result: PASSED | FAILED | BLOCKED | SKIPPED
    relatedReqs: [<REQ-ID>]
    artifact: <日志/报告路径>
    notes: <说明>
blockedBy: <仅 BLOCKED 时必填>
unblockWhen: <仅 BLOCKED 时必填>
```

## 7. 禁止事项与例外

```markdown
## 禁止事项

- 不得将 `BLOCKED` 或未执行写成 `PASSED`
- 不得跳过强制层测试却声称任务完成
- 不得使用真实生产敏感数据作 Fixture
- 不得关闭检查或弱化断言来掩盖失败

## 例外

| 规则 | 允许条件 | 批准身份 | 必须留下的证据 |
|---|---|---|---|
| 跳过某强制层 | `<条件>` | `<身份>` | `<豁免记录路径>` |
```

## 8. 测试者输出

```markdown
## 提交前自检

- [ ] 适用命令均已执行或明确 BLOCKED/SKIPPED
- [ ] 每条证据关联 REQ
- [ ] BLOCKED 含解除条件
- [ ] 无真实密钥或生产数据

## 输出格式

- 任务：`<TASK-ID>`
- 关联需求：`<REQ-ID 列表>`
- 已执行层：`<层 → PASSED/FAILED/BLOCKED/SKIPPED>`
- 证据路径：`<路径列表>`
- 阻塞项：`<无，或原因 + 解除条件>`
- 已知风险：`<无，或风险 + Owner>`
```

## 9. 激活检查

- [ ] 测试负责人完成所有占位并实际试跑命令
- [ ] `BLOCKED`/`FAILED`/`PASSED` 判定互斥且可审计
- [ ] 与 QA Strategist、实现 Skill 命令一致
- [ ] Owner 将 `status` 改为 `active`
