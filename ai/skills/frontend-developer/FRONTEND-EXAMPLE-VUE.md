# Frontend Developer Skill 已填写示例（Vue 3）

> 仅展示填写粒度，不代表 AI-SEP 默认技术栈。以下路径与命令均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.frontend-developer
version: 1.0.0
status: active
owner: frontend-platform
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: [frontend/**]
relatedRules: [RULE-ORG-STACK, RULE-ORG-CODING, RULE-PROJECT-LAYOUT]
---

# Frontend Developer Skill

## 适用范围

- 应用：`frontend/`
- 技术栈：以 `RULE-ORG-STACK` 为准
- 包管理器：pnpm；锁文件：`pnpm-lock.yaml`
- Node.js：读取根目录 `.nvmrc`
- 工作目录：仓库根目录

## 前置条件

- 执行 `pnpm install --frozen-lockfile`
- 从 `frontend/.env.example` 创建本地变量；不得提交真实密钥
- API 地址仅通过 `VITE_API_BASE_URL` 注入

## 实现工作流

1. 读取任务包、REQ、冻结 API/UI 契约与适用 Rule。
2. 确认修改路径均在 `allowModify`。
3. 页面参考 `frontend/src/features/orders/pages/OrderListPage.vue`。
4. 新组件默认使用 Vue 3 `<script setup lang="ts">`；既有 Options API 文件保持原风格。
5. 补充测试或明确验收场景后实施最小变更。
6. 执行验证矩阵并报告实际结果。

## 验证矩阵

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 安装依赖 | `pnpm install --frozen-lockfile` | 首次/锁文件变化 | 退出码 0 |
| 格式检查 | `pnpm --filter frontend format:check` | 每任务 | 退出码 0 |
| Lint | `pnpm --filter frontend lint` | 每任务 | 退出码 0 |
| 类型检查 | `pnpm --filter frontend typecheck` | 每任务 | 退出码 0 |
| 相关测试 | `pnpm --filter frontend test --run <测试文件>` | 每任务 | 退出码 0 |
| 全量单测 | `pnpm --filter frontend test --run` | 共享模块/合并前 | 退出码 0 |
| 构建 | `pnpm --filter frontend build` | 每任务 | 退出码 0 |
| 本地启动 | `pnpm --filter frontend dev` | 浏览器验证 | Vite 启动且无阻塞错误 |
| E2E | 见 `skill.tester` | P0 用户路径变化 | tester 证据为 PASSED |

## 目录与实现约定

### 页面与路由
- 业务代码放在 `frontend/src/features/<feature>/`
- 页面放在 `pages/`，模块路由放在 `routes.ts`
- 路由按现有模式懒加载，鉴权只通过 `frontend/src/router/guards/`

### 组件与样式
- 全局组件：`frontend/src/components/`
- Feature 组件：`frontend/src/features/<feature>/components/`
- Props/Emits 必须声明 TypeScript 类型；默认值使用 `withDefaults`
- `v-for` 使用稳定业务 ID，不使用可变索引作为 key
- 样式遵循现有 scoped CSS 与 Design Token

### 状态与 API
- 跨页面状态使用现有 Pinia store；局部状态留在组件内
- API 请求统一经 `frontend/src/api/client.ts`
- Feature API 放在 `frontend/src/features/<feature>/api.ts`
- 组件不得拼接后端 URL；`computed` 保持纯函数

### 用户体验
- 异步页面必须提供 loading、empty、error、success 状态
- 表单提交期间禁用重复提交
- 交互控件必须有可访问名称，核心操作支持键盘

## 测试约定

- 单元/组件测试与源码同目录，命名 `*.spec.ts`
- 公共 Fixture：`frontend/tests/fixtures/`
- Mock 网络边界，不 Mock 被测组件内部实现
- 状态分支、数据映射、权限条件和缺陷修复必须补测试
- 仅静态文案变更可免自动化测试，但须记录人工验证证据

## 禁止事项

- 不得引入第二套状态管理、HTTP Client、表单方案或设计系统，除非 ADR 已批准
- 不得使用 `any`、`@ts-ignore`、关闭 Lint 或跳过测试掩盖错误
- 不得提交 `dist/`、真实 `.env`、调试日志或无关格式化
- 不得扩大 `allowModify`

## 输出示例

- 任务：`TASK-ORDER-014`
- 关联需求：`REQ-ORDER-007`
- 修改文件：
  - `frontend/src/features/orders/pages/OrderListPage.vue`
  - `frontend/src/features/orders/pages/OrderListPage.spec.ts`
- 实现摘要：增加订单状态筛选并与 URL query 同步
- 已执行验证：
  - `pnpm --filter frontend lint` → `PASSED`
  - `pnpm --filter frontend typecheck` → `PASSED`
  - `pnpm --filter frontend test --run OrderListPage.spec.ts` → `PASSED`
  - `pnpm --filter frontend build` → `PASSED`
- 未执行验证：E2E，由 tester 在集成环境执行
- 已知风险：无
```
