package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountClient;
import com.bank.transactionservice.dto.DepositRequest;
import com.bank.transactionservice.dto.TransferRequest;
import com.bank.transactionservice.dto.WithdrawalRequest;
import com.bank.transactionservice.dto.TransactionResponse;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.exception.InsufficientFundsException;
import com.bank.transactionservice.exception.TransactionException;
import com.bank.transactionservice.exception.TransactionNotFoundException;
import com.bank.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final SagaOrchestrator sagaOrchestrator;
    private final AccountClient accountClient;

    @Transactional
    public TransactionResponse processTransfer(TransferRequest request) {
        String requestBody = request.toString();

        var existingKey = idempotencyService.checkAndSaveIdempotencyKey(request.idempotencyKey(), requestBody);
        if (existingKey.isPresent() && existingKey.get().getResponseBody() != null) {
            log.info("Returning cached response for idempotency key: {}", request.idempotencyKey());
            return parseCachedResponse(existingKey.get().getResponseBody());
        }

        if (!accountClient.accountExists(request.sourceAccountNumber())) {
            throw new TransactionException("Source account does not exist: " + request.sourceAccountNumber());
        }
        if (!accountClient.accountExists(request.destinationAccountNumber())) {
            throw new TransactionException("Destination account does not exist: " + request.destinationAccountNumber());
        }

        String transactionId = "TXN-" + UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .sourceAccountNumber(request.sourceAccountNumber())
                .destinationAccountNumber(request.destinationAccountNumber())
                .amount(request.amount())
                .currency(request.currency())
                .transactionType(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.description())
                .referenceNumber("REF-" + System.currentTimeMillis())
                .idempotencyKey(request.idempotencyKey())
                .build();

        transactionRepository.save(transaction);

        Transaction completedTransaction = sagaOrchestrator.executeTransfer(transaction);
        TransactionResponse response = TransactionResponse.fromEntity(completedTransaction);
        idempotencyService.updateIdempotencyResponse(request.idempotencyKey(), response.toString(), 200);

        return response;
    }

    @Transactional
    public TransactionResponse processDeposit(DepositRequest request) {
        String requestBody = request.toString();

        var existingKey = idempotencyService.checkAndSaveIdempotencyKey(request.idempotencyKey(), requestBody);
        if (existingKey.isPresent() && existingKey.get().getResponseBody() != null) {
            log.info("Returning cached response for idempotency key: {}", request.idempotencyKey());
            return parseCachedResponse(existingKey.get().getResponseBody());
        }

        if (!accountClient.accountExists(request.accountNumber())) {
            throw new TransactionException("Account does not exist: " + request.accountNumber());
        }

        String transactionId = "TXN-" + UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .sourceAccountNumber(request.accountNumber())
                .destinationAccountNumber(request.accountNumber())
                .amount(request.amount())
                .currency(request.currency())
                .transactionType(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.description())
                .referenceNumber("REF-" + System.currentTimeMillis())
                .idempotencyKey(request.idempotencyKey())
                .build();

        transactionRepository.save(transaction);

        try {
            Long accountId = getAccountId(request.accountNumber());
            accountClient.updateBalance(accountId, request.amount(), "CREDIT", "Deposit: " + request.description());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);

            TransactionResponse response = TransactionResponse.fromEntity(savedTransaction);
            idempotencyService.updateIdempotencyResponse(request.idempotencyKey(), response.toString(), 200);

            return response;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw new TransactionException("Deposit failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public TransactionResponse processWithdrawal(WithdrawalRequest request) {
        String requestBody = request.toString();

        var existingKey = idempotencyService.checkAndSaveIdempotencyKey(request.idempotencyKey(), requestBody);
        if (existingKey.isPresent() && existingKey.get().getResponseBody() != null) {
            log.info("Returning cached response for idempotency key: {}", request.idempotencyKey());
            return parseCachedResponse(existingKey.get().getResponseBody());
        }

        if (!accountClient.accountExists(request.accountNumber())) {
            throw new TransactionException("Account does not exist: " + request.accountNumber());
        }

        String transactionId = "TXN-" + UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .sourceAccountNumber(request.accountNumber())
                .destinationAccountNumber(request.accountNumber())
                .amount(request.amount())
                .currency(request.currency())
                .transactionType(Transaction.TransactionType.WITHDRAWAL)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.description())
                .referenceNumber("REF-" + System.currentTimeMillis())
                .idempotencyKey(request.idempotencyKey())
                .build();

        transactionRepository.save(transaction);

        try {
            Long accountId = getAccountId(request.accountNumber());
            accountClient.updateBalance(accountId, request.amount(), "DEBIT", "Withdrawal: " + request.description());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);

            TransactionResponse response = TransactionResponse.fromEntity(savedTransaction);
            idempotencyService.updateIdempotencyResponse(request.idempotencyKey(), response.toString(), 200);

            return response;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw new TransactionException("Withdrawal failed: " + e.getMessage(), e);
        }
    }

    public Optional<TransactionResponse> getTransactionById(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(TransactionResponse::fromEntity);
    }

    public List<TransactionResponse> getTransactionsByAccount(String accountNumber) {
        return transactionRepository.findBySourceAccountNumber(accountNumber).stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    private Long getAccountId(String accountNumber) {
        var account = accountClient.getAccountByNumber(accountNumber);
        return account.id();
    }

    private TransactionResponse parseCachedResponse(String responseBody) {
        // In production, use proper JSON serialization
        throw new TransactionException("Cached response parsing not implemented");
    }
}
