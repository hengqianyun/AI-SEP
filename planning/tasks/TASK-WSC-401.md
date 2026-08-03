# 任务包 TASK-WSC-401

```yaml
taskId: TASK-WSC-401
runId: RUN-WSC-006
status: VERIFIED
wave: W1
verifiedAt: 2026-08-03T10:38:00+08:00
evidenceId: EVID-TASK-WSC-401-1
evidence: planning/tasks/TESTRUN-TASK-WSC-401.md
goal: |
  将「引入目录」门户首页 /workspace /docs 以静态页方式迁入 WSC（迁移 mock 文案/数据即可）；
  /workspace「打开接入端」→ WSC /login。
  新增产品自动生成产品编码；高分屏去掉过窄 max-width；
  产品列表查询按修改时间倒序。
reqs: [REQ-SHELL-001, REQ-CAT-001, REQ-CAT-005, DEC-WSC-001, REQ-UX-004]
actorInstance: developer-wsc-401
sourceRef: C:\Users\water\Documents\xwechat_files\wxid_usi2o3v27zjt22_b5ca\msg\file\2026-07\frontend\frontend
```

## PO 补充（2026-08-03）

- `/`、`/workspace`、`/docs` 为**静态页面**；对应迁移源站 **mock 数据/文案**即可，勿接动态 CMS。
- 产品列表查询排序：**按修改时间倒序**（`updatedAt` desc；无则回退合理时间字段）。

## 背景约束（硬）

- 源仓为 **React + Tailwind**；目标仓为 **Vue 3 + 现有 token CSS**。**禁止**把 React 运行时打进 WSC。须 **移植 UI + 静态 mock**。
- 源：`HomePage.tsx`、`WorkspaceIntroPage.tsx`、`DocsPage.tsx`；mock：`mock/modules/site.ts`（抽静态 TS/JSON）。
- **不**移植源 `/workspace/console` 全工作台。

## 路由 / 鉴权

1. `/`、`/workspace`、`/docs`（及 docs 子路径若需要最小集）为 **public**，**不**包在 `WorkbenchLayout`。
2. 工作台路由保持 `/overview`、`/catalog`…，仍需登录 + WorkbenchLayout。
3. 根路径 **不再** redirect 到 `/overview`；未登录访问受保护路由 → `/login`。
4. `/workspace` 页「打开接入端」→ **`/login`**（可用 `router-link`；勿链到源 console）。
5. 登录成功默认仍进 `/overview`（可保留 redirect query）。

## 产品编码自动生成

- **仅 create 模式**（`/catalog/products/new`）：进入页或切换产品类型时自动生成符合 DEC-WSC-001 `{DOMAIN}-{FEATURE}-{NNNN}` 的编码并填入；字段只读或标明「自动生成」。
- DOMAIN/FEATURE 可按 `productType` 映射（如 API→`API`/`SVC`，DATASET→`DATA`/`SET` 等，在 DEV 中声明表）；NNNN 用时间戳/随机保证本机唯一尝试。
- 编辑模式不改已有编码。

## 高分屏空挡

- 去掉/放宽总览等主区 `max-width: 1200px` 一类限制，改为流体宽度 + 合理 padding（对齐目录页已做的视口利用）。
- 门户页宽屏也应铺满，避免右侧大片空白。

## 产品列表排序

- `GET /api/v1/catalog/products` 列表在分页前按 **修改时间倒序**。
- 当前 `CatalogProduct` 可能无 `updatedAt`：允许在 browse/editor 内存模型上增加 `updatedAt`（ISO-8601）；创建/编辑/导入成功写入时刷新；种子数据给合理初始值。
- **默认可不增查询参数**；契约仅在必要时最小补 `updatedAt` 字段（优先只改后端排序行为 + 响应带字段）。

## writeSet

- `frontend/src/features/portal/**`（静态页 + 从源 `mock/modules/site.ts` 抽出的静态数据）
- `frontend/src/router/**`；`frontend/src/layouts/**`（门户壳）
- `frontend/src/features/catalog/editor/**`（编码自动生成）
- `frontend/src/features/catalog/browse/**`（若列表展示/测排序）
- `frontend/src/features/overview/**`（高分屏宽度）
- `frontend/src/api/catalog.ts`（仅 Product 类型补 `updatedAt?` 若需要）
- `backend/.../catalog/browse/**`；`backend/.../catalog/editor/**`（及必要 import 写路径触摸 `updatedAt`）
- 对应测试；`planning/tasks/DEV-TASK-WSC-401.md`

## denyModify

- `contracts/**`（无强制契约变更则不动；若必须补字段再最小改 openapi 并在 DEV 声明）
- `frontend/src/components/**`
- `product/**`；`planning/approved/**`；`ai/runs/**/state.yaml`

## acceptance

- `/` `/workspace` `/docs` 为静态门户页（数据来自迁移 mock）
- 「打开接入端」→ `/login`
- 新增产品编码自动生成
- 宽屏无大片空挡
- 产品列表（含翻页/触底加载）按修改时间新→旧
- Vitest / 后端测覆盖排序或编码生成

## 派发

- 2026-08-03T09:35:00+08:00 PO 需求口述
- 2026-08-03T09:38:00+08:00 PO：静态页+mock；列表按修改时间倒序
