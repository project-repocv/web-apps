package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    Optional<IdempotencyKey> findByKeyValue(String keyValue);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
