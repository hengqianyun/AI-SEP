# Migration Review Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认迁移流程。以下命令均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.migration-review
version: 1.0.0
status: active
owner: backend-platform
compatibleRoles: [migrationReviewer]
appliesTo:
  paths: [backend/src/main/resources/db/migration/**]
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-ARCHITECTURE]
---

# Migration Review Skill

## 审查清单

- 文件命名：`VYYYYMMDDHHMM__*.sql` 且序号不冲突
- 是否可重复执行 / 失败可诊断
- 破坏性变更（删列/改类型）是否有回滚或前进修复剧本
- 回填是否与 DDL 分离；大数据是否可分批
- 本地：`./mvnw -f backend/pom.xml flyway:migrate` + `flyway:validate`

## 禁止通过条件

- 无回滚/前进修复说明的破坏性变更
- 直接改已应用的历史迁移文件
- 在应用代码中手写 DDL 绕过 Flyway

## 输出示例

- 迁移：`V202607271200__add_orders_status_index.sql`
- 结论：`APPROVE`（仅加索引，可前进删除索引回滚）
- 验证：`flyway:migrate` PASSED；`flyway:validate` PASSED
```
