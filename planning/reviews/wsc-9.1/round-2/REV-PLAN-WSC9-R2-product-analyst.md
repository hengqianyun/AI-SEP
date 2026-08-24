# 规划评审 — PLAN-WSC-9.2 Round 2（productAnalyst 复评）

```yaml
reviewId: REV-PLAN-WSC9-R2-product-analyst
planId: PLAN-WSC-9.2
round: 2
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-009
actorInstance: product-analyst-wsc-012-r2
decision: APPROVE
summary: |
  R1 APPROVE 基础未被削弱：SNAP-WSC-009 全部 21 条增量 REQ（19×P0 + 2×P1 + SHELL-011 修订）
  仍有主任务映射与 E2E 覆盖；poLockedDefaults 全文保持；RBAC 三角色矩阵不变；继承不回退
  声明齐全；修订说明表 16 条 ISSUE 吸收摘要与正文 R2 revised 注释逐一对应，均为 API/UX/安全/
  测试精度增强而非范围扩大。无新增 SNAP 外 REQ、无 PO 锁定默认值改动、无继承 REQ 回退。
  本角色无 REQUEST_CHANGES 异议 → APPROVE。
```

## 复评范围与方法

- **权威**：`SNAP-WSC-009`（`status: APPROVED`，PO 2026-08-19）
- **对象**：`planning/proposals/PLAN-WSC-9.2.md`（Round 2 CANDIDATE / DRAFT，基于 PLAN-WSC-9.1 修订）
- **对照基线**：`REV-PLAN-WSC9-R1-product-analyst.md`（本角色 R1 APPROVE）+ `REV-PLAN-WSC9-R1-*.md` + `SUMMARY.md`（R1 全部 16 条 ISSUE）
- **复评焦点**：R1 APPROVE 基础是否被削弱；16 条 ISSUE 吸收后是否引入范围偏差；修订说明表与正文一致性；继承不回退完整性

## 1. R1 APPROVE 基础逐项确认

| R1 APPROVE 依据 | R2 状态 | 结论 |
|---|---|---|
| §7 REQ 映射：21 条增量 REQ 均有主任务 | §7 仍为 21 条（ORDER-001~017 + FE-001~003 + SHELL-011）；919 `requirements` 新增 `REQ-WSC-ORDER-017`（ISSUE-QA-001 修订吸收，**增强覆盖**） | **完整，未削弱** |
| P0 可观察验收 | 913 增 UUID v4 格式、IDOR 负例、mock 安全隔离；916 增 ADMIN 审计、chainCount 逐态、IDOR 负例；917 增布局/上传/取消冻结、ADMIN 标识 | **增强，未削弱** |
| In/Out Scope 与 SNAP 一致 | §1.1 未增减；新增冻结均为已有 REQ 范围内细化 | **未偏离** |
| poLockedDefaults | 文首「PO 已锁定」逐项与 SNAP `poLockedDefaults` 一致，**未改任何默认值** | **未削弱** |
| RBAC 三角色矩阵 | §3.1 矩阵字面表 + §4 可测矩阵均未改 V1.6 单元格；订单新增键角色权限与 R1 一致 | **未削弱** |
| 波次与 SNAP 对齐 | A/B/C 内容未变；D 波仍为 E2E 门禁编排 | **未偏离** |
| 继承 REQ 不回退 | §7 末行继承声明完整；各任务 `denyModify` 未弱化；§1.5 UX 不回退；§6.1 E2E #9 V1.6 回归 | **齐全** |
| OQ non-blocking | §2.2 OQ-V17-001/002、OQ-V16-* 状态与口径未变 | **正确** |

## 2. R1 本角色 ISSUE 状态确认

R1 productAnalyst 评审无开放 ISSUE（0 条）。复评无需关闭/确认任何自提 ISSUE。

## 3. R1 其他角色 ISSUE 吸收后范围偏差审查

R1 共 16 条 ISSUE（1×P0 + 11×P1 + 4×P2），修订说明表均标注吸收摘要。逐条审查是否引入范围偏差：

| ISSUE | 吸收内容 | 范围偏差？ | 说明 |
|---|---|---|---|
| ISSUE-API-001 (P0) | ChainAttestationPort 并列方案 B（`OrderChainAttestationPort`） | **无** | API 接口细化，不新增 REQ；§3.1 冻结字段枚举 |
| ISSUE-API-002 (P1) | multipart `file` + `transactionInfo` JSON schema 硬冻结 | **无** | 013/014 范围内 API 细节冻结 |
| ISSUE-API-003 (P1) | GET /orders 200 九组字段枚举 + `{ total, list, page, pageSize }` envelope | **无** | 007/008 范围内 API 响应冻结 |
| ISSUE-API-004 (P1) | POST /orders/{id}/confirm 纯动作端点，无 body | **无** | 010/011 范围内 API 细节 |
| ISSUE-UX-001 (P1) | 确认页布局冻结（三区纵向 + disabled + ADMIN 标识）+ testScope Vitest 断言 | **无** | 015 范围内 UX 细化 |
| ISSUE-UX-002 (P1) | 附件上传交互冻结（进度 + 即时校验 + 删除/替换）+ testScope | **无** | 013 范围内 UX 细化 |
| ISSUE-UX-003 (P1) | 取消对话框冻结（标题/正文/danger 样式/代操作身份）+ testScope | **无** | 012/FE-002 范围内 UX 细化 |
| ISSUE-SEC-001 (P1) | 订单号 UUID v4（不可预测、无业务语义） | **无** | 006 范围内安全加固 |
| ISSUE-SEC-002 (P1) | IDOR 负例（`enterpriseId`/`userId`/`demandUserId` 参数不扩权） | **无** | 001/011 范围内安全加固 |
| ISSUE-SEC-003 (P1) | ADMIN 代操作审计（`update_by`=ADMIN、时间线 operator 记录 ADMIN） | **无** | 011 范围内安全加固 |
| ISSUE-QA-001 (P1) | 919 `requirements` 增 REQ-017；§6.1 增场景 #10；918 testScope 增版本 UI 断言 | **无** | REQ-017 是 **SNAP 已有 P1 REQ**，从「未在 919 明确映射」到「显式纳入」，属覆盖增强而非范围扩大 |
| ISSUE-SEC-004 (P2) | mock 上链禁止读真实链配置；mock 安全默认值 | **无** | 016 范围内安全加固 |
| ISSUE-QA-002 (P2) | 916 testScope 增 chainCount 逐态一致断言 | **无** | 016 范围内测试精度 |
| ISSUE-QA-003 (P2) | 918 testScope「无 URL 提示」改正向断言 | **无** | 005 范围内测试措辞优化 |
| ISSUE-API-005 (P2) | GET /orders/notices/current 响应含 `{ content, version }` | **无** | 004 范围内 API 细节 |
| ISSUE-API-006 (P2) | contractsTarget 元数据说明（工作树 2.3.3 > SNAP 声明 2.3.2） | **无** | 元数据一致性，不涉及需求范围 |

**结论：全部 16 条 ISSUE 吸收均为 API/UX/安全/测试精度增强，未新增 REQ、未改 PO 锁定默认值、未扩大 In Scope、未缩小 Out Scope。**

## 4. 修订说明表与正文一致性

逐条核对修订说明表「修订章节/任务」列与正文对应位置 `<!-- R2 revised: ... -->` 注释：

- ISSUE-API-001 → §3.1 `ChainAttestationPort.md` 行：并列方案 `OrderChainAttestationPort` ✓
- ISSUE-API-002 → §3.1 `POST /orders/{id}/contract` 行：multipart 硬冻结 ✓
- ISSUE-API-003 → §3.1 `GET /orders 200 响应` 行：九组字段枚举 + envelope ✓
- ISSUE-API-004 → §3.1 `POST /orders/{id}/confirm` 行：纯动作端点无 body ✓
- ISSUE-UX-001 → 917 acceptance：布局冻结 + disabled + ADMIN 标识 ✓
- ISSUE-UX-002 → 917 acceptance：附件上传交互冻结 ✓
- ISSUE-UX-003 → 917 acceptance：取消对话框冻结 ✓
- ISSUE-SEC-001 → §1.2 假设表 + 913 acceptance + §3.2：UUID v4 ✓
- ISSUE-SEC-002 → 913/916 acceptance + §3.1 安全说明：IDOR 负例 ✓
- ISSUE-SEC-003 → 916 acceptance：ADMIN 代操作审计 ✓
- ISSUE-QA-001 → 919 `requirements` + §6.1 场景 #10 + 918 testScope：版本 UI 断言 ✓
- ISSUE-SEC-004 → §1.2 假设表 + 913/916 acceptance + §3.2：mock 隔离 ✓
- ISSUE-QA-002 → 916 testScope：chainCount 逐态 ✓
- ISSUE-QA-003 → 918 testScope：正向断言 ✓
- ISSUE-API-005 → §3.1 notices/current：`{ content, version }` ✓
- ISSUE-API-006 → §0.1：contractsTarget 说明 ✓

**修订说明表与正文改动完全一致。**

## 5. 继承 REQ 不回退确认

| 继承维度 | 状态 |
|---|---|
| §7 末行继承声明 | 「继承 CAT/SHELL/USER/RBAC/OVW/CHAIN/API/UX → 914/919 回归 + 全任务 deny 削弱」— 完整 |
| 任务 `denyModify` | 911~919 均禁止 `catalog/browse/**`、`styles/**`、`theme/**`、`**/sql/**`（非本任务路径）— 未弱化 |
| §0.2 预落地对账 | 明确要求 diff V1.6 实现并补齐 SNAP-009 缺口 — 保留 |
| §1.5 UX 不回退 | 令牌、壳层、RULE-GLOBAL-FRONTEND §1/§2/§3 不回退 — 完整 |
| §6.1 E2E #9 | V1.6 回归抽样（`test:p0-v16` 可跑；目录双入口抽样）— 完整 |
| 914 acceptance | V1.6 数据目录/我的目录/目录维护/我的数据产品/用户管理可见性不回退 — 完整 |

**继承不回退声明齐全，未被 R2 修订削弱。**

## 决策

`APPROVE` — PLAN-WSC-9.2 Round 2 修订稿保留了 R1 APPROVE 的全部基础：SNAP-WSC-009 全部 21 条增量 REQ 覆盖完整且部分增强（919 纳入 REQ-017）；P0 可观察验收因安全/UX/测试精度冻结而增强；poLockedDefaults 未改；RBAC 矩阵未变；继承不回退声明齐全。修订说明表 16 条 ISSUE 吸收均为已有 REQ 范围内精度提升，未新增 SNAP 外 REQ、未改 PO 锁定默认值、未引入范围偏差。本角色对本 planId 无 `REQUEST_CHANGES` 异议。
