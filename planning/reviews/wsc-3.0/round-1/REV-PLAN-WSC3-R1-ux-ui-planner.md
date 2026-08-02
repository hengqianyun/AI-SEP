# 规划评审

```yaml
reviewId: REV-PLAN-WSC3-R1-uxUiPlanner
planId: PLAN-WSC-3.0
round: 1
role: uxUiPlanner
actorInstance: ux-ui-planner-wsc-003-r1
snapshotIdAtReview: SNAP-WSC-003
decision: REQUEST_CHANGES
summary: |
  作为本轮 UX 计划关键可选角色：令牌权威（§2.1↔原型 :root）、壳层 201、目录浏览 203、
  导入弹窗（沿用目录页 #import-modal 载体）与详情/编辑/上链 205 拆分基本可验收；
  截图最低清单与视觉门禁口径（OQ-UX-003）与 SNAP 一致。
  仍有两处体验验收缺口阻碍本角色 APPROVE：
  ① REQ-UX-006「分类维护弹窗」未冻结载体/宿主/入口/关闭返回点，且与现行独立路由页及 204 denyModify browse 存在可执行冲突；
  ② REQ-UX-011 差距清单仅给路径与 P0=0，未规定可追踪行 schema 与主路径/截图映射，闭环不可对照。
  故 REQUEST_CHANGES；不代替其他角色批准。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-3.0.md` |
| 快照 | `product/requirements/SNAP-WSC-003.md` |
| 视觉权威抽样 | `design/prototypes/wsc-v1.1/index.html`（`:root`、侧栏壳层、`#category-modal`/`#import-modal`、总览/目录/详情/上链结构） |

## 对照结论（本轮焦点）

| 焦点 | 计划覆盖 | 结论 |
|---|---|---|
| 视觉权威 ↔ 任务 acceptance | §0/`visualAuthority`；§2.1 令牌最小集与原型 `:root` 一致（含 `--sidebar-bg`/`232px`/`--main-bg`/语义色/三级阴影）；201 acceptance 钉死 hex/宽/圆角阶梯；202–205 有层次锚点（指标卡色块、标签 active、弹窗宽约 960/520、上链双栏）；§3.1 截图 01–10 覆盖 SNAP 主路径 | **令牌与多数主路径可对照**；分类弹窗载体未钉死（见 ISSUE-UX-WSC3-R1-001） |
| 壳层 / 目录 / 弹窗 / 详情拆分 | 201 独占 styles+shell；203 browse；204 maintenance+admin+import；205 detail+editor+chain；导入入口视觉归 203、弹窗归 204 已声明；DAG/写集互斥清楚 | **壳层/浏览/导入/详情拆分可验收**；**分类维护**在原型为目录页 overlay 弹窗（`#category-modal` inline `width:960px`），现行实现为 `/catalog/admin/categories` 整页，计划未冻结迁移或「路由页内 modal 壳」方案，且 204 禁止写 `browse/**`，若要对齐原型挂载方式则写集冲突 |
| 偏差清单闭环（REQ-UX-011） | 路径 `ux-gap-checklist.md`；206 维护；acceptance「主路径项可追踪；P0=0+证据」；§3.1 P0 失败定义 | **路径与门禁意图够用，行级闭环不可验收**（见 ISSUE-UX-WSC3-R1-002） |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-UX-WSC3-R1-001 | P1 | SNAP REQ-UX-006 / 成功标准要求「分类维护弹窗」；原型 `index.html` `#category-modal` 为目录页 overlay + `.modal` 宽 960px（header/分栏/footer 主次按钮）。PLAN §3.1 截图 `06-category-admin-modal.png` 与 TASK-WSC-204 acceptance 均写「分类弹窗」，但未冻结：①载体（真·目录页弹窗 vs 保留独立路由并用 modal 壳呈现）；②宿主写集（若挂到 browse 需与 204 `denyModify: catalog/browse/**` 及现行 `ImportDialog` 宿主模式对齐）；③入口（目录触发 vs 侧栏/写入口）与关闭后返回表面。现行 `CategoryAdminPage.vue` 为整页路由，计划未说明是否在本 Run 改载体。验收易在「整页换皮」与「原型弹窗」间漂移，走查对照失败。 | 在 §1 或 TASK-WSC-204 `acceptance`（建议同步 `testScope`）显式冻结其一：**(A)** 对齐原型：分类维护为**目录页弹窗**（约 960px overlay），写明宿主（browse 挂载点或等价）、入口、关闭回目录浏览表面，并调整 203/204 writeSet/denyModify 使宿主可合法修改；或 **(B)** 保留独立路由，但须写明「路由页内等价 modal 壳（遮罩/宽幅/header/footer）」为可验收定义，并声明与原型 overlay 的已知偏差为非 P0（须进差距清单且走查可勾选）。无论 A/B，截图 06 与 204 acceptance 用语须与所选载体一致。 | REQ-UX-006, REQ-UX-004, REQ-UX-011, REQ-CAT-006 |
| ISSUE-UX-WSC3-R1-002 | P2 | REQ-UX-011 要求「对照原型主路径的差距清单（已关闭项可追踪）」；OQ-UX-003 以人工走查+截图为硬门禁。PLAN §3.1/206 仅规定文件路径、P0「完全不像原型」、截图文件名建议，**未**规定清单行最低字段（如：主路径页/截图#、对照维度、severity、status、关闭证据链接）及与 §3.1 截图 01–10（或等价主路径集）的覆盖关系。「可预填草稿」未强制 schema，206 无法证明闭环可审计。 | 在 §3.1 和/或 TASK-WSC-206 `acceptance` 增加差距清单**最低 schema**（建议列：`id`、对应截图或主路径键、维度〔令牌/IA/控件层级/空加载反馈/文案〕、severity〔P0/P1〕、status、evidence）；并要求截图最低清单每一项至少有一行可追踪记录（无差距则 `status=closed`/`n/a`+走查引用）。P0 行关闭须链到截图或 WALKTHROUGH 条目。 | REQ-UX-011, REQ-UX-001..010 |

## 非阻塞观察（不升 ISSUE）

- REQ-UX-009 落地分散在 202–205 + 206 抽检，与映射表一致；建议修订时在各页 acceptance 各补一条「空态/加载/提交反馈引用共享令牌或全局反馈样式」以降低 206 才发现不一致的成本——本轮不单独开单。
- 导入弹窗：原型 `#import-modal` 520px + 目录页宿主；与 PLAN-WSC-2.2 已冻结口径及现行 `CatalogBrowsePage`→`ImportDialog` 一致，203/204 拆分声明足够，**通过**。
- §2.1 令牌表与原型 `:root` 抽样一致；禁止 AntDV 默认主题冒充已写入 201/门禁，**通过**。
- REQ-UX-010「常见桌面宽度」未枚举具体断点；原型存在 1200/992 等媒体查询。可由 206 走查约定 1280/1440 抽样，不升 ISSUE。
- 本角色仅就 UX 可验收性给出 `REQUEST_CHANGES`，**不**代替 solutionArchitect / qaStrategist / planEditor 等角色结论。
