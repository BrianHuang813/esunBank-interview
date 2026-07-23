# Backend

Spring Boot REST API。程式依題目要求切成四個主要責任層：

- `presentation`：HTTP controller、request/response DTO 與輸入驗證
- `business`：註冊、登入、館藏與借還書 use cases
- `data`：Stored Procedure 呼叫與 result set mapping
- `common`：統一回應、例外與資料庫錯誤碼翻譯
- `security`：JWT、Spring Security 與 BCrypt 設定

層級依賴只能由外向內：`presentation -> business -> data`。Controller 不得直接呼叫 repository，data 層只能使用固定參數的 `CALL sp_*`。

## API

| Method | Path | Authentication |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/books` | Public |
| GET | `/api/v1/books/{isbn}` | Public |
| POST | `/api/v1/borrowings` | Bearer JWT |
| POST | `/api/v1/borrowings/{id}/return` | Bearer JWT |
| GET | `/api/v1/borrowings/me` | Bearer JWT |

詳細 payload 與錯誤代碼請見 [`docs/api-contract.md`](../docs/api-contract.md)。

## 啟動

從 repository root 執行：

```bash
cp .env.example .env
docker compose up --build
```

這會先初始化 MySQL，再由 Web Server 對
`http://localhost:5173/api/v1` 提供 API。API 與資料庫不直接發布 host
port。若要直接以 Maven 啟動，需自行提供可連線的 MySQL 及以下環境變數：

```bash
export DB_URL='jdbc:mysql://localhost:3306/library'
export MYSQL_PASSWORD='<database-password>'
export JWT_SECRET='<at-least-32-random-bytes>'
mvn spring-boot:run
```

## 測試

```bash
mvn test
```

目前單元測試涵蓋密碼雜湊、登入成功與失敗、登入限流與帳號暫時鎖定、JWT、分頁邊界、書籍不存在及 MySQL procedure error translation。資料庫交易與 HTTP 流程需使用 MySQL 8.4 驗證，不能以 H2 取代 Stored Procedures。
