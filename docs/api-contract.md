# REST API 契約

Base URL：`/api/v1`。時間一律為 ISO-8601 且帶時區，例如 `2026-07-21T15:30:00+08:00`。

## 通用格式

成功：

```json
{
  "data": {},
  "timestamp": "2026-07-21T15:30:00+08:00"
}
```

失敗：

```json
{
  "code": "INVENTORY_NOT_AVAILABLE",
  "message": "此館藏目前無法借閱",
  "timestamp": "2026-07-21T15:30:00+08:00",
  "path": "/api/v1/borrowings"
}
```

## Authentication

### POST /auth/register

Request：

```json
{
  "phoneNumber": "0912345678",
  "password": "Interview123!",
  "userName": "王小明"
}
```

Response：`201 Created`，回傳 `id`、`phoneNumber`、`userName`、`registrationTime`，不回傳 password hash。

### POST /auth/login

Request：

```json
{
  "phoneNumber": "0912345678",
  "password": "Interview123!"
}
```

Response：`200 OK`

```json
{
  "data": {
    "accessToken": "<jwt>",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "userName": "王小明"
    }
  },
  "timestamp": "2026-07-21T15:30:00+08:00"
}
```

## Books

### GET /books?page=0&size=20&keyword=Java

公開端點。回傳書目、總庫存數與目前可借數，不為每一冊重複書目資料。

### GET /books/{isbn}

公開端點。回傳書目資訊及 inventory 清單，每冊包含 `inventoryId`、`status`、`storeTime`。

## Borrowings

以下端點皆需 Bearer JWT。

### POST /borrowings

```json
{
  "inventoryId": 101
}
```

成功回傳 `201 Created` 與 borrowing record。館藏不存在回傳 404；非 `AVAILABLE` 回傳 409。

### POST /borrowings/{borrowingId}/return

不需要 request body。成功回傳更新後紀錄。非本人紀錄回傳 403；已歸還回傳 409。

### GET /borrowings/me?status=ACTIVE&page=0&size=20

`status` 可為 `ACTIVE`、`RETURNED` 或 `ALL`，預設 `ALL`。每筆包含書名、ISBN、inventory ID、借出與歸還時間。

## 錯誤代碼

| HTTP | code | 情境 |
|---|---|---|
| 400 | VALIDATION_FAILED | request 欄位不合法 |
| 401 | AUTHENTICATION_FAILED | 登入資料錯誤或 JWT 無效 |
| 403 | BORROWING_FORBIDDEN | 操作不屬於自己的借閱紀錄 |
| 404 | BOOK_NOT_FOUND | 書目不存在 |
| 404 | INVENTORY_NOT_FOUND | 館藏不存在 |
| 404 | BORROWING_NOT_FOUND | 借閱紀錄不存在 |
| 409 | PHONE_NUMBER_ALREADY_EXISTS | 手機已註冊 |
| 409 | INVENTORY_NOT_AVAILABLE | 館藏無法借閱或併發時已被借走 |
| 409 | BORROWING_ALREADY_RETURNED | 借閱已歸還 |
| 500 | INTERNAL_ERROR | 未預期錯誤，回應不得包含 stack trace |
