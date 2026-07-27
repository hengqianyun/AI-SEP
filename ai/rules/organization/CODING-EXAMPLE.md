# RULE-ORG-CODING 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认技术栈。以下路径与字段名均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: RULE-ORG-CODING
version: 1.0.0
status: active
owner: architecture-committee
override: allowed-with-adr
appliesTo:
  roles: ["developer", "codeReviewer", "apiDataDesigner"]
reviewBy: 2026-07-15
---

# RULE-ORG-CODING

## API

- 风格：REST + JSON；资源路径名词复数、kebab-case
- 契约落盘路径：`contracts/openapi/acme-orders-v1.yaml`
- 版本策略：URL 前缀 `/api/v1/`；破坏性变更升 `/api/v2/` 并保留 v1 兼容期
- 错误响应结构：
  - 字段：`code`（字符串）、`message`（用户可读）、`correlationId`（UUID）、`details`（可选字段级数组）
  - 错误码体系：`<DOMAIN>_<NNN>`，如 `ORDER_001`；稳定标识不得复用
  - 脱敏示例：
    ```json
    {
      "code": "ORDER_001",
      "message": "订单状态不允许此操作",
      "correlationId": "550e8400-e29b-41d4-a716-446655440000",
      "details": [{ "field": "status", "reason": "must_be_PENDING_PAYMENT" }]
    }
    ```
- 鉴权头 / Token 约定：`Authorization: Bearer <JWT>`；JWT 由 `backend` 签发，前端不得解析 payload 做授权
- 分页约定：请求 `page`（从 0 起）、`size`（默认 20，最大 100）；响应 `{ "content": [], "page": 0, "size": 20, "totalElements": 123 }`
- 兼容策略：新增可选字段为兼容；删字段、改语义、改状态枚举为破坏性，须 ADR + OpenAPI 版本升级

## 日志

- 必填字段：`timestamp`、`level`、`correlationId`、`service`（固定 `acme-orders-api`）、`message`
- 可选字段：`userId`（仅内部 ID，禁止邮箱/手机号）
- 禁止字段：密码、JWT 全文、银行卡号、完整收货地址
- 级别与用途：`error` 未捕获异常；`warn` 可恢复业务拒绝；`info` 关键状态变更；`debug` 仅本地
- 关联规则：HTTP 入口从 `X-Correlation-Id` 读取或生成，写入 MDC；出站调用透传同 ID

## 前端编码约定

### 文件与目录
- 页面/入口：`frontend/src/features/<feature>/pages/*.vue`
- 可复用组件：`frontend/src/components/`（无业务语义）
- 业务组件：`frontend/src/features/<feature>/components/`
- 工具、类型、资源：`frontend/src/utils/`、`frontend/src/types/`、`frontend/src/assets/`

### 组件契约
- 命名：PascalCase 文件名与组件名；页面后缀 `*Page.vue`
- 输入/输出：`<script setup lang="ts">` + `defineProps` / `defineEmits`；禁止隐式 any
- 复用边界：跨 2 个以上 feature 且无业务耦合方可提升至 `components/`
- 样式与设计令牌：颜色/间距仅用 `frontend/src/styles/tokens.css` 变量；禁止硬编码 `#hex`

### 状态与数据
- 本地状态边界：单页表单、展开折叠等 UI 态
- 跨页面/全局状态边界：Pinia store，路径 `frontend/src/stores/`
- 服务端数据访问：统一经 `frontend/src/api/client.ts`；feature API 在 `features/<feature>/api.ts`
- 加载、空、错误、成功状态：列表/详情页必须四态齐全；提交按钮 loading 防重复

### 路由与导航
- 路由定义位置：`frontend/src/features/<feature>/routes.ts` 聚合至 `frontend/src/router/index.ts`
- 路由命名/元数据：`name` 用 `FeatureAction`；`meta.requiresAuth` 声明鉴权
- 鉴权与重定向：仅 `frontend/src/router/guards/authGuard.ts`；禁止页面内手写 token 跳转逻辑

## 代码

- 命名：Java 包 `com.acme.orders.*`；类 PascalCase；方法 camelCase；常量 UPPER_SNAKE
- 分层：`controller` → `service` → `domain` → `repository`；DTO 与 Entity 分离
- 数据访问：仅 `repository` 包可注入 JPA Repository；禁止 Controller 直查 EntityManager
- 测试命名与位置：`*Test.java` 与源码同包路径于 `src/test/java/`；集成测试后缀 `*IT.java`

## 审查时不可因「个人风格」阻塞的事项

- Prettier/ESLint（前端）、Spotless/Checkstyle（后端）已统一的格式
- import 顺序若已由工具固定
- 未写入本规则且无用户可见影响的主观命名偏好

## 例外

| 规则 | 允许条件 | 批准身份 | 必须留下的证据 |
|---|---|---|---|
| 新增 direct 依赖 | 无等价内置方案 | techLead | `design/decisions/DEC-<ID>.md` |
| 跳过 OpenAPI 先生成 | 紧急热修且 24h 内补契约 | techLead + maintainer | PR 链接 + 跟进 TASK |
```
