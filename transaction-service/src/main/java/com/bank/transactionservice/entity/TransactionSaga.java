package com.bank.transactionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_sagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", unique = true, nullable = false, length = 50)
    private String sagaId;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep;

    @Column(name = "compensation_data", columnDefinition = "TEXT")
    private String compensationData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SagaStatus {
        STARTED, DEBIT_COMPLETED, CREDIT_COMPLETED, COMPLETED, COMPENSATING, COMPENSATED, FAILED
    }
}
