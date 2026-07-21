package com.esunbank.library.business.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;
import com.esunbank.library.data.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookServiceTest {

    @Test
    void missingIsbnBecomesBookNotFound() {
        BookRepository repository = mock(BookRepository.class);
        when(repository.findByIsbn("9780000000000")).thenReturn(Optional.empty());
        BookService service = new BookService(repository);

        assertThatThrownBy(() -> service.findByIsbn("9780000000000"))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND));
    }
}
