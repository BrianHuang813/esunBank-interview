# Stored Procedures

所有應用程式資料存取程序放在此處。借書與還書程序需自行管理 transaction、row lock、commit 與 rollback。

| 檔案 | Procedures |
|---|---|
| `001_user_procedures.sql` | `sp_user_create`、`sp_user_find_by_phone`、`sp_user_record_login` |
| `002_book_procedures.sql` | `sp_book_list`、`sp_book_get` |
| `003_borrowing_procedures.sql` | `sp_borrow_create`、`sp_borrow_return`、`sp_borrow_list_by_user` |

程序使用固定 SQL 與輸入參數，不使用 dynamic SQL。Spring repository 應透過 JDBC `CALL` 綁定參數，不能將使用者輸入拼接進 SQL。
