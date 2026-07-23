package com.esunbank.library.presentation.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esunbank.library.business.service.BookService;
import com.esunbank.library.common.response.ApiResponse;
import com.esunbank.library.common.validation.PaginationConstraints;
import com.esunbank.library.presentation.dto.ApiDtoMapper;
import com.esunbank.library.presentation.dto.BookDtos;
import com.esunbank.library.presentation.dto.PageResponse;

@Validated
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;
    private final ApiDtoMapper mapper;

    public BookController(BookService bookService, ApiDtoMapper mapper) {
        this.bookService = bookService;
        this.mapper = mapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<BookDtos.SummaryResponse>> findAll(
            @RequestParam(defaultValue = "")
            @Size(max = 255, message = "搜尋文字最多 255 個字元")
            String keyword,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page 不可小於 0")
            @Max(value = PaginationConstraints.MAX_PAGE, message = "page 不可大於 1000000")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size 不可小於 1")
            @Max(value = 100, message = "size 不可大於 100")
            int size
    ) {
        var result = bookService.findAll(keyword, page, size);
        return ApiResponse.of(mapper.toPage(result, mapper::toBookSummary));
    }

    @GetMapping("/{isbn}")
    public ApiResponse<BookDtos.DetailResponse> findByIsbn(
            @PathVariable
            @Pattern(regexp = "^([0-9]{10}|[0-9]{13})$", message = "ISBN 格式不正確")
            String isbn
    ) {
        return ApiResponse.of(mapper.toBookDetail(bookService.findByIsbn(isbn)));
    }
}
