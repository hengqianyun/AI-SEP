# Round 2 Review — Plan Editor（计划结构完整性 · 复评）

```yaml
reviewId: REV-PLAN-WSC9-R2-plan-editor
planId: PLAN-WSC-9.2
round: 2
role: planEditor
snapshotIdAtReview: SNAP-WSC-009
actorInstance: plan-editor-wsc-012-r2
decision: APPROVE
summary: |
  作为隔离评审实例（≠修订作者 plan-editor-wsc-012-r2），对照 planEditor 批准门禁
  核对 PLAN-WSC-9.2 Round 2 修訂稿：元數據 planId=PLAN-WSC-9.2 / round=2 /
  basedOn=PLAN-WSC-9.1 / snapshotId=SNAP-WSC-009 / contractsTarget=wsc-contracts@2.3.2
  均正確；任務數量不變（仍 9 個，911–919）；修訂說明表列出 16 條 ISSUE 全部對應
  `<!-- R2 revised: ISSUE-xxx -->` 標記且標記位置正確；R1 三條非阻塞備註（矩陣鍵名
  措辭差異、矩陣未覆蓋只讀端點、數字合約版本 entity/repository 歸屬）在 R2 中仍
  有效但不構成阻塞缺陷；修訂未引入新的 writeSet 與互斥表衝突；DAG 仍無環；
  §0.3 文件級互斥表與各任務 writeSet/denyModify 字面路徑對齊。內部一致性未被削弱。
```

## 評審背景

本次為 Round 2 復評，基於以下材料：
- **PLAN-WSC-9.2**（Round 2 修訂稿，吸收 Round 1 全部 16 條 ISSUE）
- **REV-PLAN-WSC9-R1-plan-editor.md**（本人 R1 評審，結論 APPROVE + 3 條非阻塞備註）
- **plan-editor.md**（角色定義與批准門禁）

**評審焦點**：
1. R1 APPROVE + 3 條非阻塞備註 → 確認內部一致性未被削弱
2. 修訂說明表與 `<!-- R2 revised: ISSUE-xxx -->` 標記是否一致（16 條 ISSUE）
3. 元數據正確性（planId、round、basedOn、snapshotId、contractsTarget）
4. 任務數量/編號未變（仍 9 個，911–919）
5. 修訂是否引入新的內部矛盾

## 1. 元數據核對

| 項 | 期望值 | PLAN-WSC-9.2 實際值 | 結論 |
|---|---|---|---|
| `planId` | PLAN-WSC-9.2 | `PLAN-WSC-9.2` | **正確** |
| `round` | 2 | `2` | **正確** |
| `basedOn` | PLAN-WSC-9.1 | `PLAN-WSC-9.1` | **正確** |
| `lineageFrom` | PLAN-WSC-8.3 | `PLAN-WSC-8.3` | **正確** |
| `snapshotId` | SNAP-WSC-009 | `SNAP-WSC-009` | **正確** |
| `contractsTarget` | wsc-contracts@2.3.2 | `wsc-contracts@2.3.2` | **正確** |
| `status` | DRAFT | `DRAFT` | **正確** |
| `planType` | CANDIDATE | `CANDIDATE` | **正確** |
| `taskCount` | 9 | `9` | **正確** |
| `runId` | RUN-WSC-012 | `RUN-WSC-012` | **正確** |
| `authorRoles` | [planEditor] | `[planEditor]` | **正確** |
| `actorInstance` | plan-editor-wsc-012-r2 | `plan-editor-wsc-012-r2` | **正確** |

**結論**：全部元數據正確。

## 2. 任務數量/編號核對

| 任務 ID | R1 存在 | R2 存在 | 結論 |
|---|---|---|---|
| TASK-WSC-911 | ✓ | ✓ | 不變 |
| TASK-WSC-912 | ✓ | ✓ | 不變 |
| TASK-WSC-913 | ✓ | ✓ | 不變 |
| TASK-WSC-914 | ✓ | ✓ | 不變 |
| TASK-WSC-915 | ✓ | ✓ | 不變 |
| TASK-WSC-916 | ✓ | ✓ | 不變 |
| TASK-WSC-917 | ✓ | ✓ | 不變 |
| TASK-WSC-918 | ✓ | ✓ | 不變 |
| TASK-WSC-919 | ✓ | ✓ | 不變 |

**結論**：仍為 9 個任務（911–919），未增未減，編號不變。

## 3. ISSUE 標記一致性核對

修訂說明表（§「修訂說明 / 待 R2 復評」）列出 16 條 ISSUE。逐一核對 `<!-- R2 revised: ISSUE-xxx -->` 標記：

| ISSUE | 修訂說明表位置 | `<!-- R2 revised -->` 標記位置 | 一致 |
|---|---|---|---|
| ISSUE-API-001 | §3.1 / 913 / 916 | §3.1 L232 + L255；913 L378 | ✓ |
| ISSUE-API-002 | §3.1 / 916 | §3.1 L232 + L244 | ✓ |
| ISSUE-API-003 | §3.1 / 913 | §3.1 L232 + L240 | ✓ |
| ISSUE-API-004 | §3.1 / 913 | §3.1 L232 + L243 | ✓ |
| ISSUE-UX-001 | 917 | 917 L551 + L572 | ✓ |
| ISSUE-UX-002 | 917 | 917 L551 + L573 | ✓ |
| ISSUE-UX-003 | 917 | 917 L551 + L574 | ✓ |
| ISSUE-SEC-001 | §1.2 / 913 | §1.2 L162；913 L378；912 L276；§8 L787 | ✓ |
| ISSUE-SEC-002 | 913 / 916 / §3.1 | §3.1 L232 + L251；913 L378；916 L504 | ✓ |
| ISSUE-SEC-003 | 916 / 913 | 916 L504 + L536 | ✓ |
| ISSUE-QA-001 | 919 / §6.1 / 918 | 919 L627；§6.1 L712；918 L591 + L619–621；§7 L765 | ✓ |
| ISSUE-SEC-004 | 913 / 916 | §1.2 L171；913 L378；916 L504；§3.2 L281；§8 L782 | ✓ |
| ISSUE-QA-002 | 916 | 916 L504 + L542 | ✓ |
| ISSUE-QA-003 | 918 | 918 L591 + L617 | ✓ |
| ISSUE-API-005 | §3.1 | §3.1 L232 + L247 | ✓ |
| ISSUE-API-006 | YAML 元數據 | YAML L72（§0.1 表格內） | ✓ |

**結論**：16 條 ISSUE 全部有對應 `<!-- R2 revised -->` 標記，標記位置與修訂說明表描述的修訂章節/任務一致。

## 4. R1 非阻塞備註復評

### 備註 1：RBAC 矩陣鍵名與 OpenAPI 端點路徑措辭差異

**R1 描述**：`ordersSubmitContractApi` 對應端點為 `POST /orders/{orderId}/contract`，端點路徑不含 "submit"。

**R2 狀態**：§3.1 矩陣表中鍵名仍為 `ordersSubmitContractApi`，端點路徑仍為 `POST /orders/{orderId}/contract`。矩陣注釋「逐 cell 與 OpenAPI description 一致」的要求仍存在措辭差異。

**結論**：**仍有效**。不構成阻塞——鍵名作為內部標識符不影響運行時行為，但 911 實現時應在 OpenAPI description 中顯式標註矩陣鍵映射，或將鍵名改為 `ordersContractApi`。

### 備註 2：RBAC 矩陣未覆蓋只讀端點

**R1 描述**：`GET /orders/notices/current` 和 `GET /orders/{orderId}/attachment` 在契約表中已定義，但 RBAC 矩陣表未包含。

**R2 狀態**：§3.1 RBAC 矩陣字面表仍只包含 8 行訂單能力鍵（`ordersUI` 到 `ordersAttachmentGet`），其中 `ordersAttachmentGet` 已新增（R2 修訂）。但 `GET /orders/notices/current`（統一須知）仍未出現在矩陣表中。

**結論**：**部分改善**（附件下載已納入矩陣）。須知端點仍不在矩陣中——但因其為平台統一資源、所有已認證角色均可讀取，可視為 uniform-access 端點。不構成阻塞，建議 911 在矩陣表底部以注釋形式說明。

### 備註 3：數字合約版本 entity/repository 歸屬

**R1 描述**：918 需寫入 `t_order_contract_version` 表，但 918 writeSet 僅含 `service/order/**`，不含 entity/repository 路徑。

**R2 狀態**：918 writeSet（第 601–602 行）仍為 `backend/.../service/order/**`（後端），不含 entity/repository。913 writeSet（第 391–393 行）含 `entity/**`（僅訂單相關實體）和 `repository/**`（僅訂單相關 Repository）。916 writeSet（第 516 行）含「訂單相關 `entity`/`repository`/`model`（附件、明細、事件、鏈日志）」。

**結論**：**仍有效**。`t_order_contract_version` 的 entity/repository 由 913 或 916 初始化，918 在 service 層調用——這與 913/916 的寬泛 entity 寫權限兼容。建議 916 writeSet 注釋明確包含合約版本 entity/repository 初始化。

**R1 三條備註總結**：全部在 R2 中仍有效，均為非阻塞建議。修訂未削弱 R1 已識別的內部一致性。

## 5. 內部矛盾檢查（新增修訂是否引入）

### 5.1 writeSet 與互斥表衝突檢查

逐任務核對 writeSet 與 §0.3 文件級互斥總覽：

| 互斥規則 | R2 狀態 | 衝突 |
|---|---|---|
| `contracts/**` + `frontend/src/api/**` → 911 獨占 | 911 writeSet 包含；他任務 denyModify | **無** |
| `**/sql/**` → 912 獨占 | 912 writeSet 包含；他任務 denyModify `**/sql/**` | **無** |
| 訂單 BE（controller/service/entity/repository/model）→ 913→916 串行 | 913 writeSet 包含；916 dependsOn 913；互斥表標注串行 | **無** |
| RbacMatrix/WriteAuthorization → 913→916 串行 | 913 writeSet 含兩文件（僅增訂單方法）；916 writeSet 同（掛載 confirm/contract/cancel） | **無** |
| WorkbenchLayout.vue → 914 獨占 | 914 writeSet 包含；915/917/918 denyModify | **無** |
| useCanWrite.ts → 914 獨占全文 | 914 writeSet 包含；913/915/916/917 denyModify | **無** |
| routes.ts → 914→915 串行 | 914 增列表路由；915 增 subscribe/detail；917 denyModify routes | **無** |
| features/order/** → 915→917→918 串行 | 依賴鏈正確；915/917/918 writeSet 不重疊同波 | **無** |
| ProductDetailPage.vue → 915 獨占 | 915 writeSet 包含；918 denyModify | **無** |
| tests/e2e/** → 919 獨占 | 919 writeSet 包含；911–918 denyModify | **無** |
| catalog/browse/** → 無寫任務 | 全任務 denyModify | **無** |
| styles/theme → 全只讀 | 914/915 denyModify | **無** |
| ai/runs/**/state.yaml → 無寫任務 | 全任務 denyModify | **無** |

**結論**：writeSet 與互斥表一致，無衝突。

### 5.2 DAG 與依賴鏈檢查

§6 DAG 圖（Mermaid）與各任務 `dependsOn` 交叉核對：

| 任務 | dependsOn | DAG 圖邊 | 一致 |
|---|---|---|---|
| 911 | [] | 根節點 | ✓ |
| 912 | [911] | 911→912 | ✓ |
| 913 | [911, 912] | 911→913, 912→913 | ✓ |
| 914 | [911] | 911→914 | ✓ |
| 915 | [913, 914] | 913→915, 914→915 | ✓ |
| 916 | [913] | 913→916 | ✓ |
| 917 | [915, 916] | 915→917, 916→917 | ✓ |
| 918 | [916, 917] | 916→918, 917→918 | ✓ |
| 919 | [915, 917, 918] | 915→919, 917→919, 918→919 | ✓ |

**結論**：DAG 無環，依賴鏈正確。

### 5.3 修訂引入的新增寫集衝突檢查

R2 新增/修改的 writeSet 內容：
- **ISSUE-API-001**（ChainAttestationPort 並列方案）：911 新增 `chain/OrderChainAttestationPort.md` 寫入；913/916 新增 `OrderChainAttestationPort.java` 接口 + mock 適配器。互斥表中 911 獨占 `contracts/**`，913/916 在 `common/port/**` 下——**無衝突**。
- **ISSUE-SEC-004**（mock 上鏈隔離）：913 acceptance 增安全約束；不新增 writeSet 路徑——**無衝突**。
- **ISSUE-UX-001/002/003**（前端 UX 冻結）：917 acceptance 增具體 UX 規格；不新增 writeSet 路徑——**無衝突**。
- **ISSUE-QA-001**（數字合約版本 UI）：918 testScope 增 Vitest 斷言；不新增 writeSet 路徑——**無衝突**。
- **ISSUE-QA-002**（chainCount 逐態）：916 testScope 增 chainCount 斷言；不新增 writeSet 路徑——**無衝突**。

**結論**：所有修訂均未引入新的 writeSet 衝突。

## 6. 結構完整性門禁核對（R2 重新評估）

| 門禁項 | R1 結論 | R2 變化 | R2 結論 |
|---|---|---|---|
| 所有 P0/P1 REQ 均有驗收標準 | 通過 | 無變化 | **通過** |
| API / 數據 / UI 契約無未決衝突 | 通過 | §3.1 契約更詳細（multipart 硬凍結、響應枚舉、confirm 無 body、須知含版本） | **通過** |
| 每任務有依賴、文件邊界、讀寫集、測試範圍 | 通過 | 無結構性變化 | **通過** |
| DAG 無環；並行寫集無交集 | 通過 | 無變化 | **通過** |
| 安全/遷移/部署/回滾風險已處理或明確接受 | 通過 | §8 新增訂單號可預測風險、mock 隔離風險 | **通過** |
| 所有必需角色輸出 APPROVE | 不適用 | R2 進行中；本文件為 planEditor 復評 | **不適用本角色單點關閉** |
| 候選聲明 / DRAFT·CANDIDATE | 通過 | 無變化 | **通過** |
| 預落地對賬節 | 通過 | §0.2 更明確 contracts/VERSION=2.3.3 對賬 | **通過** |

## 7. SNAP-WSC-009 一致性核對（R2 確認）

R1 已完整核對 SNAP-WSC-009 一致性。R2 修訂未改變 REQ 映射（§7 仍為 21 條 REQ）、PO 锁定項、波次語義。確認 R1 結論在 R2 中仍然成立。

## 決策

`APPROVE` — 本角色無阻塞級結構缺陷；R1 三條非阻塞備註在 R2 中仍有效，修訂未引入新的內部矛盾或結構缺陷。

不偽造他角色 `APPROVE`；不關閉非本角色異議；全員獨立共識與 Orchestrator 門禁通過前不得將本計劃視為可派發實現權威；**不得**將 `status` 標為 `APPROVED` 或寫入 `planning/approved/`；本評審**不**寫 `ai/runs/**`、**不** commit。
