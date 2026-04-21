package com.bank.transactionservice.controller;

import com.bank.transactionservice.dto.DepositRequest;
import com.bank.transactionservice.dto.TransferRequest;
import com.bank.transactionservice.dto.WithdrawalRequest;
import com.bank.transactionservice.dto.TransactionResponse;
import com.bank.transactionservice.service.IdempotencyService;
import com.bank.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.processTransfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.processDeposit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<TransactionResponse> withdrawal(@Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse response = transactionService.processWithdrawal(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return transactionService.getTransactionById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(
            @PathVariable String accountNumber) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccount(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/cleanup/idempotency")
    public ResponseEntity<String> cleanupIdempotencyKeys() {
        idempotencyService.cleanupExpiredKeys();
        return ResponseEntity.ok("Idempotency keys cleaned up");
    }
}
