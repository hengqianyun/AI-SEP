---
id: RULE-PROJECT-LAYOUT
version: 1.2.0
status: active
owner: techLead
override: allowed
appliesTo:
  roles: ["parallelPlanner", "developer", "codeReviewer", "orchestrator"]
reviewBy: 2026-11-05
pilot: WSC
sourceOfTruth:
  backend: backend/CLAUDE.md
---

# 目录与模块地图（WSC 试点）

## 顶层目录

| 路径 | 职责 | 默认可写角色 |
|---|---|---|
| `product/` | 需求与业务 | productAnalyst, planEditor（制品） |
| `design/` | 设计决策、UI、原型 | uxUiPlanner, solutionArchitect |
| `design/prototypes/` | 可交互 HTML 等原型参考 | productOwner, uxUiPlanner |
| `planning/` | 计划、任务、评审 | 规划委员会 |
| `frontend/` | 前端实现（亦可推送独立仓 `data-chain-static`，见 `repos.yaml`） | developer（任务 allowModify 内） |
| `backend/` | 后端实现（Maven 多模块；亦可推送独立仓 `data-chain-backend`） | developer（任务 allowModify 内） |
| `contracts/` | OpenAPI 等共享契约 | apiDataDesigner, developer（契约任务） |
| `tests/` / `e2e/` | 测试与证据 | developer, tester |
| `ai/` | Agent 契约与控制面 | orchestrator；规则变更须人类审批 |

## 前端布局与写边界

| 模块代码 | 业务名称 | 可修改路径 glob | Owner | 冲突升级角色 |
|---|---|---|---|---|
| SHELL | 壳层导航 | `frontend/src/features/shell/**`, `frontend/src/layouts/**`, `frontend/src/router/**` | techLead | techLead |
| RBAC | 鉴权前端 | `frontend/src/features/auth/**` | techLead | techLead |
| OVW | 总览 | `frontend/src/features/overview/**` | techLead | techLead |
| CAT | 数据目录 | `frontend/src/features/catalog/**` | techLead | techLead |
| CHAIN | 上链信息 | `frontend/src/features/chain/**` | techLead | techLead |

### 前端共享边界

- 共享 API client：`frontend/src/api/`；修改须独立任务或与契约同步
- 共享组件：`frontend/src/components/`；跨 feature 变更串行

## 后端布局（data-chain 多模块）

模块根（权威；独立仓 `data-chain-backend`，本地 `backend/`；DEC-WSC-005）：

```text
backend/
├── pom.xml
└── app/
    └── data-chain-service/          # 唯一服务模块
        ├── pom.xml
        └── src/
            ├── main/
            │   ├── java/com/shdata/datachain/
            │   └── resources/
            │       ├── application.yml
            │       └── sql/
            │           ├── init/       # 空库建库 + Flyway baseline
            │           └── migration/ # 业务表与初始数据（Flyway）
            └── test/
```

### Java 包结构（按类型分层 — 硬约束）

权威说明：`backend/CLAUDE.md`「包结构规则」。**禁止**按业务域创建顶层包（如顶层 `catalog/`、`chain/`、`overview/`、`security/`、`rbac/`、`demo/`）。

```text
com.shdata.datachain/
├── controller/          ← @RestController，只做编排
│   └── {域}/{子域}/     ← 例 controller/catalog/admin/
├── service/             ← @Service，全部业务逻辑
│   └── {域}/{子域}/
├── repository/          ← JPA Repository + Store（不按域拆顶层）
├── entity/              ← @Entity（继承 BaseEntity）
├── model/               ← record / DTO / 枚举（纯数据）
├── config/              ← @Configuration
├── common/              ← 至少两模块共享的基础设施
│   ├── entity/          ← BaseEntity
│   ├── exception/
│   ├── response/        ← ApiResponse、ServiceResult
│   ├── constant/
│   ├── support/
│   ├── security/        ← RBAC / 鉴权组件
│   ├── port/            ← 端口与适配器（如 ChainAttestationPort）
│   ├── codec/
│   └── mapper/
└── DataChainApplication.java
```

包结构铁律：

1. 子包以域名为前缀区分，如 `controller.catalog.admin`、`service.chain`
2. 纯数据类一律 `model/`；Repository/Store 一律 `repository/`
3. `common/` 只放跨模块共享代码；单模块工具放对应 `service/` 或 `repository/`
4. 新建 Java 文件前必须对照本结构选路径；移动文件须同步 package/import/测试，并通过 `mvn compile` + `mvn test-compile`

## 后端写边界

并行规划按**域子包**拆分写集（类型层固定，域在 `controller|service` 下）：

| 路径/glob | 内容职责 | Owner | 允许写入角色 | 并行写规则 |
|---|---|---|---|---|
| `.../controller/security/**`、`.../service/security/**`、`.../common/security/**` | 会话与 RBAC | techLead | developer | 与 SHELL 协调 |
| `.../controller/overview/**`、`.../service/overview/**` | 总览指标 | techLead | developer | 可读 catalog 聚合 |
| `.../controller/catalog/**`、`.../service/catalog/**` | 目录与产品 | techLead | developer | 同域独占写集 |
| `.../controller/chain/**`、`.../service/chain/**`、`.../common/port/**` | 存证适配 | techLead | developer | 与 CAT 编辑同事务时串行 |
| `.../entity/**`、`.../model/**`、`.../repository/**` | 共享模型与仓储 | techLead | developer | 跨域变更串行或独立任务 |
| `.../common/**`（除 port/security 上列） | 共享基础设施 | techLead | developer | 串行；优先小 diff |
| `backend/app/*/src/main/resources/sql/migration/**` | Schema 迁移 | techLead | developer | 串行；触发 migrationReviewer |
| `backend/app/*/src/main/resources/sql/init/**` | 空库 init / baseline | techLead | developer | 串行；与 migration 同属 schema 任务 |

> 上表 `...` = `backend/app/data-chain-service/src/main/java/com/shdata/datachain`。  
> 遗留路径 `backend/src/main/resources/db/migration/**`：仅 V1.0 历史；新迁移禁止写入。

## 默认 denyModify

- `ai/rules/global/**`：仅平台
- `ai/workflow/definition.yaml`：仅流程变更审批后
- 生产密钥与环境私密配置：`**/.env`、`**/application-local.yml`、`**/application-prod.yml`

## 共享契约路径（并行时须独立前置任务拥有）

| 路径 | Owner | 并行写 | 变更协议 |
|---|---|---|---|
| `contracts/openapi/` | apiDataDesigner | 否 | 变更后 openapi-diff；前后端任务依赖契约任务完成 |
