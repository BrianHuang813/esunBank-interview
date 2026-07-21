# 系統架構

## 1. 系統邊界

系統提供三種能力：帳號驗證、館藏瀏覽、借還書。管理員後台、逾期罰款、預約與通知不在本次範圍。

```text
Browser
  │
  │ Vue SPA / REST JSON / Bearer JWT
  ▼
Spring Boot Application Server
  ├── presentation：HTTP、DTO、輸入驗證
  ├── business：使用案例與業務規則
  ├── data：Stored Procedure gateway、result mapping
  ├── security：JWT、授權、BCrypt
  └── common：統一錯誤、回應與共用型別
  │
  │ JDBC CALL，所有參數皆綁定
  ▼
MySQL 8.4
  ├── users
  ├── books
  ├── inventory
  ├── borrowing_records
  └── Stored Procedures / Transactions / Row Locks
```

正式環境由 Web Server 提供 Vue 靜態檔案並反向代理 `/api` 至 Spring Boot；本機則由 Vite dev server 完成代理。

## 2. 後端依賴規則

```text
presentation  ──▶  business  ──▶  data  ──▶  MySQL
       │                 │            │
       └──────────── common ◀──────────┘
                         ▲
                     security
```

- Controller 僅處理 HTTP status、輸入驗證與登入身分，不直接存取資料庫。
- Service 使用業務語言，例如 `borrowInventory`，不傳遞 JDBC `ResultSet`。
- Repository 只呼叫 Stored Procedure，禁止在 Java 字串拼接 SQL。
- Request/response DTO 不進入 data 層；資料庫 row model 不直接回傳給前端。
- 共用錯誤由 global exception handler 轉成固定錯誤格式。

## 3. 驗證與授權

1. 註冊時檢查手機格式與唯一性，密碼以 BCrypt 產生帶 salt 的 hash。
2. 登入成功後簽發短效 JWT，subject 為不可變的 `userId`。
3. 借書、還書與個人紀錄 API 必須攜帶 `Authorization: Bearer <token>`。
4. 借還書的使用者 ID 一律由 JWT 取得，不接受 client 指定。
5. 登入失敗統一回覆相同訊息，避免洩漏帳號是否存在。

前端不使用 `v-html` 顯示書籍簡介或使用者名稱；所有輸出維持 Vue 的預設 escaping。JWT 不寫入 log，也不放入 URL。

## 4. 交易與併發

借書及還書各由一個 Stored Procedure 完成，該程序持有完整 transaction：

- `START TRANSACTION`
- 以 `SELECT ... FOR UPDATE` 鎖定目標 inventory／open borrowing row
- 檢查目前狀態與操作人
- 同時更新 inventory 與 borrowing_records
- 成功 `COMMIT`，SQL exception 時 `ROLLBACK` 並拋出明確業務錯誤

Spring service 對這兩個 procedure 不再啟動另一個 transaction，避免 MySQL 不支援巢狀 transaction 所造成的隱性 commit。每個使用案例僅呼叫一個原子程序。

除了 row lock，`borrowing_records` 也會以唯一約束防止同一 inventory 同時出現兩筆未歸還紀錄，形成第二層資料完整性保護。

## 5. 錯誤與觀測

API 錯誤格式固定包含 `code`、`message`、`timestamp`、`path`。預期中的衝突使用 HTTP 409；認證失敗使用 401；越權使用 403；找不到資源使用 404。

伺服器 log 可記錄 request correlation ID、路徑、狀態碼與耗時，但不得記錄密碼、JWT 或完整手機號碼。

## 6. 測試邊界

- Unit：service 規則、DTO validation、JWT 與例外映射
- Repository integration：Stored Procedure 的參數與結果映射
- Database integration：重複借閱、重複歸還、rollback 與並發借閱
- Frontend component：登入狀態、錯誤顯示、借還書按鈕狀態
- End-to-end：註冊 → 登入 → 借書 → 個人紀錄 → 還書
