# QA Strategist Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认质量策略。以下分层与门槛均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.qa-strategist
version: 1.0.0
status: active
owner: qa-lead
compatibleRoles: [qaStrategist]
relatedSkills: [skill.tester, skill.frontend-developer, skill.backend-developer]
---

# QA Strategist Skill

## 测试分层

| 层级 | 目标 | 强制时机 | 主要入口 |
|---|---|---|---|
| Unit | 纯逻辑、映射、权限条件 | 每实现任务相关变更 | FE Vitest / BE JUnit |
| Integration | API + DB 边界 | 契约或数据访问变化 | `./mvnw -f backend/pom.xml -Pintegration verify` |
| E2E | P0 用户路径 | P0 REQ 或关键下单路径变化 | `pnpm --filter e2e test` |
| 冒烟 | 晋级前关键 | 集成审查 / staging→prod | `pnpm --filter e2e smoke` |

## 覆盖门槛

- 所有 P0 REQ 必须有可追溯自动化或书面豁免（Tech Lead + QA 双签）
- 共享模块变更合并前必须全量单测
- 无测试证据不得将发布门禁标为完成

## 规划评审关注点

- 任务包是否标明测试层级与环境依赖
- 风险标签（鉴权/PII/迁移）是否映射到强制层
- 并行任务是否避免共享契约无主修改

## 输出示例

- 计划轮次建议：订单筛选属 P1，强制 unit；若改 OpenAPI 则加 integration
- 发布阻断条件：缺 E2E 证据且路径为结账主流程
```
