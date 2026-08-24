# 规划评审 — PLAN-WSC-9.1 Round 1（productAnalyst）

```yaml
reviewId: REV-PLAN-WSC9-R1-product-analyst
planId: PLAN-WSC-9.1
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-009
actorInstance: product-analyst-wsc-012-r1
decision: APPROVE
summary: |
  相对 SNAP-WSC-009（status=APPROVED）：21 条增量 REQ（19×P0 + 2×P1）+ SHELL-011 修订
  均有主任务映射与 E2E 场景覆盖；全部 P0 具备可观察验收；In/Out Scope、
  poLockedDefaults（订单可追踪率、止于合约达成、平台内建单、外部回传不做、
  签署附件为主、统一合约确认页、白名单+扫毒、人民币两位小数、含税未税同价两字段、
  状态不可回退、三方取消三未完成态、ADMIN 可代 PROVIDER、PROVIDER 仅本企业、
  USER 仅本人需求方、ADMIN 列表全部、产品始终快照、数字合约变更新版本、上链 mock、
  按角色脱敏、列表仅分页、RULE-GLOBAL-FRONTEND）与 SNAP/PRD 一致；
  §4 RBAC 三角色矩阵与 SNAP 角色表对齐；波次 A/B/C 内容与 SNAP 交付波次等价
  （计划增加 D 波 E2E 为编排门禁，不削弱 REQ 覆盖）；继承 REQ 不回退声明与 §6.1
  回归场景齐全。本角色无开放 ISSUE → APPROVE。
```

## 评审范围与方法

- **权威**：`SNAP-WSC-009`（`status: APPROVED`，PO 2026-08-19）+ `product/prd/wsc-v1.7-transaction-orders.md`
- **对象**：`planning/proposals/PLAN-WSC-9.1.md`（CANDIDATE / DRAFT）；**不以**工作树预落地代码为批准依据
- **焦点**：全部 21 条 REQ 任务覆盖、P0 可观察验收、In/Out Scope、RBAC/菜单（ADMIN 可代 PROVIDER；PROVIDER 仅本企业；USER 仅本人需求方）、波次与 SNAP 对齐、PO 锁定默认值、继承 REQ 不回退、OQ non-blocking 正确性

## 覆盖结论（相对 SNAP-WSC-009）

| 检查项 | 结论 |
|---|---|
| 全部 21 REQ→任务 | §7 逐条映射完整：19×P0 + 2×P1 + SHELL-011 修订；每条 REQ 至少 1 个主任务 + 919 E2E |
| `REQ-WSC-ORDER-001` 角色菜单与数据范围 | **914**（菜单开放 + useCanWrite）+ **913**（列表 scope API）+ **919**（E2E #1/#3）；USER「我的订单」/PROVIDER+ADMIN「交易订单」文案区分 |
| `REQ-WSC-ORDER-002` 订购入口 | **915**：产品详情 CTA + 订购页展示订购人及单位 |
| `REQ-WSC-ORDER-003` 订购方式边界 | **913**（`allowSimpleOrder` 校验）+ **915**（按钮禁用/错误提示）；外部跳转不建单归 **918** |
| `REQ-WSC-ORDER-004` 订购须知确认 | **913**（后端 `noticeAccepted=true` 校验）+ **915**（前端勾选） |
| `REQ-WSC-ORDER-005`（P1）外部跳转 | **918**（Wave C）；有地址打开、无地址提示、不建单；不阻塞主流程 — 与 SNAP 优先级一致 |
| `REQ-WSC-ORDER-006` 链内创建与快照 | **913**（创建 API + 快照 + 时间线 + mock）+ **915**（UI + toast）+ **919**（E2E #2） |
| `REQ-WSC-ORDER-007` 列表查询与分页 | **913**（keyword + status + 分页 API）+ **915**（jumper + pageSize UI）+ **919**（E2E #4） |
| `REQ-WSC-ORDER-008` 列表字段与脱敏 | **915**（九组字段 UI）+ **918**（C 波脱敏对齐） |
| `REQ-WSC-ORDER-009` 详情分区 | **915**（只读详情 + 快照）+ **918**（版本区 + 脱敏） |
| `REQ-WSC-ORDER-010` 主状态流转 | **916**（后端四步状态机）+ **917**（UI 流程）+ **919**（E2E #5）；止于合约达成 |
| `REQ-WSC-ORDER-011` 状态动作权限 | **916**（RbacMatrix + WriteAuthorizationInterceptor）+ **917**（按钮可见性）+ **919**（E2E #8）；ADMIN 可代、PROVIDER 本企业、USER 上传 |
| `REQ-WSC-ORDER-012` 取消旁路 | **916**（三未完成态取消 + 时间线）+ **917**（二次确认）+ **919**（E2E #6） |
| `REQ-WSC-ORDER-013` 签署附件 | **916**（白名单 + 扫描端口 + 20MB + 单文件）+ **917**（上传表单 + 下载） |
| `REQ-WSC-ORDER-014` 计费与金额 | **916**（CNY 两位 + 双字段同价 + 单价>0）+ **917**（交易信息表单） |
| `REQ-WSC-ORDER-015` 统一合约确认页 | **916**（后端统一确认）+ **917**（前端统一页 + 未勾选阻断）+ **919**（E2E #5） |
| `REQ-WSC-ORDER-016` 时间线与 mock 上链 | **913**（创建态 mock）+ **916**（每次状态变更 mock）+ **919**（E2E #7 `chainCount` 一致） |
| `REQ-WSC-ORDER-017`（P1）数字合约版本 | **918**（Wave C）；初版 + 变更新版本 + 历史可查 + 空值不阻断 |
| `REQ-WSC-ORDER-FE-001` 写成功 toast | **915**（创建 toast）+ **917**（确认/提交/取消 toast）+ **919**（E2E #5/#6） |
| `REQ-WSC-ORDER-FE-002` 二次确认 | **917**（取消二次确认 + 未确认不发请求）+ **919**（E2E #6） |
| `REQ-WSC-ORDER-FE-003` 分页 jumper+pageSize | **915**（Ant Pagination jumper + pageSize 档位 10/20/50/100）+ **919**（E2E #4） |
| `REQ-SHELL-011` 导航开放 | **914**（从 navClosed 移除占位 + 角色化菜单）+ **919**（E2E #1）；取代 SHELL-001 订单占位 |
| In Scope | §1.1 四波任务表与 SNAP In Scope 逐项对应；链内简易流程、列表分页、详情快照、签署附件、统一确认页、toast、二次确认均有覆盖 |
| Out Scope | §1.1「Out of Scope / 默认 deny」10 项与 SNAP Out of Scope 一致：外部回传建单、拒绝/退回/超时、通知、真实上链、支付/退款/发票/交付、列表导出/批量/日期筛选、司法效力、企业内共享、企业管理页、削弱 V1.6 |
| RBAC/菜单 | §4 / §4.1 三角色矩阵与 SNAP「角色与权限摘要」表一致：ADMIN 全部订单 + 可代 PROVIDER（确认订单/确认合约/取消）；PROVIDER 仅本企业 + 不可代他企；USER 仅本人需求方 + 仅上传；隐藏≠授权 + 深链负例在 913/914/915/919 |
| PO 锁定默认值 | §1.2 / 文首覆盖全部 22 项 `poLockedDefaults`：`successMetric=order-traceability`、`scopeEndsAt=contract-reached`、`inChainCreateOnly=true`、`noExternalPushCreate=true`、`unifiedNotice=platform-wide`、`contractBody=signed-attachment-primary`、`unifiedContractConfirmPage=true`、`attachment=word-pdf-20mb-single-unlimited-retention`、`attachmentSecurity=whitelist-plus-virus-scan`、`downloadByRole=true`、`currency=CNY-2dp`、`taxInclusiveEqualsExclusive=true`、`keepBothTaxFields=true`、`noRejectReturnTimeoutStates=true`、`chainAttestation=mock`、`privacy=mask-by-role`、`list=pagination-only`、`pagination=jumper-and-pageSize`、`pageSizeDefaultTiers=[10,20,50,100]`、`stateIrreversible=true`、`cancel=demand-provider-admin-on-three-open-states`、`adminCanActAsProvider=true`、`providerScope=current-enterprise-as-provider`、`userScope=own-as-demand`、`adminListScope=all`、`productSnapshot=at-order-time`、`digitalContract=master-data-plus-new-version-on-change`、`noNotifySla=true`、`frontendRule=RULE-GLOBAL-FRONTEND` |
| 波次 vs SNAP | SNAP `poLockedDefaults.waves: [A, B, C]`；计划 A/B/C 内容与 SNAP 交付波次等价（A=菜单+创建+列表+详情只读+toast；B=主流转+权限+附件+计费+确认+取消+时间线；C=数字合约+外部跳转+脱敏）。计划增加 **D 波**（919 E2E）为发布门禁编排，不额外覆盖功能 REQ；SNAP §6.1 E2E 场景表完整纳入 §6.1 — 未遗漏 SNAP 波次内 REQ |
| 继承 REQ 不回退 | §7 末行明确「继承 CAT/SHELL/USER/RBAC/OVW/CHAIN/API/UX → 914/919 回归 + 全任务 deny 削弱」；各任务 `denyModify` 禁止改 `catalog/browse/**`、`styles/**`、`theme/**`；§0.2 预落地对账要求 diff V1.6 实现；§1.5 UX 不回退令牌/壳层；§6.1 E2E #9 V1.6 回归 |
| OQ non-blocking | §2.2：OQ-V17-001（脱敏）、OQ-V17-002（扫描）标 OPEN non-blocking 并给实现假设；OQ-V16-001..003 继承 non-blocking；OQ-V17-PO-001..020 CLOSED 按 SNAP — 正确 |
| 状态机一致 | §1.2 假设 + §3.1 契约：`PENDING_CONFIRM → PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED` + `CANCELLED` 旁路终态；不可回退；无拒绝/退回/超时 — 与 SNAP 状态机表及 PRD 一致 |
| 状态枚举名称 | 契约 `PENDING_CONFIRM`/`PENDING_UPLOAD`/`PENDING_CONTRACT_CONFIRM`/`CONTRACT_REACHED`/`CANCELLED` 与 SNAP 页面名「待确认订单/待上传合约/待确认合约/合约已达成/已取消」语义对齐 |

## 异议

（本角色无开放 ISSUE。）

## 非阻塞备注（不构成独立 ISSUE）

- 计划 `waveCount: 4`（A/B/C/D）与 SNAP `poLockedDefaults.waves: [A, B, C]` 为**编排粒度**差异（D 波 = E2E 门禁），非范围差异；计划 §6 表头已注明 D 波内容为 E2E，SNAP §6.1 E2E 场景表完整纳入。委员会若需字面对齐，可在计划 §1.1 或文首增加「SNAP 波次 ↔ 计划波次」对照说明，**不阻碍**本角色 APPROVE。
- 计划 §7 注释行 `21 条增量 REQ（20×P0 级主路径含 FE + SHELL-011；2×P1）` 中 P0 实际为 **19 条**（ORDER-001~016 共 16 + FE-001~003 共 3 = 19 P0；ORDER-005、017 = 2 P1；SHELL-011 = 修订）。建议在后续 round 修正为「19×P0 + 2×P1 + 1 修订（SHELL-011）」，**不影响**覆盖完整性。
- `OQ-V17-002`（扫描实现）§2.2 给出 `OrderAttachmentScanPort` 同步默认实现假设，明确「未调用端口不得入库」可观察；选型不在本计划裁定——与 SNAP 一致。建议 916 tester 在集成测试中显式验证「未扫描 → 不能进入待确认合约」的负面路径。
- `contractsTarget` 文首声明 `wsc-contracts@2.3.2`，§0.1 注明工作树当前 `2.3.3`，911 在 2.3.3 上合并增量且禁止无故再 bump——文档自洽。建议 911 acceptance 中显式说明「VERSION 保持 2.3.3 或对齐 bump 原因」以便审计追溯。
- git status 显示 `useCanWrite.ts` 已有未提交修改，§0.2 已将此列为预落地迹象并要求 914 对账；实现时须先确认当前 diff 是否为 V1.6 合法变更再叠加订单导出，避免混入非计划范围改动。

## 决策

`APPROVE` — 相对 SNAP-WSC-009，PLAN-WSC-9.1 Round 1 产品范围、全部 21 条 REQ 覆盖、P0 可观察验收、RBAC/菜单、PO 锁定默认值、In/Out Scope、波次、继承不回退与 OQ 处理对齐充分；本角色对本 planId 无 `REQUEST_CHANGES` 异议。
