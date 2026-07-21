package com.esunbank.library.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "請檢查輸入資料"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "手機號碼或密碼錯誤"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "沒有權限執行此操作"),
    BORROWING_FORBIDDEN(HttpStatus.FORBIDDEN, "不能操作其他使用者的借閱紀錄"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "使用者不存在"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "找不到指定書籍"),
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "找不到指定館藏"),
    BORROWING_NOT_FOUND(HttpStatus.NOT_FOUND, "找不到指定借閱紀錄"),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "此手機號碼已註冊"),
    INVENTORY_NOT_AVAILABLE(HttpStatus.CONFLICT, "此館藏目前無法借閱"),
    BORROWING_ALREADY_RETURNED(HttpStatus.CONFLICT, "此借閱紀錄已歸還"),
    INVENTORY_STATUS_INCONSISTENT(HttpStatus.CONFLICT, "館藏狀態不一致，請聯絡管理員"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "系統暫時無法處理請求");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
