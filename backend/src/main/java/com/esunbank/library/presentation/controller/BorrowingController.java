package com.esunbank.library.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esunbank.library.business.service.BorrowingService;
import com.esunbank.library.common.response.ApiResponse;
import com.esunbank.library.common.validation.PaginationConstraints;
import com.esunbank.library.presentation.dto.ApiDtoMapper;
import com.esunbank.library.presentation.dto.BorrowingDtos;
import com.esunbank.library.presentation.dto.PageResponse;
import com.esunbank.library.security.AuthenticatedUser;

@Validated
@RestController
@RequestMapping("/api/v1/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final ApiDtoMapper mapper;

    public BorrowingController(BorrowingService borrowingService, ApiDtoMapper mapper) {
        this.borrowingService = borrowingService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BorrowingDtos.Response>> borrow(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody BorrowingDtos.BorrowRequest request
    ) {
        var borrowing = borrowingService.borrow(user.userId(), request.inventoryId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(mapper.toBorrowingResponse(borrowing)));
    }

    @PostMapping("/{borrowingId}/return")
    public ApiResponse<BorrowingDtos.Response> returnBorrowing(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable @Positive(message = "borrowingId 必須是正整數") long borrowingId
    ) {
        var borrowing = borrowingService.returnBorrowing(user.userId(), borrowingId);
        return ApiResponse.of(mapper.toBorrowingResponse(borrowing));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<BorrowingDtos.Response>> findMine(
            @AuthenticationPrincipal AuthenticatedUser user,

            @RequestParam(defaultValue = "ALL")
            @Pattern(regexp = "(?i)^(ACTIVE|RETURNED|ALL)$", message = "status 必須是 ACTIVE、RETURNED 或 ALL")
            String status,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page 不可小於 0")
            @Max(value = PaginationConstraints.MAX_PAGE, message = "page 不可大於 1000000")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size 不可小於 1")
            @Max(value = 100, message = "size 不可大於 100")
            int size
    ) {
        var result = borrowingService.findByUser(user.userId(), status.toUpperCase(), page, size);
        return ApiResponse.of(mapper.toPage(result, mapper::toBorrowingResponse));
    }
}
