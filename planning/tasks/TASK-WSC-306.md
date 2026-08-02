# 任务包 TASK-WSC-306（HOTFIX / UX）

```yaml
taskId: TASK-WSC-306
runId: RUN-WSC-005
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
status: VERIFIED
wave: HOTFIX
verifiedAt: 2026-08-02T21:40:00+08:00
evidenceId: EVID-TASK-WSC-306-1
evidence: planning/tasks/TESTRUN-TASK-WSC-306.md
kind: HOTFIX
goal: |
  数据目录高分辨率可用；页头增加「新增产品」（productWriteVisible）；
  隐藏工作台底部写入口演示卡片；产品列表标题 sticky 吸附且无上方露缝/空位。
reqs: [REQ-CAT-001, REQ-CAT-005, REQ-RBAC-001, REQ-UX-004]
actorInstance: developer-wsc-306
```

## 验收

1. **高分辨率**：主内容区在宽屏/高屏下列表+预览充分利用视口（避免大片留白或列表区过矮）；不破坏既有触底加载高度约束。
2. **新增产品**：数据目录页头在 `productWriteVisible` 时展示「新增产品」按钮 → `/catalog/products/new`；USER 不可见；与「批量导入」并排（导入仍按 import 权限）。
3. **写入口卡片**：`WorkbenchLayout` 不再渲染 `WriteEntryDemo`（或等价永久隐藏）；侧栏/目录写路径不依赖该卡片。
4. **列表 title 吸附**：滚动列表时「产品列表」+「已加载 x/共 y」吸附在列表 pane 顶部；**不出现**标题上方露出行内容的空隙（修 sticky + pane padding 经典露缝）。

## writeSet

- `frontend/src/features/catalog/browse/**`
- `frontend/src/layouts/WorkbenchLayout.vue`
- `frontend/src/features/shell/components/WriteEntryDemo.vue`（仅若改为默认不展示；优先布局不挂载）
- 对应 `*.spec.ts`
- `planning/tasks/DEV-TASK-WSC-306.md`

## denyModify

- `backend/**`；`contracts/**`；`frontend/src/api/**`
- `frontend/src/components/**`
- `editor/**`；`detail/**`；`import/**`（除 browse 跳转）
- `product/**`；`planning/approved/**`；`ai/runs/**/state.yaml`（orchestrator 写）

## testScope

- Vitest：新增按钮权限可见性；布局/sticky 相关可测点
- 自测：宽屏目录页；ADMIN 见新增+导入；USER 皆无写按钮；滚动无露缝

## 派发

- dispatchedAt: 2026-08-02T21:32:00+08:00
- PO 口述 UI 需求
