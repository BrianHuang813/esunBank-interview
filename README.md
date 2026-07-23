# 玉山銀行 Java 實作題 B：線上圖書借閱系統

以 Vue 3、Spring Boot 3 與 MySQL 8.4 實作的前後端分離圖書借閱系統，提供手機註冊／登入、館藏查詢、借書、還書及個人借閱紀錄。

> 設計導讀請見 [HIGHLIGHTS.md](HIGHLIGHTS.md)。

## 技術架構

```text
Browser
   │
   │ HTTP / REST JSON
   ▼
Nginx Web Server
   ├── 提供 Vue 靜態檔案
   ├── Security Headers / CSP
   └── /api/v1/* reverse proxy
             │
             ▼
Spring Boot Application Server
   ├── presentation  Controller、DTO、輸入驗證
   ├── business      註冊、登入、借還書 use cases
   ├── data          Stored Procedure gateway
   ├── security      JWT、BCrypt、登入防護
   └── common        統一回應與例外處理
             │
             │ JDBC CALL（參數綁定）
             ▼
MySQL 8.4
   ├── users / books / inventory / borrowing_records
   ├── Stored Procedures
   └── Transaction / Row Lock / Constraints
```

| 區域 | 技術 |
|---|---|
| Frontend | Vue 3、Vite、Vue Router、Pinia、Axios |
| Web Server | Nginx |
| Backend | Java 21、Spring Boot 3、Spring Security、Spring JDBC |
| Authentication | Bearer JWT、BCrypt |
| Database | MySQL 8.4、Stored Procedure、Transaction |
| Build / Runtime | Maven、npm、Docker Compose |

### 核心機制

- 借還書由 Stored Procedure 在單一 transaction 內完成，使用 `SELECT ... FOR UPDATE` 避免併發重複借閱。
- `borrowing_records` 另有唯一約束，防止同一館藏同時存在兩筆未歸還紀錄。
- 所有 SQL 參數皆透過 JDBC 綁定，不拼接使用者輸入。
- 密碼使用 BCrypt 加鹽雜湊；JWT 僅由後端簽發並驗證。
- 登入同時針對帳號與來源 IP 限流，並以 dummy BCrypt 降低帳號探測時間差。
- Vue 使用預設 escaping，Nginx 提供 CSP、`nosniff` 與防 iframe headers。
- Database runtime user 只能執行 Stored Procedure，不能直接讀寫資料表。

## Docker Compose 部署

### 1. 準備環境變數

```bash
cp .env.example .env
```

編輯 `.env`，至少填入：

```dotenv
MYSQL_PASSWORD=<application-database-password>
MYSQL_ROOT_PASSWORD=<root-database-password>
JWT_SECRET=<at-least-32-random-bytes>
```

可使用 `openssl rand -base64 32` 產生資料庫密碼，使用
`openssl rand -base64 48` 產生 JWT secret。這些 secret 沒有預設值，未設定時 Compose 會拒絕啟動。

### 2. 建置並啟動

```bash
docker compose up --build -d
```

啟動後：

- Web UI：`http://localhost:5173`
- API：`http://localhost:5173/api/v1`
- API smoke test：`http://localhost:5173/api/v1/books`

只有 Nginx 綁定在 `127.0.0.1`。Spring Boot 與 MySQL 不發布 host port，只能經由 Compose 內部網路存取。

### 3. 檢查與停止

```bash
docker compose ps
docker compose logs -f api
docker compose down
```

MySQL 資料保存在 named volume。`docker compose down` 不會刪除資料；帶有
`--volumes` 會刪除資料，僅能用於確認不需保留內容的開發環境。

## 測試

後端：

```bash
cd backend
mvn test
```

前端：

```bash
cd frontend
npm ci
npm test
npm run build
```

目前自動化測試涵蓋密碼處理、登入成功與失敗、帳號鎖定、IP 限流、JWT、輸入與分頁邊界、錯誤碼翻譯及前端 validation/formatter。

## 專案結構

```text
.
├── frontend/                 Vue SPA 與 Nginx 設定
├── backend/                  Spring Boot REST API
├── DB/
│   ├── ddl/                  Tables、constraints、indexes
│   ├── dml/                  初始書籍與館藏
│   ├── procedures/           Stored Procedures 與 transactions
│   └── 999_limit_app_privileges.sh
├── compose.yaml
├── .env.example
└── HIGHLIGHTS.md             委員導讀與設計亮點
```
