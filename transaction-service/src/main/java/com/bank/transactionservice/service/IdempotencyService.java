package com.bank.transactionservice.service;

import com.bank.transactionservice.dto.TransactionEvent;
import com.bank.transactionservice.entity.IdempotencyKey;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.entity.TransactionSaga;
import com.bank.transactionservice.exception.IdempotencyException;
import com.bank.transactionservice.exception.InsufficientFundsException;
import com.bank.transactionservice.exception.TransactionException;
import com.bank.transactionservice.repository.IdempotencyKeyRepository;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.repository.TransactionSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public Optional<IdempotencyKey> checkAndSaveIdempotencyKey(String key, String requestBody) {
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByKeyValue(key);

        if (existing.isPresent()) {
            IdempotencyKey idempotencyKey = existing.get();
            if (idempotencyKey.getExpiresAt().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(idempotencyKey);
                return Optional.empty();
            }
            String requestHash = hashString(requestBody);
            if (!idempotencyKey.getRequestHash().equals(requestHash)) {
                throw new IdempotencyException("Idempotency key already used with different request");
            }
            return existing;
        }

        IdempotencyKey newKey = IdempotencyKey.builder()
                .keyValue(key)
                .requestHash(hashString(requestBody))
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();

        return Optional.of(idempotencyKeyRepository.save(newKey));
    }

    public void updateIdempotencyResponse(String key, String responseBody, int statusCode) {
        idempotencyKeyRepository.findByKeyValue(key).ifPresent(existing -> {
            existing.setResponseBody(responseBody);
            existing.setStatusCode(statusCode);
            idempotencyKeyRepository.save(existing);
        });
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new TransactionException("Failed to hash string", e);
        }
    }

    public void cleanupExpiredKeys() {
        idempotencyKeyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired idempotency keys");
    }
}
