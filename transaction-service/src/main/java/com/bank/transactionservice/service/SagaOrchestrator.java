package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountClient;
import com.bank.transactionservice.dto.TransactionEvent;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.entity.TransactionSaga;
import com.bank.transactionservice.exception.InsufficientFundsException;
import com.bank.transactionservice.exception.TransactionException;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.repository.TransactionSagaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final TransactionRepository transactionRepository;
    private final TransactionSagaRepository sagaRepository;
    private final AccountClient accountClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "transferFallback")
    public Transaction executeTransfer(Transaction transaction) {
        String sagaId = "SAGA-" + UUID.randomUUID();

        TransactionSaga saga = TransactionSaga.builder()
                .sagaId(sagaId)
                .transactionId(transaction.getTransactionId())
                .status(TransactionSaga.SagaStatus.STARTED)
                .currentStep(0)
                .createdAt(LocalDateTime.now())
                .build();

        sagaRepository.save(saga);

        try {
            // Step 1: Debit source account
            log.info("Executing debit for transaction: {}", transaction.getTransactionId());
            accountClient.updateBalance(
                    getAccountId(transaction.getSourceAccountNumber()),
                    transaction.getAmount(),
                    "DEBIT",
                    "Transfer debit: " + transaction.getDescription()
            );

            saga.setStatus(TransactionSaga.SagaStatus.DEBIT_COMPLETED);
            saga.setCurrentStep(1);
            saga.setUpdatedAt(LocalDateTime.now());
            sagaRepository.save(saga);

            // Step 2: Credit destination account
            log.info("Executing credit for transaction: {}", transaction.getTransactionId());
            accountClient.updateBalance(
                    getAccountId(transaction.getDestinationAccountNumber()),
                    transaction.getAmount(),
                    "CREDIT",
                    "Transfer credit: " + transaction.getDescription()
            );

            saga.setStatus(TransactionSaga.SagaStatus.CREDIT_COMPLETED);
            saga.setCurrentStep(2);
            saga.setUpdatedAt(LocalDateTime.now());
            sagaRepository.save(saga);

            // Complete transaction
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);

            saga.setStatus(TransactionSaga.SagaStatus.COMPLETED);
            saga.setUpdatedAt(LocalDateTime.now());
            sagaRepository.save(saga);

            // Publish event
            publishTransactionEvent(savedTransaction);

            log.info("Transfer completed successfully: {}", transaction.getTransactionId());
            return savedTransaction;

        } catch (Exception e) {
            log.error("Transfer failed, initiating compensation: {}", e.getMessage());
            compensate(saga, transaction);
            throw new TransactionException("Transfer failed: " + e.getMessage(), e);
        }
    }

    private void compensate(TransactionSaga saga, Transaction transaction) {
        saga.setStatus(TransactionSaga.SagaStatus.COMPENSATING);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepository.save(saga);

        try {
            if (saga.getCurrentStep() >= 2) {
                // Reverse credit
                accountClient.updateBalance(
                        getAccountId(transaction.getDestinationAccountNumber()),
                        transaction.getAmount(),
                        "DEBIT",
                        "Compensation: Reversing credit"
                );
            }

            if (saga.getCurrentStep() >= 1) {
                // Reverse debit
                accountClient.updateBalance(
                        getAccountId(transaction.getSourceAccountNumber()),
                        transaction.getAmount(),
                        "CREDIT",
                        "Compensation: Reversing debit"
                );
            }

            saga.setStatus(TransactionSaga.SagaStatus.COMPENSATED);
            transaction.setStatus(Transaction.TransactionStatus.ROLLBACK);
            transaction.setFailureReason("Compensated: " + saga.getSagaId());

        } catch (Exception e) {
            log.error("Compensation failed: {}", e.getMessage());
            saga.setStatus(TransactionSaga.SagaStatus.FAILED);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason("Compensation failed: " + e.getMessage());
        } finally {
            saga.setUpdatedAt(LocalDateTime.now());
            sagaRepository.save(saga);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
    }

    private void publishTransactionEvent(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.fromTransaction(transaction);
            rabbitTemplate.convertAndSend("transaction.exchange", "transaction.routingkey", event);
            log.info("Published transaction event: {}", event.transactionId());
        } catch (Exception e) {
            log.error("Failed to publish transaction event: {}", e.getMessage());
        }
    }

    private Long getAccountId(String accountNumber) {
        try {
            var account = accountClient.getAccountByNumber(accountNumber);
            return account.id();
        } catch (Exception e) {
            throw new TransactionException("Account not found: " + accountNumber, e);
        }
    }

    public Transaction transferFallback(Transaction transaction, Throwable t) {
        log.error("Circuit breaker triggered for transfer: {}", t.getMessage());
        throw new TransactionException("Service temporarily unavailable", t);
    }
}
