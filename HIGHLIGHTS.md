# 專案亮點與設計導讀

這份文件提供審查委員快速理解本專案的設計取捨、資料完整性及資安機制。系統的重點不只是完成借還書畫面，而是確保在併發、異常與惡意輸入下，資料仍維持一致。

## 1. 借還書交易完整性

借書需要同時更新 `inventory` 並新增 `borrowing_records`；還書則要同時更新借閱紀錄與館藏狀態。兩個流程各由一支 Stored Procedure 完成，並在資料庫內管理完整 transaction：

```text
START TRANSACTION
  → SELECT target row FOR UPDATE
  → 驗證館藏狀態與操作人
  → 更新 inventory
  → 新增或更新 borrowing_records
COMMIT

任何 SQL exception → ROLLBACK → RESIGNAL
```

這樣即使第二個資料表寫入失敗，第一個資料表也不會留下半套資料。

關鍵程式：

- [借還書 Stored Procedures](DB/procedures/003_borrowing_procedures.sql)
- [BorrowingRepository](backend/src/main/java/com/esunbank/library/data/repository/BorrowingRepository.java)

## 2. 併發借閱的雙層保護

兩位使用者可能同時看到同一本館藏為可借閱。本專案使用兩層機制避免重複出借：

1. Stored Procedure 以 `SELECT ... FOR UPDATE` 鎖定目標 inventory。
2. `borrowing_records.active_inventory_id` 是 generated column，並具有 unique constraint。

第一層負責正常併發排序，第二層則是資料庫最後防線。實際黑箱測試中，兩個並行請求只會有一個成功，另一個收到 `409 INVENTORY_NOT_AVAILABLE`。

## 3. Stored Procedure gateway 與 SQL Injection 防護

Java data layer 只呼叫固定的 `CALL sp_*`，所有輸入皆使用 JDBC placeholder 綁定。Repository 不產生 dynamic SQL，也不把使用者輸入串接進查詢。

```java
jdbcTemplate.queryForObject(
    "CALL sp_borrow_create(?, ?)",
    rowMapper,
    userId,
    inventoryId
);
```

資料庫 runtime user 亦遵循最小權限原則：

- 允許：`EXECUTE` Stored Procedure
- 拒絕：直接 `SELECT`、`INSERT`、`UPDATE`、`DELETE` tables

因此即使 Application Server 的資料庫連線資訊外洩，攻擊面仍受到限制。

## 4. 登入與帳號探測防護

登入機制除了 JWT 與 BCrypt，也處理常被忽略的暴力破解及帳號探測：

- 不存在的手機號碼仍執行 dummy BCrypt，縮小登入時間差。
- 帳號預設在 15 分鐘內失敗 5 次後鎖定 15 分鐘。
- 同一來源 IP 預設 1 分鐘失敗 20 次後回傳 HTTP 429。
- 成功登入會清除該帳號的失敗紀錄。
- 記憶體狀態具有容量上限及逾時清理，避免假帳號造成無限成長。
- 稽核 log 只記錄遮罩手機、來源 IP、失敗次數與鎖定狀態，不記錄密碼或 JWT。

目前為單一 Application Server，因此採用 process-local state；若水平擴充，介面可改接 Redis，讓多個 instance 共用限流狀態。

關鍵程式：

- [AuthService](backend/src/main/java/com/esunbank/library/business/service/AuthService.java)
- [LoginAttemptService](backend/src/main/java/com/esunbank/library/security/LoginAttemptService.java)
- [JwtAuthenticationFilter](backend/src/main/java/com/esunbank/library/security/JwtAuthenticationFilter.java)

## 5. 瀏覽器與 API 安全

- Vue template 維持預設 escaping，未使用 `v-html` 呈現資料庫內容。
- Nginx 設定 Content Security Policy、`X-Content-Type-Options`、`X-Frame-Options` 與 Referrer Policy。
- CORS 只允許設定的 origins、methods 與 headers。
- JWT 由 `Authorization: Bearer` 傳遞，不放入 URL。
- API 錯誤不回傳 stack trace 或資料庫例外內容。
- 借還書的 user ID 一律取自 JWT，不接受 client 自行指定。

## 6. 穩定性與輸入邊界

- 列表限制 `size` 最大 100、`page` 最大 1,000,000。
- offset 使用 `long` 計算，避免整數溢位形成資料庫 500。
- Axios、Nginx proxy 與 Hikari connection 均設定 timeout。
- MySQL 提供 healthcheck，API 只在資料庫健康後啟動。
- 統一 exception handler 將驗證、權限、衝突及未預期錯誤映射成固定格式。

## 7. 部署邊界

Compose 預設只將 Nginx 綁定至 `127.0.0.1`：

```text
Host
  └── 127.0.0.1:5173 → Nginx
                          └── internal → Spring Boot:8080
                                              └── internal → MySQL:3306
```

DB 密碼、root 密碼與 JWT secret 都是必填環境變數，沒有可直接誤用的 fallback。Spring Boot 和 MySQL 不直接發布 host port。

## 8. 可驗證的測試結果

| 驗證項目 | 結果 |
|---|---|
| 後端自動化測試 | 29 passed |
| 前端自動化測試 | 8 passed |
| Frontend production build | passed |
| 未登入借還書 | 401 |
| 他人借閱紀錄還書 | 403 |
| 同館藏並行借閱 | 一筆 201、一筆 409 |
| 重複還書 | 409 |
| 強制第二次資料寫入失敗 | transaction rollback 成功 |
| SQL Injection 搜尋字串 | 僅作一般參數處理 |
| 極端 page | 400，不產生 500 |
| 帳號與 IP 登入超限 | 429 |
| runtime DB user 直接讀表 | MySQL 1142 拒絕 |