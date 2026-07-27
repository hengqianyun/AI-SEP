# API/Data Designer Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认契约方案。以下路径均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.api-data-designer
version: 1.0.0
status: active
owner: backend-platform
compatibleRoles: [apiDataDesigner]
appliesTo:
  paths: [contracts/openapi/**, backend/src/main/resources/db/migration/**]
relatedRules: [RULE-ORG-CODING, RULE-ORG-STACK]
---

# API/Data Designer Skill

## 契约落盘

- OpenAPI：`contracts/openapi/orders.yaml` 等按域拆分
- 版本策略：路径前缀 `/api/v1`；破坏性变更升 minor 路径并保留旧版至下两个发布窗口
- 字段命名：camelCase JSON；与 DB snake_case 映射仅在 infrastructure

## 兼容策略

- 仅新增可选字段 / 新增枚举值：兼容
- 删字段、改类型、收紧必填：破坏性，须 ADR + 迁移计划
- 每次契约 PR 必须跑 `ci/openapi-diff`

## 数据与迁移

- 表变更经 Flyway：`backend/src/main/resources/db/migration/`
- 回填脚本与结构脚本分离；大数据回填须可暂停/可重入
- 与 `skill.migration-review` 联动：含 `schema-migration` 标签必审

## 输出示例

- 变更：`GET /api/v1/orders` 增加可选 query `status`
- 兼容性：兼容
- 文件：`contracts/openapi/orders.yaml`
- 同步：后端 Controller 参数与 FE `features/orders/api.ts`
```
