# 資料模型

## 關係

```text
users 1 ────── * borrowing_records * ────── 1 inventory * ────── 1 books
```

`books` 是書目；`inventory` 是實體館藏副本。同 ISBN 可以有多冊 inventory，借還書必須以 `inventory_id` 操作。

## 資料表

### users

| 欄位 | 型別方向 | 規則 |
|---|---|---|
| id | BIGINT | PK，自動編號 |
| phone_number | VARCHAR(20) | NOT NULL、UNIQUE、正規化後保存 |
| password_hash | VARCHAR(100) | NOT NULL，只存 BCrypt hash |
| user_name | VARCHAR(30) | NOT NULL，長度 1 至 30，僅接受中文、英文字母與空格 |
| registration_time | DATETIME(6) | NOT NULL |
| last_login_time | DATETIME(6) | 可為 NULL |

### books

| 欄位 | 型別方向 | 規則 |
|---|---|---|
| isbn | VARCHAR(13) | PK，保存移除連字號後的 ISBN |
| name | VARCHAR(255) | NOT NULL |
| author | VARCHAR(255) | NOT NULL |
| introduction | TEXT | 可為 NULL |

### inventory

| 欄位 | 型別方向 | 規則 |
|---|---|---|
| id | BIGINT | PK，自動編號 |
| isbn | VARCHAR(13) | FK → books.isbn |
| store_time | DATETIME(6) | NOT NULL |
| status | VARCHAR(20) | NOT NULL、CHECK 合法狀態 |

### borrowing_records

| 欄位 | 型別方向 | 規則 |
|---|---|---|
| id | BIGINT | PK，自動編號 |
| user_id | BIGINT | FK → users.id、indexed |
| inventory_id | BIGINT | FK → inventory.id、indexed |
| borrowing_time | DATETIME(6) | NOT NULL |
| return_time | DATETIME(6) | NULL 表示尚未歸還 |
| active_inventory_id | BIGINT generated | 未歸還時等於 inventory_id，否則 NULL |

`UNIQUE(active_inventory_id)` 利用 MySQL unique index 允許多個 NULL 的特性，保證同一冊館藏最多只有一筆 open borrowing。

## 庫存狀態

| 儲存值 | 畫面文案 | 能否借閱 |
|---|---|---|
| AVAILABLE | 在庫／可借閱 | 是 |
| BORROWED | 出借中／已借閱 | 否 |
| PROCESSING | 整理中 | 否 |
| LOST | 遺失 | 否 |
| DAMAGED | 損毀 | 否 |
| DISCARDED | 廢棄 | 否 |

合法轉移：

```text
AVAILABLE ──借書──▶ BORROWED ──還書──▶ AVAILABLE
    │                   │
    └────館務處理───────┴──▶ PROCESSING / LOST / DAMAGED / DISCARDED
```

本題公開 API 僅處理 `AVAILABLE ↔ BORROWED`，其他狀態先由種子資料與資料庫模型支援，不額外擴張管理功能。

## Stored Procedure 邊界

| Procedure | 讀寫內容 | 是否交易型 |
|---|---|---|
| sp_user_create | 新增 users | 否，單表原子寫入 |
| sp_user_find_by_phone | 查詢登入資料 | 否 |
| sp_user_record_login | 更新 last_login_time | 否 |
| sp_book_list | 書目與可借數量 | 否 |
| sp_book_get | 單一書目與 inventory | 否 |
| sp_borrow_create | 鎖 inventory、更新狀態、新增紀錄 | 是 |
| sp_borrow_return | 鎖紀錄與 inventory、更新兩表 | 是 |
| sp_borrow_list_by_user | 個人借閱歷程 | 否 |
