package com.esunbank.library.business.service;

import org.springframework.stereotype.Service;

import com.esunbank.library.business.model.BookDetail;
import com.esunbank.library.business.model.BookSummary;
import com.esunbank.library.business.model.PageResult;
import com.esunbank.library.common.exception.ApiException;
import com.esunbank.library.common.exception.ErrorCode;
import com.esunbank.library.data.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public PageResult<BookSummary> findAll(String keyword, int page, int size) {
        return bookRepository.findAll(keyword == null ? "" : keyword.trim(), page, size);
    }

    public BookDetail findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }
}
