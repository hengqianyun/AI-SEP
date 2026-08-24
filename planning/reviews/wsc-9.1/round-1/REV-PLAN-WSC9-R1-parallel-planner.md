# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC9-R1-parallelPlanner
planId: PLAN-WSC-9.1
round: 1
role: parallelPlanner
actorInstance: parallel-planner-wsc-012-r1
snapshotIdAtReview: SNAP-WSC-009
decision: APPROVE
summary: |
  DAG 无环（9 条任务、8 条边、最长路径深度 7）；dependsOn 与 Mermaid/§6 波次表一致；
  Wave A 912∥914 写集互斥成立（sql migration vs 壳层前端），913∥914 由依赖链强制错峰；
  911 契约唯一写、912 Flyway 唯一写、919 E2E 唯一写，串行交接正确；
  订单 BE 913→916 串行、订单 FE 915→917→918 串行，无假并行；
  routes.ts / useCanWrite.ts 均为文件级独占，不存在片段散文切割；
  Wave D E2E 前置依赖齐全（传递闭包含 911–918）；
  任务粒度均衡，无过大或过小任务。无 ISSUE。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-9.1.md` |
| 快照 | `product/requirements/SNAP-WSC-009.md`（PO 2026-08-19 APPROVED） |
| 角色装载 | `ai/agents/parallel-planner.md` |
| 参考格式 | `planning/reviews/wsc-8.1/round-1/REV-PLAN-WSC8-R1-parallel-planner.md` |
| 实例 | `parallel-planner-wsc-012-r1` |
| 本角色权限 | 只读计划/产品/目录结构；本文件为评审落盘；**未**改计划/源码/`ai/runs/**` |

## 评审范围与方法

逐项校验候选计划 §0.3 文件互斥、§5 任务包 dependsOn/writeSet/denyModify、§6 DAG/Mermaid/波次表、§3 契约唯一写、§3.2 Flyway 唯一写。对照 SNAP-WSC-009 需求映射（§7）验证每任务 REQ 覆盖。以 §6.1 E2E 场景反推 Wave D 前置依赖完整性。

| 维度 | 结论 |
|---|---|
| DAG 无环 | **通过** |
| dependsOn ↔ Mermaid / §6 一致 | **通过** |
| Wave A 912∥914 写集互斥 | **通过** |
| 913∥916 禁并行 | **通过** — 依赖链强制串行 |
| 914∥915 禁并行 | **通过** — dependsOn 强制串行 |
| 915∥917∥918 串行链 | **通过** — dependsOn 强制串行 |
| 不稳定读（api/contracts） | **通过** — 911 VERIFIED 后 912/914 方启动；913 须 911+912 VERIFIED |
| `contracts/**` + `frontend/src/api/**` 唯一写 | **通过**（911） |
| Flyway / sql 独占 | **通过**（912） |
| `RbacMatrix` / `WriteAuthorizationInterceptor` 写序 | **通过**（913→916 串行） |
| `useCanWrite.ts` 独占 | **通过**（914 独占全文） |
| `routes.ts` 串行增量 | **通过**（914→915 串行，917 denyModify） |
| `tests/e2e/**` 唯一写 | **通过**（919） |
| Wave D E2E 前置依赖 | **通过** — 见 §3.7 |
| 波次/任务粒度 | **通过** — 见 §4 / §5 |
| 字面 writeSet / scope-check 可调度 | **通过** |
| mergeAfter / 波次 VERIFIED 链 | **可接受** — §6 四波 + dependsOn 足够 |

## 1. DAG 无环校验

边集合（§5 `dependsOn` + §6 Mermaid）：

| from | to |
|---|---|
| TASK-WSC-911 | TASK-WSC-912, TASK-WSC-913, TASK-WSC-914 |
| TASK-WSC-912 | TASK-WSC-913 |
| TASK-WSC-913 | TASK-WSC-915, TASK-WSC-916 |
| TASK-WSC-914 | TASK-WSC-915 |
| TASK-WSC-915 | TASK-WSC-917, TASK-WSC-919 |
| TASK-WSC-916 | TASK-WSC-917, TASK-WSC-918 |
| TASK-WSC-917 | TASK-WSC-918, TASK-WSC-919 |
| TASK-WSC-918 | TASK-WSC-919 |

共 **8 条边**，全部从低编号指向高编号（911<912<…<919），**无回边、无环**。

拓扑序示例：`911 → (912∥914) → 913 → 915 → (916 starts) → 917 → 918 → 919`（含隐含的 913→916→917 链）。

**最长路径（关键路径）**：

| 路径 | 深度（边数） |
|---|---|
| 911→912→913→915→917→918→919 | **7** |
| 911→912→913→916→917→918→919 | **7** |
| 911→914→915→917→918→919 | 6 |

关键路径深度 **7**，9 个任务中 7 个落在关键路径上。可接受——详见 §5 分析。

## 2. 依赖核对

| taskId | dependsOn（§5） | Mermaid | 判定 |
|---|---|---|---|
| TASK-WSC-911 | `[]` | 源点，无入边 | **通过** |
| TASK-WSC-912 | `[911]` | 911→912 | **通过** |
| TASK-WSC-913 | `[911, 912]` | 911→913, 912→913 | **通过** |
| TASK-WSC-914 | `[911]` | 911→914 | **通过** |
| TASK-WSC-915 | `[913, 914]` | 913→915, 914→915 | **通过** |
| TASK-WSC-916 | `[913]` | 913→916 | **通过** |
| TASK-WSC-917 | `[915, 916]` | 915→917, 916→917 | **通过** |
| TASK-WSC-918 | `[916, 917]` | 916→918, 917→918 | **通过** |
| TASK-WSC-919 | `[915, 917, 918]` | 915→919, 917→919, 918→919 | **通过** |

**全部 dependsOn 与 Mermaid 边一致**，无遗漏、无多余。

专项：
- **911 契约前置**：912/913/914 均依赖 911，符合「公共契约由独立前置任务拥有」口径。
- **915 双依赖**：913（BE API 存在）+ 914（壳层导航就绪），两个前置均须 VERIFIED。
- **916 仅依赖 913**：916 为 BE 写扩展，不依赖 FE 任务；不依赖 914/915，调度安全。
- **918 双依赖**：916（BE 状态机）+ 917（FE 动作），版本/跳转/脱敏须两者均完成。

## 3. 并行写集与禁并行声明

### 3.1 Wave A：912 ∥ 914

| 资源 | 912 | 914 | 判定 |
|---|---|---|---|
| `backend/**/sql/migration/**` | **write（独占）** | denyModify | **互斥** |
| `frontend/src/layouts/WorkbenchLayout.vue` | deny | **write** | **互斥** |
| `frontend/src/features/auth/composables/useCanWrite.ts` | deny | **write（独占）** | **互斥** |
| `frontend/src/router/routes.ts` | deny | **write** | **互斥** |
| `frontend/src/features/shell/**` | deny | **write** | **互斥** |
| `contracts/**` | deny | deny | 仅 911 |
| `backend/**/java/**` | deny | deny | 归 913 |

**912∥914 允许** — 写集字面完全不相交（sql migration vs 壳层前端）。

### 3.2 913 ∥ 914

913 dependsOn 含 912，914 dependsOn 仅 911。两者可在 911 VERIFIED 后分别启动，但 913 实际须等 912 完成。

| 资源 | 913 | 914 | 判定 |
|---|---|---|---|
| `backend/**/java/**`（controller/order, service/order, entity, repo, model） | **write** | deny | **互斥** |
| `RbacMatrix.java` / `WriteAuthorizationInterceptor.java` | **write** | deny | **互斥** |
| `frontend/src/layouts/WorkbenchLayout.vue` | deny | **write** | **互斥** |
| `frontend/src/features/auth/composables/useCanWrite.ts` | deny | **write** | **互斥** |
| `frontend/src/router/routes.ts` | deny | **write** | **互斥** |

**913∥914 允许**（写集不相交：Java 后端 vs 壳层前端）。两者实际调度因 913 须等 912 而错峰，但即使理论并行，写集也无交集。

### 3.3 913 ∥ 916（禁止）

| 检查 | 结论 |
|---|---|
| 计划声明 | §0.3、§6：**禁止** |
| 916 `dependsOn` | 含 913 |
| 写集重叠 | `controller/order/**`、`service/order/**`、订单 entity/repo/model、`RbacMatrix.java`、`WriteAuthorizationInterceptor.java` |

**禁止成立** — 依赖链强制串行；916 在 913 基础上扩状态写，不可并行。

### 3.4 914 ∥ 915（禁止）

| 检查 | 结论 |
|---|---|
| 计划声明 | §0.3、§6：**禁止并行** |
| 915 `dependsOn` | 含 914 |
| 写集重叠 | `frontend/src/router/routes.ts` |

**禁止成立** — 914 建立 `/orders` 列表路由，915 在其基础上增 subscribe/detail 路由；串行交接正确。

### 3.5 订单 FE 串行链：915→917→918

| 交接 | 写集重叠 | 判定 |
|---|---|---|
| 915→917 | `frontend/src/features/order/**` | 串行，917 在 915 基础上扩动作 |
| 917→918 | `frontend/src/features/order/**` | 串行，918 在 917 基础上扩版本/跳转/脱敏 |

917 dependsOn 含 915+916；918 dependsOn 含 916+917。**串行链成立，无假并行**。

### 3.6 全局所有权矩阵

| 资源 | 唯一写任务 / 写序 | 判定 |
|---|---|---|
| `contracts/**` + `frontend/src/api/**` | **911**（唯一 bump/生成） | **通过** |
| `**/sql/migration/**` | **912**（独占 Flyway） | **通过** |
| `controller/order/**`、`service/order/**`（初版） | **913** | **通过** |
| `controller/order/**`、`service/order/**`（扩展写） | **916**（913 VERIFIED 后） | **通过** |
| 订单 entity/repo/model（初版） | **913** | **通过** |
| 订单 entity/repo/model（扩展） | **916**（913 VERIFIED 后） | **通过** |
| `RbacMatrix.java` / `WriteAuthorizationInterceptor.java` | **913→916**（串行增量） | **通过** |
| `WorkbenchLayout.vue` | **914** | **通过** |
| `useCanWrite.ts` + spec | **914**（独占全文） | **通过** |
| `router/routes.ts` | **914→915**（串行增量；917 denyModify） | **通过** |
| `deepLinkAccess.ts` / `navSurfaces.ts` | **914** | **通过** |
| `features/order/**` | **915→917→918**（串行扩写） | **通过** |
| `ProductDetailPage.vue`（订购 CTA） | **915** | **通过** |
| `OrderAttachmentScanPort` | **916** | **通过** |
| `tests/e2e/**` | **919** | **通过** |
| `features/catalog/browse/**` | **无写任务** | **通过** |
| `frontend/src/styles/**` / `theme/**` | **无写任务** | **通过** |
| `ai/runs/**/state.yaml` | **无写任务** | **通过** |

**全部资源文件级独占或串行交接，无片段散文切割。**

### 3.7 Wave D E2E 前置依赖完整性

919 dependsOn: `[915, 917, 918]`。

| 919 测试内容 | 由哪个上游任务实现 | 是否在 919 依赖传递闭包中 |
|---|---|---|
| 导航开放（§6.1 #1） | 914 | 是（914→915→919） |
| 链内创建 + toast（#2） | 913+915 | 是（913→915→919） |
| 角色列表（#3） | 913+915 | 是 |
| 分页 jumper+pageSize（#4） | 915 | 是 |
| 主路径四步（#5） | 916+917 | 是（916→917→919） |
| 取消二次确认（#6） | 916+917 | 是 |
| 时间线+mock 上链（#7） | 913+916 | 是（913→916→917→919） |
| 越权 403（#8） | 913+916 | 是 |
| V1.6 回归（#9） | 914（不削弱） | 是（914→915→919） |
| 契约/矩阵（契约增量） | 911 | 是（911→…→919） |
| 数据库 schema | 912 | 是（912→913→…→919） |

**Wave D 前置依赖齐全** — 传递闭包覆盖全部 911–918。§6.1 九项强制场景均有可达的上游实现任务。

## 4. 波次拆分合理性

| 波次 | 任务 | 串/并行 | 判定 |
|---|---|---|---|
| **A** | 911 → (912∥914) → 913 → 915 | 911 串行先行；912/914 真并行；913 等 911+912；915 等 913+914 | **合理** |
| **B** | 916 → 917 | 串行（916 VERIFIED → 917） | **合理** — 917 需消费 916 的 API |
| **C** | 918 | 单任务（916+917 VERIFIED → 918） | **合理** |
| **D** | 919 | 单任务（915+917+918 VERIFIED → 919） | **合理** |

波次 A 的并行窗口（912∥914）提供了一个真实的并行加速点。波次 B/C/D 因依赖关系自然串行，无过度串行化。

**潜在优化观察（非 ISSUE）**：915 与 916 无直接 dependsOn 边（915 依赖 913+914，916 依赖 913），理论上可在 913 VERIFIED 后并行。计划选择在波次上将它们分开（915 在 A，916 在 B），由 917 同时依赖两者实现汇合。这是一个合理的调度选择——避免了 915/916 并行时的前端联调风险。

## 5. 任务粒度与关键路径

### 5.1 任务粒度评估

| 任务 | 范围 | 粒度判定 |
|---|---|---|
| 911 | 契约增量冻结 + client 生成 | **适中** — 一次性冻结避免二次 bump，逻辑自洽 |
| 912 | Flyway 迁移脚本 | **适中偏小** — 单一脚本，scope 清晰 |
| 913 | 订单创建/列表/详情 API + RBAC 增量 | **适中偏大** — 3 个 API + 快照 + 时间线 + RBAC；但 BE 初版需这些一起交付 |
| 914 | 壳层导航开放 + useCanWrite | **适中** — 4–5 个前端文件，scope 清晰 |
| 915 | 订购入口/须知/列表/只读详情 UI | **适中偏大** — 多页面组件；但同属 Wave A FE 交付 |
| 916 | 状态机/权限/附件/计费/上链 BE 扩展 | **适中偏大** — 多状态写路径；913 基础上的自然扩展 |
| 917 | 确认/上传/统一确认页/取消 FE | **适中** — 在 915 详情上扩展动作 |
| 918 | 数字合约/外部跳转/脱敏 | **适中偏小** — 3 个独立关注点 |
| 919 | E2E 证据包 | **适中** — 独立 Playwright spec |

**无过大或过小任务**。913/915/916 范围稍大，但各自对应清晰的技术边界（BE 初版 / FE 初版 / BE 扩展），拆分会引入不必要的接口交接成本。

### 5.2 关键路径分析

关键路径：**911→912→913→916→917→918→919**（深度 7）

| 路径段 | 串行原因 | 可否优化 |
|---|---|---|
| 911→912 | 契约须先冻结再建表 | 不可——Flyway 须对齐契约 |
| 912→913 | schema 须先存在再写实体 | 不可——JPA validate |
| 913→916 | 订单 BE 初版须先建立再扩展 | 不可——同一模块写集重叠 |
| 916→917 | FE 动作依赖 BE API | 不可——917 调用 916 的端点 |
| 917→918 | 版本/脱敏依赖主流程 | 可选——918 仅扩展展示层；但 dependsOn 合理 |
| 918→919 | E2E 须全部功能就绪 | 不可——测试门禁 |

**关键路径长度由真实技术依赖决定，无过度串行化**。

### 5.3 串行瓶颈识别

| 潜在瓶颈 | 阻塞范围 | 严重性 | 缓解措施（计划已有） |
|---|---|---|---|
| **911**（契约） | 912/913/914 全部 | 高（源点） | 911 无依赖，可立即启动 |
| **913**（BE 初版） | 915/916 | 中 | 913 仅依赖 911+912，不依赖 FE |
| **916**（BE 扩展） | 917/918 | 中 | 916 在 913 VERIFIED 后可立即启动 |

911 作为 DAG 源点是合理的——契约冻结是所有后续工作的前提。913 和 916 的瓶颈性来自订单 BE 的串行交付需要，符合 §0.3 文件互斥约束。

## 6. 契约唯一写 / Flyway 唯一写串行正确性

### 6.1 契约（911 唯一写）

| 校验 | 结论 |
|---|---|
| `contracts/**` 写入者 | **仅 911** |
| `frontend/src/api/**` 生成者 | **仅 911** |
| VERSION bump | 911 保持 2.3.3（不升除非四头不一致） |
| 911→912→913 契约传递 | 912/913 readSet 含 `contracts/**`（只读）；913 readSet 含 `frontend/src/api/**`（只读） |
| 911→914 契约传递 | 914 readSet 含 `frontend/src/api/**`（只读）+ `contracts/rbac/matrix.yaml`（只读） |
| 他任务 bump 禁止 | §0.3 全局 deny；§1.4 硬门禁 |

**契约唯一写串行正确**。

### 6.2 Flyway（912 唯一写）

| 校验 | 结论 |
|---|---|
| `**/sql/**` 写入者 | **仅 912** |
| 912→913 交接 | 913 denyModify `**/sql/**`；913 在 912 VERIFIED 后对齐 JPA 实体 |
| 实体/schema 对齐 | 912 交付迁移 smoke；913 交付 JPA `validate` 通过 |
| 版本脚本不可变 | §3.2 声明 + 912 acceptance |

**Flyway 唯一写串行正确**。

### 6.3 911→912→913 链

```
911 (contracts frozen) → 912 (schema aligned to contracts) → 913 (JPA entities aligned to schema)
```

三任务严格串行，每步的产出是下一步的输入。**链路完整、无跳跃**。

### 6.4 911→914 链

```
911 (matrix frozen) → 914 (useCanWrite consumes matrix; nav surfaces consume api)
```

914 仅需 911 的契约/矩阵，不需 912 的 schema。**链路正确**。

## 7. 残余观察（非 ISSUE / 不单独阻塞）

1. **915∥916 理论可并行**：两者无直接 dependsOn 边且写集不相交（FE vs BE）。计划选择在波次上错开（915 在 A，916 在 B），由 917 汇合。这是保守但安全的调度策略——避免了前端联调时 BE API 不稳定的风险。**不升格**。

2. **919 未直接 dependsOn 916/913**：经 917 传递已闭合（917→919；917 dependsOn 915+916；915 dependsOn 913+914）。可选补边增强可读性——**不阻塞**。

3. **`allow_simple_order` 默认值 1**：§1.2 已标注为实现假设，PO 已确认。既有产品迁移默认允许，便于 Wave A 验收。若需收紧，可追加迁移改默认——**不阻塞并行调度**。

4. **无任务级 `mergeAfter` 字段**：与 WSC-8.1 本角色口径一致，波次表 + dependsOn 足以确定合入序。

## 覆盖结论

本次评审覆盖 PLAN-WSC-9.1 全部 9 个任务包的 dependsOn、writeSet、denyModify、acceptance、testScope 与 REQ 映射。DAG 无环、并行写集互斥、波次拆分合理、契约/Flyway 唯一写串行正确、Wave D E2E 前置依赖齐全。**无阻塞性 ISSUE**。

## 异议

无 ISSUE。

## 非阻塞备註

| # | 内容 |
|---|---|
| 1 | 915∥916 理论可并行但计划选择串行——保守安全，不升格 |
| 2 | 919 可选补 dependsOn 916/913 直连边增强可读性 |
| 3 | `allow_simple_order` 默认 1 为实现假设，已标注 |

## 决策

`APPROVE` — DAG **无环**且 dependsOn/Mermaid/§6 波次表三者一致；Wave A **912∥914** 写集互斥与 **913∥916** / **914∥915** 禁并行均成立；911/912/919 独占资源清晰；`useCanWrite.ts` / `routes.ts` / 订单 BE / 订单 FE 均为文件级独占或串行交接，不存在片段散文切割；Wave D E2E 传递闭包覆盖全部上游任务。任务粒度均衡，关键路径长度由真实技术依赖决定。**无 ISSUE，可进入下一阶段**。

本角色 **不** 代批其他委员会角色，**不** 代关他人 ISSUE，**不** 将本文件视为计划已全角色 `APPROVED`，**不** 写入 `ai/runs/**`。

开放 ISSUE（本角色本轮）：**0**。
