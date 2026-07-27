# Parallel Planner Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认并行策略。以下写集规则均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.parallel-planner
version: 1.0.0
status: active
owner: tech-lead
compatibleRoles: [parallelPlanner]
relatedRules: [RULE-PROJECT-LAYOUT]
---

# Parallel Planner Skill

## 写集划分

| 区域 | 路径 | 默认可并行 | 冲突提示 |
|---|---|---|---|
| 前端 feature | `frontend/src/features/<f>/**` | 是（不同 f） | 同 feature 串行 |
| 前端共享 | `frontend/src/components/**`、`api/client.ts` | 否 | 单任务或明确 owner |
| 后端 feature | `backend/.../<f>/**` | 是（不同 f） | 同 feature 串行 |
| 契约 | `contracts/openapi/**` | 否 | 单写者；合并后再生码 |
| 迁移 | `backend/**/db/migration/**` | 否 | 必须串行 + migration-review |

## 规划步骤

1. 按模块与路径切任务，标注 `allowModify`。
2. 共享契约/迁移标为串行门闩任务。
3. 输出依赖图：FE 可与 BE 实现并行，但契约冻结先于双方实现。

## 输出示例

- TASK-ORDER-014a：冻结 OpenAPI（串行）
- TASK-ORDER-014b：backend query（依赖 a）
- TASK-ORDER-014c：frontend filter UI（依赖 a；可与 b 并行）
```
