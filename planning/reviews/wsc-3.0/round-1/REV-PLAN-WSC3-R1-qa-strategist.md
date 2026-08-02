# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC3-R1-qaStrategist
planId: PLAN-WSC-3.0
round: 1
role: qaStrategist
actorInstance: qa-strategist-wsc-003-r1
snapshotIdAtReview: SNAP-WSC-003
decision: REQUEST_CHANGES
summary: |
  OQ-UX-003/004 关闭口径已写入计划；§3.1 截图文件名与走查路径、§3.2 E2E 命令/独立 tester/失败不得发布方向正确；
  六任务均有 testScope 字段。但分波视觉验收与 writeSet 冲突、页面任务 acceptance「接近原型」与 testScope 脱节，
  且 §3.2 场景 6「三角色写入口」在基线 E2E 中不可机械勾选且 206 未强制扩展。存在 P1 → REQUEST_CHANGES。
  未执行任何测试；不宣称实现或 E2E 已通过；不伪造 APPROVE。
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-WSC3-R1-001 | P1 | TASK-WSC-201..205 的 `acceptance` 含多项视觉「接近原型 / 层次对齐」判据（P0 REQ-UX-001..007），但 `testScope` 实质仅为 build/typecheck +「既有 Vitest」；除 201 可选「截图草稿」外，202–205 **无**截图路径、无差距清单行、无走查勾选。§5.1 要求独立 tester 执行 `testScope` 后才可 VERIFIED → 页面波次可在零 UX 证据下标 VERIFIED，P0 视觉缺陷推迟到 206 才发现，违背 UX 包「分波可验证」预期。 | 二选一写清并落到各任务字段：(A) 201–205 `testScope` 增加本任务主路径 **草稿截图**（映射 §3.1 文件名）+ 差距清单草稿行交付物；或 (B) 明文规定 201–205 的 VERIFIED **仅**覆盖逻辑/构建，视觉 acceptance 全部延后由 206 关门，且 §5.1/§8 与之对齐（页面任务 acceptance 不得再把「接近原型」写成该任务独立验收而不附证据路径）。 | REQ-UX-001, REQ-UX-002, REQ-UX-003, REQ-UX-004, REQ-UX-005, REQ-UX-006, REQ-UX-007, REQ-UX-011 |
| ISSUE-QA-WSC3-R1-002 | P1 | §3.1 写「差距清单由 TASK-WSC-206 维护；**开发波次可预填草稿行**」，但仅 206 `writeSet` 含 `ai/runs/RUN-WSC-003/ux-gap-checklist.md` 与 `ux-walkthrough/**`；201–205 无对应写权（且多数 `denyModify` 未开放该路径）。「可预填」在 scope-check 下 **不可执行**，与 ISSUE-001 的分波视觉证据方案冲突。 | 要么：(A) 为 201–205 增加可写草稿路径（例如各任务可写 `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-20x.md`，或 checklist 内任务专属段落并纳入 writeSet）；要么：(B) 删除「开发波次可预填」表述，明确仅 206 写清单，并与 ISSUE-001 选项 (B) 一致。 | REQ-UX-011 |
| ISSUE-QA-WSC3-R1-003 | P1 | §3.2 回归最低集场景 6 要求「详情/编辑/上链主路径；**三角色写入口可见性不回退**」。基线 `tests/e2e/specs/p0-wsc-v1.1.spec.ts` 含角色切换与 PROVIDER 新增、ADMIN 维护/导入，但 **无** ADMIN/PROVIDER/USER 写入口可见性矩阵断言（USER 负例、PROVIDER 不可达分类/目录维护等）。206 `testScope` 仅写「执行扩展后的 Playwright」与走查，未强制把该矩阵纳入可勾选扩展步骤。REQ-UX-011「三角色 V1.1 主路径功能冒烟/约定 E2E」在计划层不可机械关门。 | 在 §3.2 场景表或 206 `testScope` 明确扩展断言（或逐步可勾选冒烟）：至少覆盖 ADMIN/PROVIDER 约定写入口可见、USER 不可见分类维护/目录维护/产品写/导入（对齐 SNAP-WSC-002 `REQ-RBAC-001` / 既有 `useCanWrite` 矩阵）；并写明落在扩展后的 `p0-wsc-v1.1.spec.ts`（或继任规格文件）中。 | REQ-UX-011, REQ-RBAC-001 |
| ISSUE-QA-WSC3-R1-004 | P2 | REQ-UX-010 / 206 acceptance「常见桌面宽度」无具体视口列表；走查无法逐条勾选「破坏性窄屏」是否测过。 | 在 §3.1 或 206 `testScope` 列出 ≥2 个桌面视口（建议含 1280×800 与 1440×900 或 1920×1080）及检查项：侧栏遮挡主区、卡片重叠、弹窗溢出视口。 | REQ-UX-010 |
| ISSUE-QA-WSC3-R1-005 | P2 | (1) 201 `testScope`「shell 相关既有单测（RoleSwitcher / 写入口可见性）」：仓库无 `frontend/src/features/shell/**/*.spec.ts`；可跑的是 `frontend/src/features/auth/composables/useCanWrite.spec.ts`（不在 201 writeSet，可跑不可改）。表述不可调度。(2) §3.2 命令为泛化 `playwright test`（将跑 `specs/` 下含 `p0-wsc.spec.ts` 与 `p0-wsc-v1.1.spec.ts`）；`playwright.config.ts` 报告目录硬编码 `reports/p0-wsc-v1.1`，与建议证据目录 `p0-wsc-v1.2-ux/` 未强制对齐（虽允许复用 V11 目录「须在报告写明」，但缺「规格文件 + 报告落点」机械条款）。 | 201 `testScope` 写明实际命令/文件（至少 `useCanWrite`）；§3.2/206 钉死本轮门禁规格（如 `p0-wsc-v1.1.spec.ts` 或 `pnpm --dir tests/e2e test:v1.1`）及报告落点（更新 config 至 `p0-wsc-v1.2-ux/` **或** 复用 V11 目录时强制报告声明 evidenceId=`TESTRUN-WSC-E2E-V12-UX` + 时间戳子目录）。 | REQ-UX-002, REQ-UX-011 |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| OQ-UX-003 / OQ-UX-004 | SNAP-WSC-003 已 CLOSED；计划 §1.2 / §3 吸收：人工走查+截图清单为硬门禁；E2E 复用扩展、不必单开 UX E2E Run。口径可测。 |
| 视觉证据路径 | §3.1：`ux-gap-checklist.md`、`ux-walkthrough/screenshots/`、`WALKTHROUGH.md`、10 张主路径文件名建议齐全；对照源指向原型。路径本身清晰；可执行性受 ISSUE-001/002 拖累。 |
| 任务 testScope 有无 | 201–206 均有字段；逻辑层可指向既有 Vitest（overview/browse/admin/maintenance/import/detail/editor/chain、useCanWrite）。视觉层不足见 ISSUE-001。 |
| E2E 复用基线 | `tests/e2e/specs/p0-wsc-v1.1.spec.ts` + `E2E_RUN` skip 约定存在；§3.2 场景 1–5 与基线大体可映射；命令/独立 tester/失败不得发布已写。缺口见 ISSUE-003/005。 |
| 功能回归门禁 | §8 绑定差距清单 P0=0 + E2E 通过 + 契约/后端无变更；方向正确。三角色写入口与分波 UX 证据未闭合前不得 APPROVE。 |
| REQ-UX-009 空态/加载 | 分散 202–205 acceptance + 206 统一抽检；作为 P1 可接受，但依赖 206 走查清单可勾选行（建议在差距清单模板中单列，非本轮新开 ISSUE）。 |
| 令牌/样式契约单测 | 201 未强制 CSS 变量存在性断言；OQ-UX-003 以人工走查为门禁，不升格为 P1。 |
| 分类维护「弹窗」vs 现网路由 | 基线 E2E 走 `/catalog/admin/categories`；204/截图称弹窗。206 允许 DOM/选择器适配即可，不单列 ISSUE；建议计划一句声明「路由可保留，截图以最终可达 UI 为准」。 |

## 说明

- 输入：`planning/proposals/PLAN-WSC-3.0.md`；`product/requirements/SNAP-WSC-003.md`（含 OQ-UX-003/004 CLOSED）。
- 对照仓库只读核验：`tests/e2e/**`、`frontend` 既有 `*.spec.ts`（无 shell 单测）、`playwright.config.ts` 报告路径。
- **未**执行 Vitest/Playwright/走查；**未**代替 developer/tester 宣称通过。
- 决策：`REQUEST_CHANGES`（P1 未关闭）；非验收标准本身不可测所致 `BLOCK`。
