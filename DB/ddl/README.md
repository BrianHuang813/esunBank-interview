# DDL

資料表、foreign key、check constraint 與 index 腳本放在此處。

- `001_create_tables.sql`：建立使用者、書目、實體館藏與借閱紀錄四張表。
- `active_inventory_id` 是 generated column；搭配 unique constraint，從資料庫層保證每冊館藏最多只有一筆未歸還紀錄。
- 所有表使用 InnoDB，才能支援 transaction、foreign key 與 row-level lock。
