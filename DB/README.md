# Database scripts

## DDL 與 Stored Procedure 是什麼

DDL（Data Definition Language）負責定義資料庫的結構。本專案的 DDL 建立 `users`、`books`、`inventory`、`borrowing_records`，並設定主鍵、外鍵、檢查條件、索引與「同一冊只能有一筆未歸還紀錄」的唯一限制。

Stored Procedure（預存程序）是保存在 MySQL 內、可由 Spring Boot 以參數呼叫的固定 SQL 流程。查詢程序統一資料存取格式；借書和還書程序則將鎖定、檢查與多表更新包在同一個 transaction 中。

```text
POST /borrowings
  → Spring service
  → CALL sp_borrow_create(userId, inventoryId)
  → 鎖定 inventory
  → 更新 inventory + 新增 borrowing_records
  → COMMIT 或 ROLLBACK
```

## 初始化順序

MySQL 初始化順序：

1. `ddl/*.sql`：資料表、約束、index
2. `procedures/*.sql`：Stored Procedures
3. `dml/*.sql`：書籍、館藏與測試帳號等初始資料
4. `999_limit_app_privileges.sh`：撤銷 runtime user 的資料表權限，只保留 Stored Procedure `EXECUTE`

檔名使用三位數排序，例如 `ddl/001_create_tables.sql`。腳本需可重跑；破壞性重建腳本必須獨立並清楚標示，不放入預設初始化流程。

根目錄的 `000_init.sql` 由 MySQL Docker image 在全新 volume 初始化時載入，依上述順序引入各資料夾內的 SQL。`999_limit_app_privileges.sh` 則由 image 在 SQL 初始化完成後自動執行。新增 SQL 初始化腳本時，也要將它加入 `000_init.sql`。

## 本機執行

首次啟動：

```bash
cp .env.example .env
docker compose up -d db
```

runtime user 只允許呼叫 Stored Procedure，不允許直接讀寫資料表。確認權限：

```bash
docker compose exec db mysql -u library_app -p -e \
  "CALL library.sp_book_list('', 0, 1)"
```

Docker 的初始化腳本只會在資料 volume 為空時執行。若 SQL 已修改，開發者應建立新的測試 volume 重新驗證；不要在含有重要資料的環境直接刪除 volume。
