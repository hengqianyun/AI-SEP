# Integration Review Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认集成门禁。以下命令均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.integration-review
version: 1.0.0
status: active
owner: maintainer
compatibleRoles: [integrationReviewer]
relatedSkills: [skill.tester]
relatedRules: [RULE-ORG-RELEASE]
---

# Integration Review Skill

## 冒烟路径

1. 启动：`docker compose -f deploy/local/docker-compose.yml up -d`
2. 后端健康：`curl -sf http://localhost:8080/actuator/health`
3. 前端：`pnpm --filter frontend dev`；打开订单列表
4. 契约：确认 FE 调用的 path/query 与 `contracts/openapi/orders.yaml` 一致
5. E2E 冒烟：`pnpm --filter e2e smoke`

## 通过标准

- 健康检查 UP；冒烟用例全 PASSED
- 无未关闭 P0；P1 有 Owner 与解除计划
- 发布相关检查与 `RULE-ORG-RELEASE` 一致

## 输出示例

- 轮次：集成审查 #3
- 结果：`PASSED`
- 证据：`e2e smoke` 日志链接（示例环境）
- 残留风险：无
```
