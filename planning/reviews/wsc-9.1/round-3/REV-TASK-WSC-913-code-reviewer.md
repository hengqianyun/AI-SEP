```yaml
reviewId: REV-TASK-WSC-913-R3
taskId: TASK-WSC-913
role: codeReviewer
actorInstance: code-reviewer-wsc-913
round: 3
decision: APPROVE
summary: |
  P1-1（分頁下推至數據庫層）已完全修復。Repository 新增三個 `@Query(nativeQuery=true)` 方法，
  均接受 `Pageable` 參數並返回 `Page<TOrderHeaderEntity>`；ADMIN 路徑改用 `searchAdminOrders` 而非
  全量加載；Service 層 `subList` 內存分頁已移除；批量預取（`batchFetchUsers` / `batchFetchEnterprises`）
  保留用於當前頁組裝，避免 N+1。未引入新 P0/P1 問題；SCOPE_CHECK 通過。P2：Repository 中殘留 6 個
  未被調用的全量加載方法（`findAllByOrderByIdDesc` 等），為死代碼，可延期清理。
```

## 1. P1-1 殘留問題逐項驗證

### 1.1 Repository 是否使用 `Pageable` 參數進行數據庫層分頁 — ✅ PASS

`TOrderHeaderRepository` 新增三個分頁方法，均使用 `@Query(nativeQuery=true)` + `Pageable`：

| 方法 | 角色 | 返回類型 | Pageable |
|---|---|---|---|
| `searchAdminOrders(status, keyword, pageable)` | ADMIN | `Page<TOrderHeaderEntity>` | ✅ |
| `searchUserOrders(demandUserId, status, keyword, pageable)` | USER | `Page<TOrderHeaderEntity>` | ✅ |
| `searchProviderOrders(providerEnterpriseId, status, keyword, pageable)` | PROVIDER | `Page<TOrderHeaderEntity>` | ✅ |

Service 層 `listOrders` 構建 `PageRequest.of(page - 1, pageSize)` 傳入（第 318 行），分頁完全在數據庫層完成。

### 1.2 ADMIN 路徑是否不再全量加載 — ✅ PASS

`queryOrdersByRole` 方法（第 492-512 行）ADMIN 分支調用 `orderHeaderRepo.searchAdminOrders(status, keyword, pageable)`，不再調用 `findAllByOrderByIdDesc()`。

### 1.3 是否移除了 `subList` 分頁邏輯 — ✅ PASS

`subList` 僅出現在 Javadoc 註釋（第 285 行：「避免全量加載後 subList 內存分頁」），實際代碼中無任何 `subList` 調用。

### 1.4 是否移除了全量加載方法 — ⚠️ P2（非阻塞）

Repository 中仍保留 6 個全量加載方法：

- `findByDemandUserIdOrderByIdDesc`
- `findByProviderEnterpriseIdOrderByIdDesc`
- `findAllByOrderByIdDesc`
- `findByDemandUserIdAndStatusOrderByIdDesc`
- `findByProviderEnterpriseIdAndStatusOrderByIdDesc`
- `findByStatusOrderByIdDesc`

經全局搜索，**無任何 Java 文件調用這些方法**。它們是死代碼，不影響運行時行為，但增加維護認知負擔。P2 級別，可延期清理。

### 1.5 批量預取是否仍用於當前頁組裝 — ✅ PASS

`listOrders` 方法（第 327-345 行）：
1. 從 `resultPage.getContent()` 取當前頁實體
2. 收集 `demandUserId` 和 `providerEnterpriseId` 集合
3. 調用 `batchFetchUsers` / `batchFetchEnterprises` 批量查詢
4. 將 Map 傳入 `toListItem` 避免逐條 N+1

---

## 2. 修復是否引入新 P0/P1 問題 — ✅ 無

### 2.1 SQL 正確性

三個原生 SQL 均包含：
- `h.del_flag = 0` 邏輯刪除過濾 ✅
- `LEFT JOIN sys_user` / `LEFT JOIN sys_enterprise` 支持跨表關鍵字搜索 ✅
- `CAST(u.id AS CHAR)` / `CAST(e.id AS CHAR)` 處理 BIGINT→VARCHAR 類型匹配 ✅
- 獨立 `countQuery` 避免分頁計數受 JOIN 影響 ✅
- `(:status IS NULL OR :status = '' OR h.status = :status)` 可空狀態篩選 ✅
- `(:kw IS NULL OR :kw = '' OR ...)` 可空關鍵字搜索 ✅

### 2.2 邊界安全

- `page < 1` 自動修正為 1（第 313-315 行） ✅
- `pageSize` 僅允許 10/20/50/100（第 309-312 行） ✅
- PROVIDER 角色 `NumberFormatException` 防護（第 499-505 行） ✅

### 2.3 性能

- JSON_EXTRACT 用於產品名搜索（`JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))`），無法走索引，但產品快照為 JSON 字段且搜索為模糊匹配，可接受。
- 無全量加載風險，分頁限制在數據庫層。

---

## 3. SCOPE_CHECK — ✅ PASS

審查範圍限定為：
- `OrderService.java` — 修改 ✅
- `TOrderHeaderRepository.java` — 修改 ✅

兩文件均在 TASK-WSC-913 writeSet 內（`service/order/**`、`repository/**`）。未發現越權修改。

---

## 4. 發現清單

### P0 — 必須修復

無。

### P1 — 必須修復

無。

### P2 — 可記錄延期

| # | 發現 | 證據 | closeWhen |
|---|---|---|---|
| P2-1 | Repository 殘留 6 個未使用的全量加載方法（`findAllByOrderByIdDesc` 等），為死代碼 | 全局搜索無調用方；Service 層已改用 `search*Orders` 分頁方法 | 後續清理 PR 中移除 |

---

## 5. 決策

**APPROVE**

理由：
1. **P1-1 完全修復**：分頁已下推至數據庫層（`@Query(nativeQuery=true)` + `Pageable`），ADMIN 路徑不再全量加載，`subList` 已移除，批量預取保留。
2. **無新 P0/P1**：SQL 正確性、邊界安全、SCOPE_CHECK 均通過。
3. **P2-1（死代碼）**：非阻塞，可延期清理。

P0/P1 已清零，批准進入測試門禁。
