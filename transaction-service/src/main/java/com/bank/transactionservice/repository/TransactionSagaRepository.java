package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.TransactionSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionSagaRepository extends JpaRepository<TransactionSaga, Long> {

    Optional<TransactionSaga> findBySagaId(String sagaId);

    Optional<TransactionSaga> findByTransactionId(String transactionId);
}
