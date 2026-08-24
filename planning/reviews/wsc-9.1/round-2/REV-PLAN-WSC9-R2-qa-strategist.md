# Round 2 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC9-R2-qa-strategist
planId: PLAN-WSC-9.2
round: 2
role: qaStrategist
actorInstance: qa-strategist-wsc-012-r2
snapshotIdAtReview: SNAP-WSC-009
decision: APPROVE
summary: |
  R1 三条 ISSUE 全部被正确吸收并关闭：
  ISSUE-QA-001（P1）— TASK-WSC-919 requirements 已增 REQ-WSC-ORDER-017；§6.1 已增场景 #10「数字合约版本」（建议级，与 P1 优先级匹配）；TASK-WSC-918 testScope 已增 Vitest 版本区 UI 断言（命名用例 918-contract-version-ui）。三层覆盖齐备，关闭。
  ISSUE-QA-002（P2）— TASK-WSC-916 testScope 已增「每步状态变更后 chainCount = 时间线记录条数；列表 chainCount 与详情一致」显式可勾选项，命名用例 916-chain-count-per-state。关闭。
  ISSUE-QA-003（P2）— TASK-WSC-918 testScope「无 URL 提示」已改为正向断言「无地址时展示"未配置"或等价提示文案（testid 可断言）」。关闭。
  其他角色 ISSUE 吸收后未引入新的测试风险；UX 冻结（ISSUE-UX-001/002/003）均在 917 testScope 增补了 Vitest 断言；安全加固（ISSUE-SEC-001/002/003/004）均在 913/916 testScope 增补了命名用例。
  无新 ISSUE。未执行任何测试；不宣称实现或 E2E 已通过。
```

## 评审范围与方法

| 项目 | 说明 |
|---|---|
| 输入 | `planning/proposals/PLAN-WSC-9.2.md`（Round 2 CANDIDATE / DRAFT）；`planning/reviews/wsc-9.1/round-1/REV-PLAN-WSC9-R1-qa-strategist.md`（R1 本角色评审）；`planning/reviews/wsc-9.1/round-1/SUMMARY.md`；`product/requirements/SNAP-WSC-009.md` |
| 方法 | 逐条对照 R1 ISSUE 的 `closeWhen` 条件，在 PLAN-WSC-9.2 正文中逐字面确认吸收；检查其他角色 ISSUE 吸收是否引入新测试风险 |
| 角色范围 | 只读；**未**修改计划、源码或 `ai/runs/**/state.yaml` / `events.jsonl` |

## ISSUE 复评（逐条确认）

### ISSUE-QA-001（P1）— REQ-WSC-ORDER-017 数字合约版本历史 UI 可见性

| closeWhen 条件 | PLAN-WSC-9.2 字面证据 | 结论 |
|---|---|---|
| TASK-WSC-919 `requirements` 增 REQ-WSC-ORDER-017 | §TASK-WSC-919 第 628 行：`requirements` 含 `REQ-WSC-ORDER-017` | ✅ 满足 |
| §6.1 增场景 #10「数字合约版本」 | §6.1 第 725 行：场景 #10「创建后版本 1 可见；主路径达成后版本 ≥2 且历史可查」，标记为**建议** | ✅ 满足（建议级与 P1 优先级匹配，不强制阻塞发布门禁） |
| TASK-WSC-918 testScope 增 Vitest UI 断言 | §TASK-WSC-918 第 619-621 行：「详情页版本区可见版本号、版本列表条数随状态变更递增、历史版本可展开查看」，命名用例 `918-contract-version-ui` | ✅ 满足 |

**状态：已关闭。** 三层（E2E 建议场景 + 919 requirements 映射 + 918 Vitest UI 断言）均已补齐。版本历史 UI 可见性不再仅有后端计数覆盖。

---

### ISSUE-QA-002（P2）— mock 上链 chainCount 逐态核对

| closeWhen 条件 | PLAN-WSC-9.2 字面证据 | 结论 |
|---|---|---|
| TASK-WSC-916 testScope 增「每步状态变更后 chainCount = 时间线记录条数；列表 chainCount 与详情一致」 | §TASK-WSC-916 第 542 行：`集成：**每步状态变更后 GET /orders/{id} 的 chainCount = 时间线记录条数；列表 chainCount 与详情一致**`；第 546 行命名用例含 `916-chain-count-per-state` | ✅ 满足 |

**状态：已关闭。** chainCount 一致性已从 acceptance 隐含描述提升为 testScope 显式可勾选项，tester 必须逐态验证。

---

### ISSUE-QA-003（P2）— 外部跳转 testScope 措辞含糊

| closeWhen 条件 | PLAN-WSC-9.2 字面证据 | 结论 |
|---|---|---|
| TASK-WSC-918 testScope「无 URL 提示」改为正向断言 | §TASK-WSC-918 第 617 行：`Vitest：外部跳转无 POST；**无地址时展示「未配置」或等价提示文案（testid 可断言）**` | ✅ 满足 |

**状态：已关闭。** 措辞从含糊的「无 URL 提示」改为明确的正向断言，含 testid 可断言指引，tester 不再有歧义。

---

## 其他角色 ISSUE 吸收后的测试风险评估

| 来源 ISSUE | 吸收位置 | 测试影响 | 新风险？ |
|---|---|---|---|
| ISSUE-API-001（ChainAttestationPort 并列方案） | §3.1 / 913 / 916 | 913 testScope 含 mock 上链集成用例；新端口 `OrderChainAttestationPort` 与现有端口共存，mock 适配器可独立测试 | 无 |
| ISSUE-API-002（multipart 硬冻结） | §3.1 / 916 | 917 testScope 含「附件上传 — 非白名单格式/超 20MB 前端即时校验」Vitest | 无（改善） |
| ISSUE-API-003（列表字段枚举） | §3.1 / 913 | 913 acceptance 九组字段枚举 + testScope「列表字段 testid 齐全」 | 无（改善） |
| ISSUE-API-004（confirm 无 body） | §3.1 / 916 | 916 acceptance 明确空 JSON 对象；无额外测试风险 | 无 |
| ISSUE-UX-001（确认页布局冻结） | 917 | 917 testScope 含「统一确认未勾选不发请求（button disabled 断言）」+「ADMIN 代确认身份标识可见」 | 无（改善） |
| ISSUE-UX-002（附件上传交互） | 917 | 917 testScope 含文件校验 Vitest | 无（改善） |
| ISSUE-UX-003（取消对话框） | 917 | 917 testScope 含「取消对话框标题/正文/按钮 class（danger）断言」 | 无（改善） |
| ISSUE-SEC-001（UUID v4 订单号） | §1.2 / 913 | 913 testScope 含「订单号格式验证（UUID v4 正则，不含 productId/enterpriseId 子串）」 | 无（改善） |
| ISSUE-SEC-002（IDOR 负例） | 913 / 916 | 913 testScope 含「userId/demandUserId 参数负例」；916 含「确认/取消篡改 enterprise/user 参数 → 负例」 | 无（改善） |
| ISSUE-SEC-003（ADMIN 代操作审计） | 916 | 916 acceptance/testScope 含 ADMIN `update_by` + 时间线 operator 审计断言 | 无（改善） |
| ISSUE-SEC-004（mock 上链隔离） | §1.2 / 913 | §1.2 硬性禁止条款 + 913 testScope「mock 上链记录不含真实链节点信息」 | 无（改善） |
| ISSUE-API-005（须知含版本） | §3.1 | 911 契约层面冻结，无额外测试需求 | 无 |
| ISSUE-API-006（contractsTarget 元数据） | YAML 头 | 元数据澄清，无测试影响 | 无 |

**结论：其他角色 ISSUE 吸收均为测试覆盖改善（从无到有或从模糊到精确），未引入新测试风险。**

---

## 领域核对（R2 更新确认）

| 检查项 | R1 结论 | R2 状态 |
|---|---|---|
| 任务 testScope 有无 | 911–919 均有 | R2 维持；916/917/918 testScope 均有增补 |
| 21 条 REQ 覆盖 | 017 E2E 缺失 → ISSUE-001 | **已修复**：919 requirements 含 017；§6.1 场景 #10 |
| E2E §6.1 场景 | 九场景强制 | R2 增场景 #10（建议）；九强制场景不变 |
| RBAC 矩阵可测性 | 正确 | 不变 |
| RULE-GLOBAL-FRONTEND | 三条可 Vitest | 不变；917 新增 disabled/upload/dialog 断言增强 |
| 回归 V1.6 不回退 | 正确 | 不变 |
| mock 上链验收 | chainCount 逐态 → ISSUE-002 | **已修复**：916 testScope 显式条目 |
| 并行写集互斥 | 正确 | 不变 |

## 非阻塞备注

1. **§6.1 场景 #10 为「建议」而非「强制」**：与 REQ-WSC-ORDER-017 的 P1 优先级一致，不阻塞发布门禁。若后续 PO 升级 017 为 P0，需同步将场景 #10 改为强制。
2. **TASK-WSC-918 testScope 的版本 UI 断言依赖前端渲染**：命名用例 `918-contract-version-ui` 需要详情页版本区有可断言的 testid。建议 918 developer 在实现时为版本号、版本列表、历史展开控件分配明确 testid。
3. **R1 非阻塞备注第 1-3 条**（ADMIN 代操作 E2E 深度、数据量基线、脱敏 mock 局限性）在 R2 中无变化，仍为非阻塞备注，不重新开 ISSUE。

## 决策

**APPROVE**

- ISSUE-QA-001 / 002 / 003 全部已关闭，closeWhen 条件逐条字面满足。
- 其他角色 ISSUE 吸收后无新测试风险；测试覆盖整体改善。
- 未执行 Vitest / JUnit / Playwright；未代替 developer / tester 宣称通过。
- 不代写 `ai/runs/**/state.yaml` 或 `events.jsonl`。
