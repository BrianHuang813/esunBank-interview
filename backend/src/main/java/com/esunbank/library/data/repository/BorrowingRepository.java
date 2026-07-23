package com.esunbank.library.data.repository;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.esunbank.library.business.model.BorrowingRecord;
import com.esunbank.library.business.model.PageResult;
import com.esunbank.library.common.exception.DatabaseErrorTranslator;
import com.esunbank.library.data.mapper.ResultSetMappers;

@Repository
public class BorrowingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseErrorTranslator errorTranslator;

    public BorrowingRepository(JdbcTemplate jdbcTemplate, DatabaseErrorTranslator errorTranslator) {
        this.jdbcTemplate = jdbcTemplate;
        this.errorTranslator = errorTranslator;
    }

    public BorrowingRecord borrow(long userId, long inventoryId) {
        try {
            return jdbcTemplate.queryForObject(
                    "CALL sp_borrow_create(?, ?)",
                    (resultSet, rowNumber) -> ResultSetMappers.mapBorrowingRecord(resultSet),
                    userId,
                    inventoryId
            );
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    public BorrowingRecord returnBorrowing(long userId, long borrowingId) {
        try {
            return jdbcTemplate.queryForObject(
                    "CALL sp_borrow_return(?, ?)",
                    (resultSet, rowNumber) -> ResultSetMappers.mapBorrowingRecord(resultSet),
                    userId,
                    borrowingId
            );
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    public PageResult<BorrowingRecord> findByUser(
            long userId,
            String status,
            int page,
            int size
    ) {
        try {
            List<BorrowingRow> rows = jdbcTemplate.query(
                    "CALL sp_borrow_list_by_user(?, ?, ?, ?)",
                    (resultSet, rowNumber) -> new BorrowingRow(
                            ResultSetMappers.mapBorrowingRecord(resultSet),
                            resultSet.getLong("total_items")
                    ),
                    userId,
                    status,
                    (long) page * size,
                    size
            );
            long total = rows.isEmpty() ? 0 : rows.getFirst().totalItems();
            return new PageResult<>(rows.stream().map(BorrowingRow::borrowing).toList(), page, size, total);
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    private record BorrowingRow(BorrowingRecord borrowing, long totalItems) {
    }
}
