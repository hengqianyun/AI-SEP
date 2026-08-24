# Round 2 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC9-R2-parallel-planner
planId: PLAN-WSC-9.2
round: 2
role: parallelPlanner
actorInstance: parallel-planner-wsc-012-r2
snapshotIdAtReview: SNAP-WSC-009
decision: APPROVE
summary: |
  R1 APPROVE 结论未被削弱：DAG 仍无环（9 任务、14 条边、最长路径深度 7），波次结构不变；
  R2 修订仅改变任务包内部 acceptance/testScope 与 readSet/writeSet 细化，未改变 dependsOn、波次划分或文件互斥拓扑；
  OrderChainAttestationPort 为只读接口消费（913 建立、916 只读引用），不引入新写集冲突；
  UUID v4 订单号仅影响 913 acceptance（格式断言）和 912 acceptance（VARCHAR(36)），不影响 912→913 依赖方向；
  允许的并行对（912∥914、913∥914）写集仍完全不相交；禁并行声明全部成立。无 ISSUE。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-9.2.md`（Round 2 修訂稿） |
| 快照 | `product/requirements/SNAP-WSC-009.md`（PO 2026-08-19 APPROVED） |
| 角色装载 | `ai/agents/parallel-planner.md` |
| R1 评审 | `planning/reviews/wsc-9.1/round-1/REV-PLAN-WSC9-R1-parallel-planner.md`（APPROVE） |
| 实例 | `parallel-planner-wsc-012-r2` |
| 本角色权限 | 只读计划/产品/目录结构；本文件为评审落盘；**未**改计划/源码/`ai/runs/**` |

## 评审范围与方法

在 R1 APPROVE 基础上，重点复评：
1. R1 结论是否被 R2 修订削弱（DAG 无环、写集互斥、波次结构）
2. R2 修订是否改变 DAG 结构（任务数、dependsOn、波次）
3. `OrderChainAttestationPort`（ISSUE-API-001 新增）是否引入写集冲突
4. UUID v4 订单号（ISSUE-SEC-001）是否影响 912/913 依赖关系

逐项核对 R2 PLAN-WSC-9.2 §0.3、§5 任务包 dependsOn/writeSet/denyModify、§6 DAG/Mermaid/波次表。

## 1. R1 APPROVE 结论复核

| R1 维度 | R1 结论 | R2 状态 | 判定 |
|---|---|---|---|
| DAG 无环 | 通过（9 任务、8 边、深度 7） | **不变** — §6 Mermaid 与 §5 dependsOn 未改 | **未削弱** |
| dependsOn ↔ Mermaid ↔ §6 一致 | 通过 | **不变** — 三者仍完全对齐 | **未削弱** |
| Wave A 912∥914 写集互斥 | 通过 | **不变** — writeSet/denyModify 未改 | **未削弱** |
| 913∥916 禁并行 | 通过 | **不变** — 916 dependsOn 仍含 913 | **未削弱** |
| 914∥915 禁并行 | 通过 | **不变** — 915 dependsOn 仍含 914 | **未削弱** |
| 915→917→918 串行链 | 通过 | **不变** — dependsOn 链未改 | **未削弱** |
| 不稳定读（api/contracts） | 通过 | **不变** — 911 VERIFIED 后 912/914 方启动 | **未削弱** |
| contracts/api 唯一写（911） | 通过 | **不变** | **未削弱** |
| Flyway 唯一写（912） | 通过 | **不变** | **未削弱** |
| RbacMatrix/WriteAuth 串行（913→916） | 通过 | **不变** | **未削弱** |
| useCanWrite.ts 独占（914） | 通过 | **不变** | **未削弱** |
| routes.ts 串行（914→915→917 deny） | 通过 | **不变** | **未削弱** |
| tests/e2e 唯一写（919） | 通过 | **不变** | **未削弱** |
| Wave D E2E 前置依赖齐全 | 通过 | **不变** — 919 dependsOn 仍为 [915, 917, 918] | **未削弱** |
| 波次/任务粒度 | 通过 | **不变** — 仍为 A/B/C/D 四波 | **未削弱** |

**R1 全部结论未被 R2 修订削弱。**

## 2. DAG 结构比对（R1 vs R2）

### 2.1 边集合

| from | to | R1 | R2 |
|---|---|---|---|
| 911 | 912 | ✅ | ✅ |
| 911 | 913 | ✅ | ✅ |
| 911 | 914 | ✅ | ✅ |
| 912 | 913 | ✅ | ✅ |
| 913 | 915 | ✅ | ✅ |
| 913 | 916 | ✅ | ✅ |
| 914 | 915 | ✅ | ✅ |
| 915 | 917 | ✅ | ✅ |
| 915 | 919 | ✅ | ✅ |
| 916 | 917 | ✅ | ✅ |
| 916 | 918 | ✅ | ✅ |
| 917 | 918 | ✅ | ✅ |
| 917 | 919 | ✅ | ✅ |
| 918 | 919 | ✅ | ✅ |

**R2 Mermaid（§6）有 14 条边，R1 有 8 条边**。差异来源：R1 review §1 Mermaid 列表仅枚举了 task→直接后继的压缩边（如 911→912/913/914 合并为一行），R2 §6 Mermaid 展开了全部 14 条单独边。**实际拓扑完全一致**，无新增/删除边。

### 2.2 任务数与波次

| 项 | R1 | R2 | 判定 |
|---|---|---|---|
| 任务数 | 9（911–919） | 9（911–919） | **不变** |
| 波次 | A/B/C/D | A/B/C/D | **不变** |
| 最长路径深度 | 7 | 7 | **不变** |
| 912∥914 并行窗口 | 有 | 有 | **不变** |

**DAG 结构未被 R2 修订改变。**

## 3. OrderChainAttestationPort 写集分析（ISSUE-API-001）

R2 §3.1 新增 `OrderChainAttestationPort.md` 与 `OrderChainAttestationPort` 接口定义：

| 任务 | 对 OrderChainAttestationPort 的操作 | 写集影响 |
|---|---|---|
| **911** | 写入 `contracts/chain/OrderChainAttestationPort.md`（冻结接口定义） | 归 911 契约写集内，不溢出 |
| **913** | writeSet 含 `common/port/OrderChainAttestationPort.java`（接口定义 + `SimulatedOrderChainAttestationAdapter` mock 实现） | 新增写路径，仅 913 写 |
| **916** | readSet 含 `OrderChainAttestationPort`（**只读**消费） | **不写** — 仅读接口注入 |
| **918** | 读取订单 BE | **不写** port |

| 检查 | 判定 |
|---|---|
| 913∥916 是否因 OrderChainAttestationPort 产生写集冲突？ | **否** — 913 写（建立接口+mock），916 只读（注入使用），仍为串行 |
| 911∥913 是否因 OrderChainAttestationPort.md 产生冲突？ | **否** — 911 写 `contracts/chain/`，913 写 `common/port/`（Java 接口），路径不相交；且 913 dependsOn 911，串行 |
| 现有 ChainAttestationPort 是否被削弱？ | **否** — §3.1 明确「与现有 ChainAttestationPort 共存」「现有签名不变」 |
| 文件互斥表（§0.3）是否反映新增？ | **是** — §0.3 `backend/.../common/port/OrderChainAttestationPort.java` 归 913 writeSet |

**OrderChainAttestationPort 不引入新写集冲突，不影响 DAG 结构。**

## 4. UUID v4 订单号对 912/913 依赖的影响（ISSUE-SEC-001）

| 影响面 | 分析 | 判定 |
|---|---|---|
| 912 Flyway 脚本 | `t_order_header.order_no` 改为 `VARCHAR(36) NOT NULL`（存储 UUID v4） | 仅列定义变更，不改表结构拓扑 |
| 912→913 依赖 | 913 仍 dependsOn 912（需 schema 存在写实体） | **依赖方向不变** |
| 913 生成逻辑 | UUID v4 在 service 层生成，不含企业 ID/产品编码/用户 ID 子串 | 实现细节，不改任务边界 |
| 913 acceptance | 新增 UUID v4 格式正则断言 + 不含业务语义子串负例 | 新增测试用例，不改 writeSet |
| 916 acceptance | 无直接影响 | **不变** |

**UUID v4 不影响 912/913 依赖关系，仅细化 acceptance 与列定义。**

## 5. 其他 R2 修订对并行规划的影响

| ISSUE | 修订内容 | 对 DAG/写集/波次的影响 |
|---|---|---|
| ISSUE-API-002 | multipart part 名/schema 冻结 | **无** — 仅 916 acceptance 细化 |
| ISSUE-API-003 | 列表九组字段+envelope 枚举 | **无** — 仅 913/911 acceptance 细化 |
| ISSUE-API-004 | confirm 无 body | **无** — 仅 916 acceptance 细化 |
| ISSUE-API-005 | 须知响应含 version | **无** — 仅 911 契约细化 |
| ISSUE-API-006 | contractsTarget 说明 | **无** — YAML 元数据说明 |
| ISSUE-UX-001 | 确认页布局+disabled | **无** — 仅 917 acceptance 细化 |
| ISSUE-UX-002 | 附件上传交互 | **无** — 仅 917 acceptance 细化 |
| ISSUE-UX-003 | 取消对话框 | **无** — 仅 917 acceptance 细化 |
| ISSUE-SEC-002 | IDOR 负例 | **无** — 仅 913/916 acceptance/testScope 细化 |
| ISSUE-SEC-003 | ADMIN 代操作审计 | **无** — 仅 916 acceptance 细化 |
| ISSUE-SEC-004 | mock 上链隔离 | **无** — 仅 912/913/916 acceptance 细化 |
| ISSUE-QA-001 | 数字合约版本 E2E+UI | **无** — 仅 918/919 testScope 细化 |
| ISSUE-QA-002 | chainCount 逐态 | **无** — 仅 916 testScope 细化 |
| ISSUE-QA-003 | 外部跳转正向断言 | **无** — 仅 918 testScope 细化 |

**16 条 ISSUE 修订均为 acceptance/testScope/契约细节细化，无一改变 dependsOn、writeSet、denyModify 或波次结构。**

## 6. 写集互斥复核（R2 增量后）

### 6.1 允许的并行对

| 并行对 | R2 writeSet 变化 | 判定 |
|---|---|---|
| 912 ∥ 914 | 912 新增 `order_no VARCHAR(36)` 列定义（仍在 `sql/migration/**`）；914 writeSet 不变 | **仍互斥** — sql vs 壳层 |
| 913 ∥ 914 | 913 新增 `OrderChainAttestationPort.java` 写路径（`common/port/`）；914 writeSet 不变 | **仍互斥** — Java backend vs 壳层前端 |

### 6.2 禁并行对

| 禁并行对 | R2 变化 | 判定 |
|---|---|---|
| 913 ∥ 916 | 913 新增 OrderChainAttestationPort 写；916 仅 readSet 引用 | **仍禁止** — 916 dependsOn 913，串行 |
| 914 ∥ 915 | 无变化 | **仍禁止** |
| 915 ∥ 917 | 无变化 | **仍禁止** |
| 917 ∥ 918 | 无变化 | **仍禁止** |

### 6.3 全局所有权矩阵增量

| 新增资源 | 唯一写任务 | 判定 |
|---|---|---|
| `common/port/OrderChainAttestationPort.java` + `SimulatedOrderChainAttestationAdapter` | **913** | **通过** — 916/918 仅 readSet |
| `contracts/chain/OrderChainAttestationPort.md` | **911** | **通过** — 归契约写集 |

**全部资源文件级独占或串行交接，R2 增量未破坏互斥。**

## 7. 残余观察（非 ISSUE）

1. **R2 Mermaid 有 14 条边 vs R1 review 列出 8 条**：仅为展开粒度差异（R1 合并了同源多目标边），实际拓扑一致。R2 §6 Mermaid 更完整，无遗漏。

2. **919 dependsOn 仍为 [915, 917, 918]**，未直连 913/916：R1 §7 备注 #2 已提及可选补边，R2 未改。经 917 传递闭包已覆盖，**不阻塞**。

3. **§0.3 新增 OrderChainAttestationPort 行**：写入者为 913，916 仅 readSet 引用，互斥成立。§0.3 表述准确。

## 覆盖结论

本次复评核对 PLAN-WSC-9.2（Round 2）全部 9 个任务包的 dependsOn、writeSet、denyModify、acceptance、testScope 与 §6 DAG/Mermaid/波次表。R1 APPROVE 的全部结论（DAG 无环、写集互斥、波次合理、契约/Flyway 唯一写、Wave D 前置齐全）**均未被 R2 修订削弱**。16 条 ISSUE 修订均为 acceptance/testScope/契约细节细化，未改变任务拓扑。OrderChainAttestationPort 不引入新写集冲突，UUID v4 不影响依赖关系。**无阻塞性 ISSUE**。

## 异议

无 ISSUE。

## 决策

`APPROVE` — R1 全部结论未被 R2 修订削弱；DAG 仍无环（9 任务、14 条边、深度 7）；允许的并行对写集仍不相交；禁并行声明全部成立；OrderChainAttestationPort 为只读消费不引入新写集冲突；UUID v4 仅影响 acceptance/列定义不影响依赖拓扑；16 条 ISSUE 修订均为细节细化无拓扑影响。**可进入下一阶段**。

本角色 **不** 代批其他委员会角色，**不** 代关他人 ISSUE，**不** 将本文件视为计划已全角色 `APPROVED`，**不** 写入 `ai/runs/**`。

开放 ISSUE（本角色本轮）：**0**。
