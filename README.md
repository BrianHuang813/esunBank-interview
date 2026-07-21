# 玉山銀行 Java 實作題 B：線上圖書借閱系統

本專案採用前後端分離的三層式架構，目標是完成使用者註冊／登入、館藏查詢、借書、還書與借閱紀錄查詢。

## 技術基線

- Frontend：Vue 3、Vite、Vue Router、Pinia、Axios
- Backend：Java 21、Spring Boot 3、Spring Security、Maven
- Database：MySQL 8.4、Stored Procedure、Transaction
- Authentication：JWT、BCrypt password hash
- Local runtime：Docker Compose

## 專案結構

```text
.
├── frontend/                 Vue 使用者介面
├── backend/                  Spring Boot REST API
├── DB/
│   ├── ddl/                  資料表、索引、約束
│   ├── dml/                  初始與測試資料
│   └── procedures/           Stored Procedures
├── docs/
│   ├── architecture.md       系統與後端分層設計
│   ├── api-contract.md       REST API 契約
│   ├── data-model.md         資料模型、狀態與交易規則
│   └── implementation-plan.md 前後端施工順序與驗收標準
├── compose.yaml              本機 MySQL 服務
└── .env.example              環境變數範本
```

## 開始開發

1. 複製 `.env.example` 為 `.env`，設定本機密碼與 JWT secret。
2. 以 `docker compose up -d db` 啟動 MySQL。
3. 依 [implementation-plan.md](docs/implementation-plan.md) 先完成資料庫，再分別施工後端與前端。

本機需求：Java 21、Maven 3.9+、Node.js 22+、Docker Desktop。

## 文件入口

- [架構設計](docs/architecture.md)
- [API 契約](docs/api-contract.md)
- [資料模型](docs/data-model.md)
- [實作計畫](docs/implementation-plan.md)
