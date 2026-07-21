package com.esunbank.library.data.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.esunbank.library.business.model.BookDetail;
import com.esunbank.library.business.model.BookSummary;
import com.esunbank.library.business.model.InventoryItem;
import com.esunbank.library.business.model.PageResult;
import com.esunbank.library.common.exception.DatabaseErrorTranslator;
import com.esunbank.library.data.mapper.ResultSetMappers;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseErrorTranslator errorTranslator;

    public BookRepository(JdbcTemplate jdbcTemplate, DatabaseErrorTranslator errorTranslator) {
        this.jdbcTemplate = jdbcTemplate;
        this.errorTranslator = errorTranslator;
    }

    public PageResult<BookSummary> findAll(String keyword, int page, int size) {
        try {
            List<BookRow> rows = jdbcTemplate.query(
                    "CALL sp_book_list(?, ?, ?)",
                    (resultSet, rowNumber) -> new BookRow(
                            ResultSetMappers.mapBookSummary(resultSet),
                            resultSet.getLong("total_items")
                    ),
                    keyword,
                    page * size,
                    size
            );
            long total = rows.isEmpty() ? 0 : rows.getFirst().totalItems();
            return new PageResult<>(rows.stream().map(BookRow::book).toList(), page, size, total);
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    public Optional<BookDetail> findByIsbn(String isbn) {
        try {
            return jdbcTemplate.execute((ConnectionCallback<Optional<BookDetail>>) connection -> {
                try (CallableStatement statement = connection.prepareCall("{CALL sp_book_get(?)}")) {
                    statement.setString(1, isbn);
                    if (!statement.execute()) {
                        return Optional.empty();
                    }

                    BookSummary book;
                    try (ResultSet resultSet = statement.getResultSet()) {
                        if (!resultSet.next()) {
                            return Optional.empty();
                        }
                        book = ResultSetMappers.mapBookSummary(resultSet);
                    }

                    List<InventoryItem> inventory = new ArrayList<>();
                    if (statement.getMoreResults()) {
                        try (ResultSet resultSet = statement.getResultSet()) {
                            while (resultSet.next()) {
                                inventory.add(ResultSetMappers.mapInventoryItem(resultSet));
                            }
                        }
                    }

                    return Optional.of(new BookDetail(
                            book.isbn(),
                            book.name(),
                            book.author(),
                            book.introduction(),
                            book.inventoryCount(),
                            book.availableCount(),
                            List.copyOf(inventory)
                    ));
                }
            });
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    private record BookRow(BookSummary book, long totalItems) {
    }
}
