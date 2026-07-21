package com.esunbank.library.data.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.esunbank.library.business.model.BookSummary;
import com.esunbank.library.business.model.BorrowingRecord;
import com.esunbank.library.business.model.InventoryItem;
import com.esunbank.library.business.model.User;

public final class ResultSetMappers {

    private ResultSetMappers() {
    }

    public static RowMapper<User> publicUser() {
        return (resultSet, rowNumber) -> mapUser(resultSet, false);
    }

    public static RowMapper<User> authenticatedUser() {
        return (resultSet, rowNumber) -> mapUser(resultSet, true);
    }

    public static User mapUser(ResultSet resultSet, boolean includePasswordHash) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("phone_number"),
                includePasswordHash ? resultSet.getString("password_hash") : null,
                resultSet.getString("user_name"),
                resultSet.getObject("registration_time", LocalDateTime.class),
                resultSet.getObject("last_login_time", LocalDateTime.class)
        );
    }

    public static BookSummary mapBookSummary(ResultSet resultSet) throws SQLException {
        return new BookSummary(
                resultSet.getString("isbn"),
                resultSet.getString("name"),
                resultSet.getString("author"),
                resultSet.getString("introduction"),
                resultSet.getLong("inventory_count"),
                resultSet.getLong("available_count")
        );
    }

    public static InventoryItem mapInventoryItem(ResultSet resultSet) throws SQLException {
        return new InventoryItem(
                resultSet.getLong("inventory_id"),
                resultSet.getString("isbn"),
                resultSet.getObject("store_time", LocalDateTime.class),
                resultSet.getString("status")
        );
    }

    public static BorrowingRecord mapBorrowingRecord(ResultSet resultSet) throws SQLException {
        return new BorrowingRecord(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getLong("inventory_id"),
                resultSet.getObject("borrowing_time", LocalDateTime.class),
                resultSet.getObject("return_time", LocalDateTime.class),
                resultSet.getString("inventory_status"),
                resultSet.getString("isbn"),
                resultSet.getString("book_name"),
                resultSet.getString("author")
        );
    }
}
