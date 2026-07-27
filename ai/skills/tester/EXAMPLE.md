# Tester Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认测试方案。以下路径与命令均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.tester
version: 1.0.0
status: active
owner: qa-lead
outputSchema: test-evidence@1
compatibleRoles: [tester]
appliesTo:
  paths: ["frontend/**", "backend/**", "e2e/**"]
relatedRules: [RULE-ORG-STACK, RULE-ORG-RELEASE]
relatedSkills: [skill.qa-strategist, skill.frontend-developer, skill.backend-developer]
---

# Tester Skill

## 环境

- 工作目录：仓库根目录
- 依赖服务：PostgreSQL（Testcontainers 或 `docker compose -f deploy/local/docker-compose.yml up -d db`）
- 环境变量样例：`e2e/.env.example`；禁止真实密钥
- 测试账号 / Fixture：`e2e/fixtures/`、`backend/src/test/resources/fixtures/`
- 运行时：Node 见 `.nvmrc`；Java 见 `.sdkmanrc`

## 前置条件

- 前端：`pnpm install --frozen-lockfile`
- 后端：`./mvnw -f backend/pom.xml -q -DskipTests dependency:resolve`
- DB 健康检查失败时记 `BLOCKED`，不得写成 `PASSED`

## 执行工作流

1. 读取任务包、变更摘要与实现侧已跑命令。
2. 按 `skill.qa-strategist` 分层决定 unit / integration / e2e。
3. 执行适用命令；失败即停并记录复现步骤。
4. 填写 `ai/schemas/templates/test-evidence.md`，每条证据含命令与结果。

## 验证矩阵

| 层级 | 命令 | 触发条件 | 通过标准 |
|---|---|---|---|
| 前端相关单测 | `pnpm --filter frontend test --run <spec>` | FE 行为变化 | 退出码 0 |
| 后端相关单测 | `./mvnw -f backend/pom.xml -Dtest=<Class> test` | BE 行为变化 | 退出码 0 |
| 后端集成 | `./mvnw -f backend/pom.xml -Pintegration verify` | API/数据访问 | 退出码 0 |
| E2E | `pnpm --filter e2e test --grep <用例>` | P0 用户路径 | 退出码 0 |
| 冒烟 | `pnpm --filter e2e smoke` | 集成审查前 | 关键路径 PASSED |

## 证据约定

- 模板：`ai/schemas/templates/test-evidence.md`
- 每条必须含：层级、命令、结果、关联 REQ、环境说明
- `BLOCKED` 必须写解除条件（例如「staging DB 不可达」）

## 禁止事项

- 环境不可用时不得标记 `PASSED`
- 不得用生产数据或真实用户凭据
- 不得跳过 P0 路径的强制层

## 输出示例

- 任务：`TASK-ORDER-014`
- 关联需求：`REQ-ORDER-007`
- 已执行：
  - `pnpm --filter frontend test --run OrderListPage.spec.ts` → `PASSED`
  - `./mvnw -f backend/pom.xml -Dtest=OrderQueryServiceTest test` → `PASSED`
- 未执行：E2E（等待 staging）；状态 `BLOCKED`；解除条件：staging 部署完成
- 结论：实现侧单测通过；发布前仍需 E2E
```
