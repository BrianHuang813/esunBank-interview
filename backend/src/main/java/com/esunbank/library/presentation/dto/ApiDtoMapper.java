package com.esunbank.library.presentation.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.esunbank.library.business.model.AuthResult;
import com.esunbank.library.business.model.BookDetail;
import com.esunbank.library.business.model.BookSummary;
import com.esunbank.library.business.model.BorrowingRecord;
import com.esunbank.library.business.model.PageResult;
import com.esunbank.library.business.model.User;

@Component
public class ApiDtoMapper {

    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");

    public AuthDtos.UserResponse toUserResponse(User user) {
        return new AuthDtos.UserResponse(
                user.id(),
                user.phoneNumber(),
                user.userName(),
                toOffsetDateTime(user.registrationTime()),
                toOffsetDateTime(user.lastLoginTime())
        );
    }

    public AuthDtos.LoginResponse toLoginResponse(AuthResult result) {
        return new AuthDtos.LoginResponse(
                result.accessToken(),
                "Bearer",
                result.expiresIn(),
                new AuthDtos.LoginUserResponse(result.user().id(), result.user().userName())
        );
    }

    public BookDtos.SummaryResponse toBookSummary(BookSummary book) {
        return new BookDtos.SummaryResponse(
                book.isbn(),
                book.name(),
                book.author(),
                book.introduction(),
                book.inventoryCount(),
                book.availableCount()
        );
    }

    public BookDtos.DetailResponse toBookDetail(BookDetail book) {
        return new BookDtos.DetailResponse(
                book.isbn(),
                book.name(),
                book.author(),
                book.introduction(),
                book.inventoryCount(),
                book.availableCount(),
                book.inventory().stream()
                        .map(item -> new BookDtos.InventoryResponse(
                                item.inventoryId(),
                                item.status(),
                                toOffsetDateTime(item.storeTime())
                        ))
                        .toList()
        );
    }

    public BorrowingDtos.Response toBorrowingResponse(BorrowingRecord borrowing) {
        return new BorrowingDtos.Response(
                borrowing.id(),
                borrowing.inventoryId(),
                borrowing.isbn(),
                borrowing.bookName(),
                borrowing.author(),
                borrowing.inventoryStatus(),
                toOffsetDateTime(borrowing.borrowingTime()),
                toOffsetDateTime(borrowing.returnTime())
        );
    }

    public <T, R> PageResponse<R> toPage(
            PageResult<T> page,
            java.util.function.Function<T, R> mapper
    ) {
        return new PageResponse<>(
                page.content().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(TAIPEI).toOffsetDateTime();
    }
}
