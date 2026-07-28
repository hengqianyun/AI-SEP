---
id: skill.tester
version: 1.0.0
status: active
owner: tech-lead
outputSchema: test-evidence@1
compatibleRoles: [tester]
appliesTo:
  paths: ["frontend/**", "backend/**", "e2e/**", "tests/**"]
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-LAYOUT]
relatedSkills: [skill.frontend-developer, skill.backend-developer]
pilot: WSC
---

# Tester Skill（WSC 试点）

## 环境

- 工作目录：仓库根目录
- 依赖服务：PostgreSQL（Testcontainers 或本地 docker compose）
- 环境变量样例：`e2e/.env.example`、`frontend/.env.example`、`backend/.../application-local.example.yml`
- 禁止真实密钥与生产数据

## 前置条件

- 前端：`pnpm install --frozen-lockfile`
- 后端：`./mvnw -f backend/pom.xml -q -DskipTests dependency:resolve`
- DB/服务健康检查失败时记 `BLOCKED`，不得写成 `PASSED`

## 执行工作流

1. 读取任务包、变更摘要与实现侧已跑命令。
2. 按 P0 路径优先执行适用层（unit → integration → e2e）。
3. 失败即停并记录复现步骤。
4. 填写 `ai/schemas/templates/test-evidence.md`。

## 验证矩阵

| 层级 | 命令 | 触发条件 | 通过标准 |
|---|---|---|---|
| 前端相关单测 | `pnpm --filter frontend test --run <spec>` | FE 行为变化 | 退出码 0 |
| 后端相关单测 | `./mvnw -f backend/pom.xml -Dtest=<Class> test` | BE 行为变化 | 退出码 0 |
| 后端集成 | `./mvnw -f backend/pom.xml -Pintegration verify` | API/数据访问 | 退出码 0 |
| E2E | `pnpm --filter e2e test --grep <用例>` | P0 用户路径 | 退出码 0 |

## P0 路径关注（WSC）

- 角色权限：管理员 / 提供方 / 普通用户写入口差异与 API 403
- 目录筛选检索 → 详情 → 上链信息
- 新增/编辑产品产生版本化存证记录
- 有产品的分类禁止删除

## 禁止事项

- 环境不可用时不得标记 `PASSED`
- 不得用生产数据或真实用户凭据
- 不得跳过 P0 路径的强制层
