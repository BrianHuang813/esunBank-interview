# 實作計畫與完成定義

## Phase 1：Database

- 完成四張資料表、FK、index、CHECK 與 active borrowing unique constraint。
- 建立所有查詢與寫入 Stored Procedures。
- 以兩個資料庫 session 驗證同時借同一 inventory 只有一人成功。
- 提供可重跑的 DDL、DML 與程序腳本。

完成定義：空資料庫可由 `DB/` 腳本初始化，借／還任一步失敗時兩張表皆不會留下半套資料。

## Phase 2：Backend

- 註冊、登入、JWT、BCrypt。
- 書目列表與明細。
- 借書、還書、我的借閱紀錄。
- 統一 validation 與 error response。
- repository integration tests 與主要 API tests。

完成定義：所有 API 符合 `api-contract.md`，data 層只透過 Stored Procedure 存取資料庫，測試可重複執行。

## Phase 3：Frontend

- 註冊／登入頁與 session 狀態。
- 書目列表、搜尋、明細與副本狀態。
- 借書操作、衝突訊息、個人借閱紀錄與還書。
- loading、empty、error、disabled 等完整狀態。
- 不使用 `v-html`，不把 token 寫入 URL 或 log。

完成定義：使用者能完成註冊 → 登入 → 借書 → 還書流程，重新整理後有合理 session 行為，手機與桌面皆可操作。

## Phase 4：整合與交付

- README 補齊初始化、啟動、測試與測試帳號。
- 確認 DDL/DML 位於 `DB/`。
- 執行 backend tests、frontend tests/build 與主要人工流程。
- 確認 GitHub repository 不含 `.env`、密碼、JWT secret、target 或 node_modules。

## 建議工作切分

Database contract 應先固定；之後前後端可依 API 文件並行施工。前端先使用 mock data，後端先以 API tests 驗證 JSON 契約，整合時只替換 service endpoint。
