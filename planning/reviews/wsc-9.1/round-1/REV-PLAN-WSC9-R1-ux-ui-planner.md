# 规划评审 — UX/UI（PLAN-WSC-9.1 Round 1）

```yaml
reviewId: REV-PLAN-WSC9-R1-ux-ui-planner
planId: PLAN-WSC-9.1
round: 1
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-009
actorInstance: ux-ui-planner-wsc-012-r1
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-009 / PRD / RULE-GLOBAL-FRONTEND：订购流程路径清晰（产品详情→订购CTA→须知勾选→提交→时间线），
  列表分页 jumper+pageSize 档位正确，角色可见性矩阵与三角色回归正负例覆盖充分，toast/二次确认写入任务包，
  V1.6 目录/壳层/企业归属通过文件互斥与 deny 声明保障不回退。但 Wave B 三个 P0 交互细节未冻：
  ① 统一合约确认页信息区布局/勾选控件与按钮状态/代操作身份标识未在 917 acceptance 冻结；
  ② 附件上传中/失败/已上传三种状态的交互反馈未冻；
  ③ 取消二次确认对话框标题/正文（含不可回退警告）/按钮样式未冻。
  故 REQUEST_CHANGES。
```

## 评审基线

| 项 | 值 |
|---|---|
| 角色 | `uxUiPlanner`（`ai/agents/ux-ui-planner.md`） |
| 计划 | `planning/proposals/PLAN-WSC-9.1.md`（`CANDIDATE` / `DRAFT`，round 1） |
| 需求权威 | `product/requirements/SNAP-WSC-009.md`（`APPROVED`，PO 2026-08-19） |
| PRD | `product/prd/wsc-v1.7-transaction-orders.md` |
| UX 基线 | SNAP-WSC-003 / PLAN-WSC-3.1（令牌/壳层质感不回退） |
| 功能基线 | SNAP-WSC-008 / PLAN-WSC-8.3（目录/壳层/企业归属不削弱） |
| 前端全局规则 | `ai/rules/global/RULE-GLOBAL-FRONTEND.md` §1 toast / §2 二次确认 / §3 分页 |
| 格式参考 | `planning/reviews/wsc-8.1/round-1/REV-PLAN-WSC8-R1-ux-ui-planner.md` |

## 评审范围与方法

以 uxUiPlanner 角色，对照 SNAP-WSC-009 全部 21 条增量 REQ、PRD 状态机与交互描述、RULE-GLOBAL-FRONTEND 三条底线、TASK-WSC-911～919 acceptance/testScope，逐项检查 PLAN-WSC-9.1 在以下 10 个体验焦点上的覆盖完整性与可验收性。

## 覆盖结论

| # | 焦点 | 计划覆盖 | 结论 |
|---|---|---|---|
| 1 | 订购流程 UX（产品详情→须知→创建→时间线） | 915 acceptance 覆盖订购入口字段、须知校验、创建成功 toast、列表九组字段、详情只读分区+快照；§1.5 路由常量 `ROUTE_ORDER_SUBSCRIBE` | **通过** — 路径清晰：ProductDetail → 订购 CTA → Subscribe 页（产品信息+须知勾选+备注）→ 提交 → 详情（含时间线） |
| 2 | 列表交互（分页 jumper+pageSize） | §1.5 明确档位 10/20/50/100 缺省 10；915 acceptance/testScope 含 jumper、pageSize 变更回第 1 页、Vitest 断言 | **通过** — 与 RULE-GLOBAL-FRONTEND §3 和现有目录维护分页约定一致 |
| 3 | 订单详情（时间线/快照/合约/附件） | REQ-WSC-ORDER-009 覆盖全部分区；915 acceptance 冻结标题含订单号、可返回列表、产品区为快照；918 冻结版本展示 | **通过** — 分区明确；快照冻结是良好 UX 决策 |
| 4 | 统一合约确认页 UX | 917 acceptance 写「同一页展示交易信息、平台统一须知、签署附件；未勾选不能确认」 | **未冻** — 布局/勾选控件/代操作标识见 ISSUE-001 |
| 5 | 取消流程 UX | §1.5 + 917 acceptance 写「先弹确认，取消弹窗则无请求、状态不变；确认后 toast 且进入已取消」 | **未冻** — 对话框细节见 ISSUE-003 |
| 6 | 角色可见性 UX | §3.1 RBAC 矩阵逐 cell；§4.1 三角色回归正负例；914 覆盖菜单文案+useCanWrite+深链门控 | **通过** — USER「我的订单」/ PROVIDER+ADMIN「交易订单」；V1.6 目录菜单不回退 |
| 7 | RULE-GLOBAL-FRONTEND 合规 | §1.5 明确 toast（五动作）、二次确认（取消）、分页（jumper+pageSize）；915/917 testScope 含 Vitest 计数与断言 | **通过** — 前端底线三条均有对应 acceptance + testScope 锚点 |
| 8 | 外部跳转提示 UX | 918 acceptance 冻结：有地址打开、无地址提示未配置、不调用 POST /orders | **通过** — 非建单语义明确；列表不因此新增行 |
| 9 | 基线不回退 | §0.3 文件互斥（browse/** deny、WorkbenchLayout 914 独占、useCanWrite 914 独占）；§4.1 回归单元格；914 acceptance 明确 V1.6 目录/壳层可见性 | **通过** |
| 10 | 響應式/可訪問性 | 计划未提及移动端适配或键盘可访问性 | **观察项** — SNAP-009/PRD 同样无此需求，V1.6 基线无响应式要求，不阻塞本轮 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-UX-WSC9-R1-001 | P1 | SNAP `REQ-WSC-ORDER-015` / PRD「统一合约确认页」段：合并原标准合约弹窗与一事一议确认为同一页，展示交易信息、平台统一须知、签署附件，勾选确认后达成。PLAN TASK-WSC-917 acceptance 写「统一确认页同时可见交易信息、平台统一须知、签署附件（下载受角色限制）；未勾选不能确认」——**定义了「展示什么」但未冻「如何展示」**：① 信息区布局：交易信息区、须知区、附件区的纵向排列顺序与视觉分组（卡片/分割线/折叠面板）未冻；② 勾选控件与按钮状态：未勾选时确认按钮是 disabled（灰态）还是 clickable + toast 提示？PRD 写「未勾选不能确认」，应明确为 disabled 状态以减少无效点击；③ ADMIN 代 PROVIDER 确认时，页面是否标识操作人身份（如「当前操作：管理员代提供方确认」），避免误操作或审计盲区；④ 附件下载入口位置（附件区内 inline 下载按钮 vs 独立操作区）。这些是开发者实现时高频分歧点，若不冻可能导致 917 交付时与产品预期不一致。 | 在 TASK-WSC-917 `acceptance` 增补：**(A)** 确认页布局冻结（建议：交易信息区 → 平台统一须知区 → 签署附件区，纵向分区，每区有标题）；**(B)** 勾选控件紧邻确认按钮上方或左侧，未勾选时确认按钮 disabled；**(C)** ADMIN 代确认时页内可见操作人身份标识；**(D)** 附件下载按钮在附件区内 inline。Vitest 含未勾选 → button disabled 断言。 | REQ-WSC-ORDER-015, REQ-WSC-ORDER-FE-001 |
| ISSUE-UX-WSC9-R1-002 | P1 | SNAP `REQ-WSC-ORDER-013` / PRD「签署附件与交易信息」段：一份 Word/PDF ≤20MB，白名单+病毒扫描，无合格附件不能进待确认合约。TASK-WSC-917 acceptance 仅写「附件规则前端预校验与后端一致（类型/大小）」——**未冻附件上传全生命周期交互**：① 上传中：进度条、loading spinner、还是骨架屏？大文件（接近 20MB）时应有进度反馈；② 上传失败：格式不符（非 Word/PDF）→ 即时前端校验提示？超 20MB → 即时提示？病毒扫描失败 → 后端返回后 toast 或 inline 错误？三种失败场景的提示位置与文案未冻；③ 已上传成功：文件名展示、删除/替换按钮、文件大小展示。当前「无合格附件不能进待确认合约」是后端约束，但前端应在上传区即时反馈而非仅在提交时 4xx。 | 在 TASK-WSC-917 `acceptance` 增补：**(A)** 上传中显示进度（Ant Upload progress 或等价）；**(B)** 前端即时校验：非白名单格式/超 20MB 在选择文件后立即提示且不发起上传；**(C)** 后端扫描失败 inline 错误提示（不依赖提交时 4xx）；**(D)** 已上传文件显示文件名+大小，可删除/替换（替换前二次确认）。testScope 含文件类型/大小前端校验 Vitest。 | REQ-WSC-ORDER-013, REQ-WSC-ORDER-FE-002 |
| ISSUE-UX-WSC9-R1-003 | P1 | SNAP `REQ-WSC-ORDER-012` / PRD「取消订单旁路」段：三方均可在三个未完成状态取消，须二次确认，不可回退，无需对方同意。TASK-WSC-917 acceptance 写「先弹确认，取消弹窗则无请求、状态不变；确认后 toast 且进入已取消」——**仅冻了行为结果，未冻对话框内容**：① 标题：如「确认取消订单」或「取消订单？」；② 正文：是否包含「取消后不可回退」的不可逆警告？PRD 写「不可回退」，正文应体现此约束；③ 按钮：确认按钮文案「确认取消」且使用 Ant danger 样式（红色），与默认「确定」区分，降低误操作；④ ADMIN 代 PROVIDER 取消时，正文是否体现「您正在代提供方取消此订单」。取消是破坏性操作（不可回退），对话框内容直接决定用户理解后果的程度。 | 在 TASK-WSC-917 `acceptance` 增补：**(A)** 确认对话框标题「确认取消订单」；**(B)** 正文包含「取消后订单将进入已取消状态，此操作不可回退」；**(C)** 确认按钮文案「确认取消」，使用 danger 样式；**(D)** ADMIN 代取消时正文体现代操作身份。testScope 含对话框文本/按钮 class 断言。 | REQ-WSC-ORDER-012, REQ-WSC-ORDER-FE-002 |

## 非阻塞备註（不升 ISSUE）

- **订购流程路径**：ProductDetail → 订购 CTA → Subscribe 页（产品信息+须知勾选+备注）→ 提交 → 详情（含时间线），路径清晰且与 PRD 端到端流程一致，**通过**。
- **列表九组字段**：订单号/产品信息（名称+编码+类型+来源平台）/提供方/需求方（姓名+用户名+单位）/金额/状态/上链次数/下单时间/查看——字段分组合理，需求方三子字段在同一列展示可接受，**通过**。
- **状态时间线视觉布局**（观察项）：REQ-WSC-ORDER-009 要求「可展示状态进度」，但详情时间线的视觉形式（Ant Steps 步骤条 vs Ant Timeline 垂直列表 vs 自定义进度条）未在计划中指定。考虑到 Ant Design Vue 同时提供 Steps 和 Timeline 组件，且两者在功能上等价，开发者可自行选择——**不阻塞**，但建议 915 developer 选用与 V1.6 存证时间线一致的组件风格。
- **脱敏实现假设**（OQ-V17-001）：PLAN §2.2 假设 ADMIN 完整可读、PROVIDER 可见需求方姓名/单位但信用代码截断、USER 仅见本人明文——UX 合理，与「最小权限可见」原则一致。Wave C（918）再对齐，**观察项**。
- **产品 `allow_simple_order` 默认 1**：§1.2 标明实现假设——既有产品默认允许链内订购。从 UX 角度，用户进入产品详情看到「订购」CTA 是期望行为；若改为默认 0 需额外产品编辑器改动，当前假设可接受。
- **V1.6 数据登记/连接器仍占位未开放**：914 acceptance 写「数据登记、连接器管理仍未开放」——与 SNAP-009 不冲突（订单占位开放，非订单占位保留），**通过**。
- **外部跳转 UX**（P1，Wave C）：918 冻结「有地址打开、无地址提示未配置」，交互足够清晰。但「提示未配置」的 toast/alert 形式未指定——建议 918 developer 使用 toast 提示，与全局成功/失败提示风格一致。

## 决策

`REQUEST_CHANGES` — 关闭上表 ISSUE-001、002、003（均为 P1）前本角色不改为 `APPROVE`。三个 ISSUE 均指向 TASK-WSC-917 acceptance 的 UX 细节缺口——统一合约确认页、附件上传、取消对话框是 Wave B P0 交互核心，若不冻可能导致 917 交付时与产品/UX 预期分歧，增加返工成本。

本实例 **不** 代写计划补丁、**不** 伪造他角色 APPROVE、**不** 写入 `ai/runs/**`、**不** 修改 `state.yaml` / `events.jsonl`、**不** commit。
