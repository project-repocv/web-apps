package com.bank.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private String transactionId;
    private String accountId;
    private String customerId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime timestamp;
    private String status;
}
