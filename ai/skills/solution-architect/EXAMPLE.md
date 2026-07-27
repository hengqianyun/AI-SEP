# Solution Architect Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认架构。以下 NFR 与边界均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.solution-architect
version: 1.0.0
status: active
owner: tech-lead
compatibleRoles: [solutionArchitect]
relatedRules: [RULE-PROJECT-ARCHITECTURE, RULE-ORG-STACK]
---

# Solution Architect Skill

## 架构边界

- 前端 SPA：`frontend/`；后端单体模块化：`backend/`；契约：`contracts/openapi/`
- 模块关系见 `RULE-PROJECT-ARCHITECTURE`；跨模块写操作须经应用服务，禁止直连他域表
- ADR 入口：`design/decisions/`

## NFR 基线（示例）

| 类别 | 基线 | 验证方式 |
|---|---|---|
| 可用性 | staging/prod 健康检查 `/actuator/health` | 部署流水线 |
| 延迟 | 列表 P95 < 300ms（不含冷启动） | 集成环境抽样 |
| 安全 | 鉴权变更触发 securityReviewer | policies.riskTriggers |
| 可回滚 | 迁移可回滚或可前进修复 | migration-review |

## 规划评审关注点

- 是否引入第二套栈/模式（无 ADR 则否决）
- 写集冲突与共享契约归属是否清晰
- 开放问题是否升级给 Tech Lead / Product Owner

## 输出示例

- 任务包评估：订单筛选仅影响 query 与 UI，无新边界
- 风险：无；不触发 security/migration
- 建议：沿用现有 `OrderQueryService`，勿新建查询总线
```
