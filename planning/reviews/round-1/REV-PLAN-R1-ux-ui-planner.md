# Round 1 Review — UX/UI Planner

```yaml
reviewId: REV-PLAN-R1-uxUiPlanner
planId: PLAN-WSC-1.0
round: 1
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-001
decision: REQUEST_CHANGES
summary: |
  候选计划将壳层导航、三角色写入口可见性、总览/目录三栏/详情/编辑/上链信息及未开放菜单提示映射到 TASK-WSC-002..006，
  主路径与 SNAP 成功标准 E2E 对齐；空态在趋势图、筛选无结果、上链无记录等处有部分覆盖。
  主要缺口：关键写路径与列表页缺少统一的加载/错误/成功交互状态约束；OQ-004「其他数据产品」类型切换的 UI 口径未写入 TASK-WSC-005 验收；
  快照要求的关键操作成功/失败反馈与敏感标识截断+复制未形成跨页面可执行 UX 验收项。
  体验层可实现，无业务目标冲突，但上述缺口关闭前不能 APPROVE。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 用户旅程 / 关键路径 | 通过 — 登录/角色 → 总览 → 目录 → 详情/上链/编辑 可由任务 acceptance 串起 |
| 关键异常路径状态 | 部分缺口 — 空态有覆盖；加载/失败/成功反馈不足，见 ISSUE |
| 组件与可访问性 | 弱 — Ant Design Vue 选型在硬门禁，但无 a11y/焦点/键盘约束 |
| API/UI 状态匹配 | 风险 — 写失败错误码与表单提示、类型专属字段区切换依赖契约冻结细节 |

## 主路径与关键异常覆盖核对

| 路径 | 计划覆盖 | 判定 |
|---|---|---|
| 壳层导航与未开放菜单（REQ-SHELL-001） | TASK-WSC-002 acceptance | 通过 |
| 角色写入口立即更新（REQ-RBAC-001 / SHELL） | TASK-WSC-002 | 通过（需配合加载态，见 ISSUE-UX-R1-001） |
| 总览指标/趋势/流/分布 | TASK-WSC-003；趋势空态与缩放可读 | 部分 — 缺加载/接口失败态 |
| 目录三栏、筛选空态、预览权限入口 | TASK-WSC-004 | 部分 — 缺列表加载/筛选错误态 |
| 详情/新增编辑/分类维护 | TASK-WSC-005 | 部分 — 校验与禁止删除有提示；缺统一成功/失败反馈与 OQ-004 UI |
| 上链列表与快照、敏感标识可读 | TASK-WSC-006 | 通过（空态 + 截断/复制已写） |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-UX-R1-001 | P1 | SNAP 非功能约束要求「关键操作有明确成功/失败反馈」；计划仅在个别负例（编码冲突、分类删除拒绝）提及提示，未对总览加载失败、目录筛选失败、产品提交成功/失败、分类保存成功/失败定义统一交互状态（至少：loading / empty / error / success）。TASK-WSC-001 路由清单亦不要求 UI 状态矩阵。 | 在计划附件或 TASK-WSC-001/相关 feature 任务 `acceptance` 中补充主路径 UI 状态矩阵（loading/empty/error/success），并映射到 OVW/CAT/CHAIN/SHELL 写操作与列表页；testScope 含至少一条失败反馈与一条成功反馈组件/E2E 断言。 | REQ-SHELL-001, REQ-OVW-001, REQ-OVW-002, REQ-CAT-002, REQ-CAT-005, REQ-CAT-006, REQ-CHAIN-001 |
| ISSUE-UX-R1-002 | P1 | 快照 OQ-004 与计划 §2.2：V1「其他数据产品」仅基础信息、无独立类型专属字段区；TASK-WSC-005 acceptance 仅写「类型切换专属字段区」，未排除该类型。实现易误做出第四套专属区或切换后残留校验，与产品裁决冲突。 | TASK-WSC-005（及 TASK-WSC-001 路由/表单契约说明若需要）显式写入：产品类型为「其他数据产品」时不展示、不校验类型专属字段区，仅基础信息分组；预览/详情同口径；testScope 含该类型用例。 | REQ-CAT-004, REQ-CAT-005 |
| ISSUE-UX-R1-003 | P2 | SNAP 要求敏感标识（哈希、DID、信用代码）完整可读且可截断+复制；仅 TASK-WSC-006 acceptance 写明，详情/预览中供应商信用代码等展示规则未写入 TASK-WSC-004/005。 | 在 CAT 详情/预览相关 acceptance 或共享 UI 约束中复用「截断展示 + 完整复制」规则，并指向 CHAIN 页同一组件约定。 | REQ-CAT-003, REQ-CAT-004, REQ-CHAIN-001 |

## 无异议项（记录）

- 未开放菜单「本版本未开放」与 OQ-001 Out of Scope 一致，可避免空页误导。
- 普通用户无编辑按钮（REQ-CAT-003）与 RBAC 写入口分工清晰，不依赖纯风格偏好。
- 分类维护「待保存」提示、有挂载产品禁止删除原因提示与 DEC-WSC-003 体验闭环可接受。
- 无 UI 范围冲突导致的 BLOCK；不升级 Product Owner。
