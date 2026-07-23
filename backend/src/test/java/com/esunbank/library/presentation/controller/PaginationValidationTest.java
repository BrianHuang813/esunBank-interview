package com.esunbank.library.presentation.controller;

import java.lang.reflect.Method;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.esunbank.library.business.service.BookService;
import com.esunbank.library.business.service.BorrowingService;
import com.esunbank.library.common.validation.PaginationConstraints;
import com.esunbank.library.presentation.dto.ApiDtoMapper;
import com.esunbank.library.security.AuthenticatedUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PaginationValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void bookListRejectsPageAboveSupportedMaximum() throws Exception {
        BookController controller = new BookController(
                mock(BookService.class),
                mock(ApiDtoMapper.class)
        );
        Method method = BookController.class.getMethod(
                "findAll",
                String.class,
                int.class,
                int.class
        );

        var violations = validator.forExecutables().validateParameters(
                controller,
                method,
                new Object[]{"", PaginationConstraints.MAX_PAGE + 1, 20}
        );

        assertThat(violations).singleElement()
                .satisfies(violation ->
                        assertThat(violation.getPropertyPath().toString()).endsWith(".page"));
    }

    @Test
    void borrowingListRejectsPageAboveSupportedMaximum() throws Exception {
        BorrowingController controller = new BorrowingController(
                mock(BorrowingService.class),
                mock(ApiDtoMapper.class)
        );
        Method method = BorrowingController.class.getMethod(
                "findMine",
                AuthenticatedUser.class,
                String.class,
                int.class,
                int.class
        );

        var violations = validator.forExecutables().validateParameters(
                controller,
                method,
                new Object[]{new AuthenticatedUser(1L), "ALL", PaginationConstraints.MAX_PAGE + 1, 20}
        );

        assertThat(violations).singleElement()
                .satisfies(violation ->
                        assertThat(violation.getPropertyPath().toString()).endsWith(".page"));
    }
}
