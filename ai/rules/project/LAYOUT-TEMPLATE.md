# RULE-PROJECT-LAYOUT 填写模板

> 已填写示例：[LAYOUT-EXAMPLE.md](./LAYOUT-EXAMPLE.md)（仅粒度参考，非默认）
> Tech Lead 复制本结构填写 `RULE-PROJECT-LAYOUT.md`，并与 `modules.yaml` 路径对齐。尖括号内容必须替换。  
> 路径必须来自真实仓库；不得假定 `frontend/`、`backend/` 一定存在。无某侧交付时写 `N/A` 及原因。

## 1. 元数据

```yaml
---
id: RULE-PROJECT-LAYOUT
version: <语义化版本，如 1.0.0>
status: draft
owner: <techLead 或等价角色>
override: allowed
appliesTo:
  roles: ["parallelPlanner", "developer", "codeReviewer", "orchestrator"]
reviewBy: <YYYY-MM-DD 或 TBD>
---
```

## 2. 顶层目录

按仓库实际增删行；下表仅为结构示例，填写时换成真实路径。

```markdown
## 顶层目录

| 路径 | 职责 | 默认可写角色 |
|---|---|---|
| `product/` | 需求与业务 | `<角色>` |
| `design/` | 设计决策与 UI | `<角色>` |
| `planning/` | 计划、任务、评审 | `<角色>` |
| `<前端根路径或 N/A>` | 前端实现 | `<角色>` |
| `<后端根路径或 N/A>` | 后端实现 | `<角色>` |
| `<测试根路径>` | 测试与证据 | `<角色>` |
| `ai/` | Agent 契约与控制面 | `<角色；规则变更须人类审批>` |
```

## 3. 前端目录与写边界

无前端时整节 `N/A`。

```markdown
## 前端布局与写边界

| 模块代码 | 业务名称 | 可修改路径 glob | 只读/共享路径 | 禁止修改路径 | Owner | 冲突升级角色 |
|---|---|---|---|---|---|---|
| `<MODULE>` | `<名称>` | `<真实路径 glob>` | `<路径或 N/A>` | `<路径或 N/A>` | `<角色>` | `<角色>` |

### 前端共享边界
- 共享契约/类型：`<路径>`；修改条件：`<条件>`
- 共享组件/资源：`<路径>`；修改条件：`<条件>`
- 生成文件：`<路径或 N/A>`；生成来源：`<命令/流程>`
```

## 4. 后端目录与写边界

无后端独立树时按实际 monorepo/服务目录填写。

```markdown
## 后端布局与写边界

| 路径/glob | 内容职责 | Owner | 允许写入角色 | 共享/独占 | 并行写规则 |
|---|---|---|---|---|---|
| `<真实路径>` | `<职责>` | `<角色>` | `<角色列表>` | `<共享/独占>` | `<串行/拆分/升级>` |
```

## 5. 默认 denyModify 与共享契约

```markdown
## 默认 denyModify

- `<路径 glob>`：`<原因与唯一可写身份>`
- 生产密钥与环境私密配置：`<路径>`

## 共享契约路径（并行时须独立前置任务拥有）

| 路径 | Owner | 并行写 | 变更协议 |
|---|---|---|---|
| `<如 planning/api-design/ 或 packages/contracts/>` | `<角色>` | forbidden | `<锁定/协调/评审>` |

## 多任务写冲突处理

- `<串行、拆分 allowModify 或升级规则>`
```

## 6. 完成检查

- [ ] 所有路径来自实际仓库，无假定目录
- [ ] 前端/后端写边界完整，或已 `N/A`
- [ ] 共享、只读、生成和禁止修改边界明确
- [ ] 与 `modules.yaml` 的 `code` / `paths` 一致
- [ ] `parallelPlanner` 可据此形成无歧义的 `allowModify`
- [ ] Owner 将 `status` 改为 `active`
