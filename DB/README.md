# Database scripts

MySQL 初始化順序：

1. `ddl/*.sql`：資料表、約束、index
2. `procedures/*.sql`：Stored Procedures
3. `dml/*.sql`：書籍、館藏與測試帳號等初始資料

檔名使用三位數排序，例如 `ddl/001_create_tables.sql`。腳本需可重跑；破壞性重建腳本必須獨立並清楚標示，不放入預設初始化流程。

根目錄的 `00-init.sh` 由 MySQL Docker image 在全新 volume 初始化時載入，依上述順序執行各資料夾內的 SQL。
