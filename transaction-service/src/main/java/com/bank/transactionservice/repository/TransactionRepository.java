package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findBySourceAccountNumber(String accountNumber);

    List<Transaction> findByDestinationAccountNumber(String accountNumber);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
