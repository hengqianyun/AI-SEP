---
id: RULE-ORG-CODING
version: 1.0.0
status: active
owner: tech-lead
override: allowed-with-adr
appliesTo:
  roles: ["developer", "codeReviewer", "apiDataDesigner"]
reviewBy: 2026-11-05
pilot: WSC
sourceOfTruth:
  backend: backend/CLAUDE.md
  ops: backend/README.md
---

# 编码与 API 约定（WSC / data-chain）

> 后端编码硬约束以独立仓 `backend/CLAUDE.md` 为权威；构建与环境见 `backend/README.md`。  
> 本 Rule 是控制面装载副本；实现仓更新后须同步本文件。

## API

- 风格：REST + JSON；工作台 API 前缀 `/api/v1`（以 `contracts/openapi/` 为准）
- 契约落盘路径：`contracts/openapi/`
- 版本策略：URL 前缀版本；破坏性变更须 ADR + 契约升版
- 错误响应结构：
  - 统一信封：`ApiResponse`（`code` / `message` / `data`）与 WSC `ApiEnvelope`（含 `correlationId`）
  - 实现：`com.shdata.datachain.common.response.*`、`GlobalExceptionHandler`
  - 脱敏示例：`{"code":401,"message":"未登录或会话已失效","data":null}`（具体字段以契约为准）
- 鉴权：服务端 Session + RBAC（`common/security`）；写接口必须鉴权，禁止 Controller 手写旁路鉴权
- 分页：请求/响应字段以冻结 OpenAPI 为准
- 兼容策略：新增可选字段为兼容；删字段、改语义、改枚举为破坏性

## 日志

- 必填关联：`correlationId`（`CorrelationIdSupport` / `wsc.correlationId`）
- 禁止字段：密码、会话全文、数据库连接串、真实密钥
- 级别：`error` 未捕获异常；`warn` 可恢复拒绝；`info` 关键状态；`debug` 仅本地

## 前端编码约定

细则见 `skill.frontend-developer` 与 `RULE-PROJECT-LAYOUT` 前端节。摘要：

- 业务代码：`frontend/src/features/<feature>/`
- 组件：Vue 3 `<script setup lang="ts">`
- API 基址仅经 `VITE_API_BASE_URL`；不得硬编码后端 Origin

## 代码（后端）

权威包结构见 `RULE-PROJECT-LAYOUT`；以下为编码行为硬约束（源自 `backend/CLAUDE.md`）。

### 复用与抽象

- **复用优先**：尽量复用现有方法，避免重复实现
- **独立能力抽取**：可独立复用/测试的逻辑必须单独成方法，不得埋在大段代码块中

### 分层职责

- **Controller 只做编排**：调用顺序、判断与组合；业务逻辑一律在 Service
- **禁止** Controller 直接访问 Repository / EntityManager
- 分层：`controller` → `service` → `repository`（实体 `entity`，DTO/枚举 `model`）

### 命名

- 见名知意，避免自造缩写；通用缩写（URL、DTO、ID、VO）可保留
- Java：包全小写；类 PascalCase；方法/字段 camelCase；常量 UPPER_SNAKE

### 文档与注释

- **Javadoc 必写**：每个方法须有 Javadoc（功能、入参、出参）
- **逻辑注释**：关键分支与非显而易见逻辑须注释
- **功能清单（Tips）**：每个 Service 根目录编写以 `Tips` 开头的功能清单（固定格式，便于搜索）

### 大段代码编写流程（顺序铁律）

1. 先写伪代码 `//TODO`，保证整体逻辑清晰
2. 再补注释
3. 最后填充伪代码实现  
不得跳步；输出长度受限时优先保证 Javadoc、逻辑注释与伪代码框架完整。

### 排错顺序

出错时依次排查：**自己的代码 → 依赖的代码 → 环境**。

### 数据库使用

- 慎用数据库；确需建表/改表时严格遵守 Flyway 规则（见 `RULE-PROJECT-ARCHITECTURE` 与 `skill.migration-review`）
- `spring.jpa.hibernate.ddl-auto` 固定 `validate`；禁止 Hibernate 自动建表

### 兜底（不可妥协）

- 不得以「简化代码」或「性能更好」为由违反本 Rule / `backend/CLAUDE.md`
- 有疑问先升级用户 / Tech Lead，不得擅自豁免

## 审查时不可因「个人风格」阻塞的事项

- 已由 formatter / Checkstyle（若启用）统一的空白与导入顺序
- 未写入本 Rule 且无用户影响的主观命名润色（在见名知意前提下）

## 例外

| 规则 | 允许条件 | 批准身份 | 必须留下的证据 |
|---|---|---|---|
| 包结构 / Flyway / 软删硬约束 | ADR 批准且任务包显式引用 | techLead / 架构委员会 | `design/decisions/` 中 APPROVED ADR |
| 物理 DELETE | 关系短表等 CLAUDE 明确豁免 | techLead | 任务包说明 + 代码注释 |
