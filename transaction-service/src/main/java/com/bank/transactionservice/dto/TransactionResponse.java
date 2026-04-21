package com.bank.transactionservice.dto;

import com.bank.transactionservice.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        String transactionId,

        String sourceAccountNumber,

        String destinationAccountNumber,

        BigDecimal amount,

        String currency,

        String transactionType,

        String status,

        String description,

        String referenceNumber,

        LocalDateTime createdAt,

        LocalDateTime completedAt
) {

    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getSourceAccountNumber(),
                transaction.getDestinationAccountNumber(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType().name(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getReferenceNumber(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
