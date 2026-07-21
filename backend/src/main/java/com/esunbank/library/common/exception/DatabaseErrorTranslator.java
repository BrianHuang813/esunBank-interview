package com.esunbank.library.common.exception;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseErrorTranslator {

    public ApiException translate(DataAccessException exception) {
        SQLException sqlException = findSqlException(exception);
        if (sqlException == null) {
            return new ApiException(ErrorCode.INTERNAL_ERROR, exception);
        }

        ErrorCode errorCode = switch (sqlException.getErrorCode()) {
            case 30004 -> ErrorCode.PHONE_NUMBER_ALREADY_EXISTS;
            case 30005, 30201 -> ErrorCode.USER_NOT_FOUND;
            case 30202 -> ErrorCode.INVENTORY_NOT_FOUND;
            case 30203 -> ErrorCode.INVENTORY_NOT_AVAILABLE;
            case 30204 -> ErrorCode.BORROWING_NOT_FOUND;
            case 30205 -> ErrorCode.BORROWING_FORBIDDEN;
            case 30206 -> ErrorCode.BORROWING_ALREADY_RETURNED;
            case 30207 -> ErrorCode.INVENTORY_STATUS_INCONSISTENT;
            default -> ErrorCode.INTERNAL_ERROR;
        };
        return new ApiException(errorCode, exception);
    }

    private SQLException findSqlException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }
}
