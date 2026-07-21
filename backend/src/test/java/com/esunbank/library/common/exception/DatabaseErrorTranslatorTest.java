package com.esunbank.library.common.exception;

import java.sql.SQLException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.UncategorizedSQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseErrorTranslatorTest {

    private final DatabaseErrorTranslator translator = new DatabaseErrorTranslator();

    @ParameterizedTest
    @CsvSource({
            "30004, PHONE_NUMBER_ALREADY_EXISTS",
            "30202, INVENTORY_NOT_FOUND",
            "30203, INVENTORY_NOT_AVAILABLE",
            "30204, BORROWING_NOT_FOUND",
            "30205, BORROWING_FORBIDDEN",
            "30206, BORROWING_ALREADY_RETURNED",
            "30207, INVENTORY_STATUS_INCONSISTENT",
            "99999, INTERNAL_ERROR"
    })
    void translatesMysqlProcedureErrors(int mysqlErrorCode, ErrorCode expected) {
        SQLException sqlException = new SQLException("procedure error", "45000", mysqlErrorCode);
        var dataAccessException = new UncategorizedSQLException("CALL procedure", "CALL", sqlException);

        ApiException result = translator.translate(dataAccessException);

        assertThat(result.errorCode()).isEqualTo(expected);
    }
}
