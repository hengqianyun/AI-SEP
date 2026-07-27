# Developer Skill 填写模板（通用基线）

> Developer / Tech Lead 复制本结构填写 `SKILL.md`。尖括号内容必须替换；不适用项写 `N/A` 及原因。  
> **本 Skill 只写所有 Developer 共用的执行底线**（边界、验证、输出）。  
> 前端、后端等技术栈做法拆成独立 Skill，经 `context-map.yaml` 按路径装载；此处不写具体框架或命令栈。  
> 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）。

## 1. 元数据

```yaml
---
id: skill.developer
version: 1.0.0
status: draft
owner: <Tech Lead 或工程负责人>
inputSchema: task-package@1
compatibleRoles: [developer]
relatedSkills:
  - skill.frontend-developer
  - <skill.backend-developer 如已创建>
---
```

## 2. 适用范围与边界

```markdown
## 适用范围

- 角色：所有 `compatibleRoles: [developer]` 的实现任务
- 技术栈细节来源：按路径装载的独立实现 Skill（前端/后端等）
- 命令工作目录默认：`<仓库根目录；若实现 Skill 另有规定则从其规定>`

## 写边界

- 只修改任务包 `allowModify` 内文件
- 范围不足时升级，不得自行扩大写集
- 共享契约 / 生成文件遵循布局与并行规则，不得绕过
```

## 3. 通用执行工作流

```markdown
## 通用执行工作流

1. 读取任务包、关联 REQ、冻结契约和按路径装载的技术栈 Skill。
2. 确认目标文件位于 `allowModify`。
3. 先明确测试/验收场景，再做最小变更。
4. 执行技术栈 Skill 规定的全部适用验证命令（从低成本到高成本）。
5. 输出变更摘要、REQ 映射、命令与实际结果；环境问题记 `BLOCKED`。
```

## 4. 验证基线（不预设栈）

```markdown
## 验证基线

具体命令以路径装载的实现 Skill 为准。本基线要求：

| 能力 | 要求 | 不适用时 |
|---|---|---|
| 安装/恢复依赖 | 实现 Skill 给出可复制命令 | `N/A` + 原因 |
| 静态检查 | 适用则必须执行 | `N/A` + 原因 |
| 类型/编译检查 | 适用则必须执行 | `N/A` + 原因 |
| 单元测试 | 实现任务默认执行 | 豁免须有批准 |
| 构建 | 按实现 Skill 触发条件 | `N/A` + 原因 |
| 本地启动/健康检查 | 按需 | `N/A` + 原因 |

### 结果规则

- 通过：命令成功且断言通过 → `PASSED`
- 失败：命令或断言失败 → `FAILED`
- 环境/权限/依赖不可用 → `BLOCKED`（**不得宣称通过**）
```

## 5. 禁止事项

```markdown
## 禁止事项

- 不得关闭检查、跳过强制测试或弱化类型来掩盖问题
- 不得硬编码密钥、提交真实环境文件或生产数据
- 不得在未装载匹配实现 Skill 时自行选择技术栈
- 不得将 `BLOCKED` 写成 `PASSED`
- 不得扩大 `allowModify`
```

## 6. 提交审查前自检与输出

```markdown
## 提交审查前自检

- [ ] 变更全部位于 `allowModify`
- [ ] 每个变更关联 REQ
- [ ] 已装载与修改路径匹配的实现 Skill
- [ ] 适用的自测命令已执行并记录结果摘要
- [ ] 未执行项写明原因、风险和解除条件（含 BLOCKED）

## 输出格式

- 任务：`<TASK-ID>`
- 关联需求：`<REQ-ID 列表>`
- 修改文件：`<路径列表>`
- 装载的实现 Skill：`<Skill ID 列表>`
- 实现摘要：`<按需求说明>`
- 已执行验证：`<命令 → PASSED/FAILED/BLOCKED>`
- 未执行验证：`<无，或原因 + 解除条件>`
- 已知风险：`<无，或风险 + Owner>`
```

## 7. 激活检查

- [ ] 边界、验证结果规则与输出格式完整
- [ ] 明确指向前端/后端等独立 Skill，而非在此写入栈细节
- [ ] Tech Lead 确认与 Rule / context-map 装载策略一致
- [ ] Owner 将 `status` 改为 `active`
