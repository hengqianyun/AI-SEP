# 规划评审 — UX/UI（PLAN-WSC-9.2 Round 2 复评）

```yaml
reviewId: REV-PLAN-WSC9-R2-ux-ui-planner
planId: PLAN-WSC-9.2
round: 2
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-009
actorInstance: ux-ui-planner-wsc-012-r2
decision: APPROVE
summary: |
  R1 三条 P1 ISSUE 全部被 PLAN-WSC-9.2 正确吸收至 TASK-WSC-917 acceptance/testScope：
  ISSUE-UX-001（统一合约确认页）— 布局冻结为纵向三区（交易信息→须知→附件），
  未勾选 disabled、ADMIN 代操作标识、Vitest 断言齐全；
  ISSUE-UX-002（附件上传交互）— 上传中进度、前端即时校验、扫描失败 inline、已上传操作均已冻结；
  ISSUE-UX-003（取消对话框）— 标题/正文（含不可回退警告）/danger 按钮/代操作身份均已冻结。
  修订未引入新 UX 问题。故 APPROVE。
```

## 评审基线

| 项 | 值 |
|---|---|
| 角色 | `uxUiPlanner`（`ai/agents/ux-ui-planner.md`） |
| 计划 | `planning/proposals/PLAN-WSC-9.2.md`（`CANDIDATE` / `DRAFT`，round 2） |
| R1 评审 | `planning/reviews/wsc-9.1/round-1/REV-PLAN-WSC9-R1-ux-ui-planner.md` |
| 需求权威 | `product/requirements/SNAP-WSC-009.md`（`APPROVED`，PO 2026-08-19） |
| 前端全局规则 | `ai/rules/global/RULE-GLOBAL-FRONTEND.md` §1 toast / §2 二次确认 / §3 分页 |

## 复评方法

逐条对照 R1 的三条 P1 ISSUE 的 `closeWhen` 条件，检查 PLAN-WSC-9.2 中 TASK-WSC-917 的 `acceptance` 与 `testScope` 是否完整覆盖。同时扫描修订是否引入新 UX 问题。

---

## ISSUE 逐条复评

### ISSUE-UX-001（P1）：统一合约确认页 — **已关闭**

| closeWhen 条件 | PLAN-WSC-9.2 吸收情况 | 结论 |
|---|---|---|
| **(A)** 布局冻结：交易信息区→平台统一须知区→签署附件区，纵向分区，每区有标题 | 917 acceptance:「纵向分区排列 — **交易信息区**（标题+订单金额/明细）→ **平台统一须知区**（标题+须知正文）→ **签署附件区**（标题+下载按钮 inline），每区有标题分隔」 | ✅ 完全覆盖 |
| **(B)** 勾选控件紧邻确认按钮上方或左侧，未勾选时确认按钮 disabled | 917 acceptance:「勾选控件紧邻确认按钮上方，**未勾选时确认按钮 disabled（灰态）**」 | ✅ 完全覆盖 |
| **(C)** ADMIN 代确认时页内可见操作人身份标识 | 917 acceptance:「ADMIN 代 PROVIDER 确认时，页内可见操作人身份标识（如「当前操作：管理员代提供方确认」）」 | ✅ 完全覆盖 |
| **(D)** 附件下载按钮在附件区内 inline | 917 acceptance:「签署附件区（标题+下载按钮 inline）」 | ✅ 完全覆盖 |
| Vitest 含未勾选→button disabled 断言 | 917 testScope:「Vitest：统一确认未勾选不发请求（**button disabled 断言**）」 | ✅ 完全覆盖 |

**结论**：ISSUE-UX-001 四项子条件 + Vitest 断言全部被 917 acceptance/testScope 吸收，**关闭**。

---

### ISSUE-UX-002（P1）：附件上传交互 — **已关闭**

| closeWhen 条件 | PLAN-WSC-9.2 吸收情况 | 结论 |
|---|---|---|
| **(A)** 上传中显示进度（Ant Upload progress 或等价） | 917 acceptance:「上传中显示进度（Ant Upload progress 或等价）」 | ✅ 完全覆盖 |
| **(B)** 前端即时校验：非白名单格式/超 20MB 在选择文件后立即提示且不发起上传 | 917 acceptance:「前端即时校验：非白名单格式 / 超 20MB 在选择文件后立即提示且不发起上传」 | ✅ 完全覆盖 |
| **(C)** 后端扫描失败 inline 错误提示（不依赖提交时 4xx） | 917 acceptance:「后端扫描失败 inline 错误提示（不依赖提交时 4xx）」 | ✅ 完全覆盖 |
| **(D)** 已上传文件显示文件名+大小，可删除/替换（替换前二次确认） | 917 acceptance:「已上传文件显示文件名+大小，可删除/替换（替换前二次确认）」 | ✅ 完全覆盖 |
| testScope 含文件类型/大小前端校验 Vitest | 917 testScope:「Vitest：**附件上传 — 非白名单格式/超 20MB 前端即时校验**」 | ✅ 完全覆盖 |

**结论**：ISSUE-UX-002 四项子条件 + Vitest 断言全部被 917 acceptance/testScope 吸收，**关闭**。

---

### ISSUE-UX-003（P1）：取消对话框 — **已关闭**

| closeWhen 条件 | PLAN-WSC-9.2 吸收情况 | 结论 |
|---|---|---|
| **(A)** 确认对话框标题「确认取消订单」 | 917 acceptance:「标题「确认取消订单」」 | ✅ 完全覆盖 |
| **(B)** 正文包含「取消后订单将进入已取消状态，此操作不可回退」 | 917 acceptance:「正文包含「取消后订单将进入已取消状态，此操作不可回退」」 | ✅ 完全覆盖 |
| **(C)** 确认按钮文案「确认取消」，使用 danger 样式 | 917 acceptance:「确认按钮文案「确认取消」，使用 Ant danger 样式（红色）」 | ✅ 完全覆盖 |
| **(D)** ADMIN 代取消时正文体现代操作身份 | 917 acceptance:「ADMIN 代取消时正文体现代操作身份（如「您正在代提供方取消此订单」）」 | ✅ 完全覆盖 |
| testScope 含对话框文本/按钮 class 断言 | 917 testScope:「Vitest：**取消对话框标题/正文/按钮 class（danger）断言**」 | ✅ 完全覆盖 |

**结论**：ISSUE-UX-003 四项子条件 + Vitest 断言全部被 917 acceptance/testScope 吸收，**关闭**。

---

## 修订是否引入新 UX 问题

对 PLAN-WSC-9.2 全文（§0–§10）及 TASK-WSC-911～919 逐任务扫描，未发现修订引入新 UX 问题：

- **TASK-WSC-914**（菜单导航）：无新增 acceptance，保持 R1 通过状态
- **TASK-WSC-915**（订购/列表/详情）：无新增 acceptance，保持 R1 通过状态
- **TASK-WSC-917**（确认/上传/取消）：R2 修订仅在原有框架上增补了三个 ISSUE 的冻结细节，未改变交互语义
- **TASK-WSC-918**（版本/跳转/脱敏）：R2 增补了版本区 UI 断言（ISSUE-QA-001），无 UX 矛盾
- **TASK-WSC-919**（E2E）：新增建议场景 #10「数字合约版本」，与 918 版本区 UI 断言一致，无冲突
- **§1.5 UX 不回退**：保持不变，V1.6 壳层/目录/企业归属通过文件互斥与 deny 声明保障
- **RULE-GLOBAL-FRONTEND**：toast（五动作）、二次确认（取消）、分页（jumper+pageSize）三条底线均有对应 acceptance + testScope 锚点，无回退

**结论**：修订未引入新 UX 问题。

---

## 非阻塞备注（沿用 R1）

- **状态时间线视觉布局**（观察项）：R1 已提出，915 developer 选用与 V1.6 存证时间线一致的组件风格即可，不阻塞。
- **脱敏实现假设**（OQ-V17-001）：R1 已确认 UX 合理，Wave C（918）对齐。
- **外部跳转提示形式**（观察项）：R1 建议使用 toast，与全局风格一致，不阻塞。

---

## 决策

`APPROVE` — R1 三条 P1 ISSUE（ISSUE-UX-001/002/003）全部被 PLAN-WSC-9.2 TASK-WSC-917 的 acceptance 与 testScope 正确吸收，closeWhen 条件逐项满足，Vitest 断言覆盖完整。修订未引入新 UX 问题。

本实例 **不** 代写计划补丁、**不** 伪造他角色 APPROVE、**不** 写入 `ai/runs/**`、**不** 修改 `state.yaml` / `events.jsonl`、**不** commit。
