package com.bank.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionEvent(

        String transactionId,

        String sourceAccountNumber,

        String destinationAccountNumber,

        BigDecimal amount,

        String currency,

        String transactionType,

        String status,

        String description,

        LocalDateTime timestamp
) {

    public static TransactionEvent fromTransaction(com.bank.transactionservice.entity.Transaction transaction) {
        return new TransactionEvent(
                transaction.getTransactionId(),
                transaction.getSourceAccountNumber(),
                transaction.getDestinationAccountNumber(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType().name(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                LocalDateTime.now()
        );
    }
}
