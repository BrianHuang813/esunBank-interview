package com.esunbank.library.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.esunbank.library.business.model.User;
import com.esunbank.library.common.exception.DatabaseErrorTranslator;
import com.esunbank.library.data.mapper.ResultSetMappers;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseErrorTranslator errorTranslator;

    public UserRepository(JdbcTemplate jdbcTemplate, DatabaseErrorTranslator errorTranslator) {
        this.jdbcTemplate = jdbcTemplate;
        this.errorTranslator = errorTranslator;
    }

    public User create(String phoneNumber, String passwordHash, String userName) {
        try {
            return jdbcTemplate.queryForObject(
                    "CALL sp_user_create(?, ?, ?)",
                    ResultSetMappers.publicUser(),
                    phoneNumber,
                    passwordHash,
                    userName
            );
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        try {
            List<User> users = jdbcTemplate.query(
                    "CALL sp_user_find_by_phone(?)",
                    ResultSetMappers.authenticatedUser(),
                    phoneNumber
            );
            return users.stream().findFirst();
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }

    public User recordLogin(long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "CALL sp_user_record_login(?)",
                    ResultSetMappers.publicUser(),
                    userId
            );
        } catch (DataAccessException exception) {
            throw errorTranslator.translate(exception);
        }
    }
}
