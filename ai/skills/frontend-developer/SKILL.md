---
id: skill.frontend-developer
version: 1.0.0
status: active
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: [frontend/**]
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-LAYOUT]
pilot: WSC
---

# Frontend Developer Skill（WSC 试点）

## 适用范围

- 应用：`frontend/`
- 技术栈：以 `RULE-ORG-STACK` 为准（Vue 3 + TypeScript + Vite + pnpm + Pinia + Ant Design Vue）
- 包管理器：pnpm；锁文件：`pnpm-lock.yaml`（脚手架后生成）
- Node.js：读取根目录 `.nvmrc`（目标 20.x）
- 工作目录：仓库根目录

## 前置条件

- 执行 `pnpm install --frozen-lockfile`（无锁文件时首次可用 `pnpm install`）
- 从 `frontend/.env.example` 创建本地变量；不得提交真实密钥
- API 地址仅通过 `VITE_API_BASE_URL` 注入

## 实现工作流

1. 读取任务包、REQ、冻结 API/UI 契约与适用 Rule。
2. 确认修改路径均在 `allowModify`。
3. 业务代码放在 `frontend/src/features/<feature>/`（shell / auth / overview / catalog / chain）。
4. 新组件默认使用 Vue 3 `<script setup lang="ts">`。
5. 补充测试或明确验收场景后实施最小变更。
6. 执行验证矩阵并报告实际结果。

## 验证矩阵

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 安装依赖 | `pnpm install --frozen-lockfile` | 首次/锁文件变化 | 退出码 0 |
| Lint | `pnpm --filter frontend lint` | 每任务 | 退出码 0 |
| 类型检查 | `pnpm --filter frontend typecheck` | 每任务 | 退出码 0 |
| 相关测试 | `pnpm --filter frontend test --run <测试文件>` | 每任务 | 退出码 0 |
| 全量单测 | `pnpm --filter frontend test --run` | 共享模块/合并前 | 退出码 0 |
| 构建 | `pnpm --filter frontend build` | 每任务 | 退出码 0 |
| 本地启动 | `pnpm --filter frontend dev` | 浏览器验证 | Vite 启动且无阻塞错误 |
| E2E | 见 `skill.tester` | P0 用户路径变化 | tester 证据为 PASSED |

## 目录与实现约定

- 页面：`frontend/src/features/<feature>/pages/`
- Feature API：`frontend/src/features/<feature>/api.ts`；统一经 `frontend/src/api/client.ts`
- 跨页状态用 Pinia；禁止硬编码后端 URL
- 异步页必须提供 loading、empty、error、success
- 写操作按钮按角色权限显示，且不得仅靠前端隐藏作为安全边界

## 禁止事项

- 不得引入第二套状态管理、HTTP Client、UI 框架（无 ADR）
- 不得使用 `any` / `@ts-ignore` 掩盖错误
- 不得提交 `dist/`、真实 `.env`、无关全库格式化
- 不得扩大 `allowModify`
