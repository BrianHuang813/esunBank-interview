package com.esunbank.library.business.service;

import org.springframework.stereotype.Service;

import com.esunbank.library.business.model.BorrowingRecord;
import com.esunbank.library.business.model.PageResult;
import com.esunbank.library.data.repository.BorrowingRepository;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;

    public BorrowingService(BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;
    }

    public BorrowingRecord borrow(long userId, long inventoryId) {
        return borrowingRepository.borrow(userId, inventoryId);
    }

    public BorrowingRecord returnBorrowing(long userId, long borrowingId) {
        return borrowingRepository.returnBorrowing(userId, borrowingId);
    }

    public PageResult<BorrowingRecord> findByUser(
            long userId,
            String status,
            int page,
            int size
    ) {
        return borrowingRepository.findByUser(userId, status, page, size);
    }
}
