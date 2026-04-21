package com.bank.transactionservice.service;

import com.bank.transactionservice.dto.TransferRequest;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.exception.IdempotencyException;
import com.bank.transactionservice.repository.IdempotencyKeyRepository;
import com.bank.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(idempotencyKeyRepository);
    }

    @Test
    void checkAndSaveIdempotencyKey_NewKey_ShouldSaveAndReturn() {
        String key = "test-key-123";
        String requestBody = "test-request";

        when(idempotencyKeyRepository.findByKeyValue(key)).thenReturn(Optional.empty());
        when(idempotencyKeyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<com.bank.transactionservice.entity.IdempotencyKey> result =
                idempotencyService.checkAndSaveIdempotencyKey(key, requestBody);

        assertTrue(result.isPresent());
        assertEquals(key, result.get().getKeyValue());
        verify(idempotencyKeyRepository).save(any());
    }

    @Test
    void checkAndSaveIdempotencyKey_ExistingKeySameRequest_ShouldReturnExisting() {
        String key = "test-key-456";
        String requestBody = "test-request";

        com.bank.transactionservice.entity.IdempotencyKey existingKey =
                com.bank.transactionservice.entity.IdempotencyKey.builder()
                        .keyValue(key)
                        .requestHash("hash123")
                        .build();

        when(idempotencyKeyRepository.findByKeyValue(key)).thenReturn(Optional.of(existingKey));

        Optional<com.bank.transactionservice.entity.IdempotencyKey> result =
                idempotencyService.checkAndSaveIdempotencyKey(key, requestBody);

        assertTrue(result.isPresent());
        verify(idempotencyKeyRepository, never()).save(any());
    }
}
