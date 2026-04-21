package com.bank.accountservice.controller;

import com.bank.accountservice.dto.AccountRequest;
import com.bank.accountservice.dto.AccountResponse;
import com.bank.accountservice.dto.BalanceUpdateRequest;
import com.bank.accountservice.dto.UpdateAccountStatusRequest;
import com.bank.accountservice.entity.AuditLog;
import com.bank.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable UUID customerId) {
        List<AccountResponse> responses = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountResponse response = accountService.updateAccountStatus(id, request.status());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> creditAccount(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestHeader(value = "X-Performed-By", defaultValue = "SYSTEM") String performedBy) {
        AccountResponse response = accountService.creditAccount(accountNumber, amount, 
                description != null ? description : "Credit", performedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestHeader(value = "X-Performed-By", defaultValue = "SYSTEM") String performedBy) {
        AccountResponse response = accountService.debitAccount(accountNumber, amount, 
                description != null ? description : "Debit", performedBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@PathVariable Long id) {
        List<AuditLog> logs = accountService.getAuditLogs(id);
        return ResponseEntity.ok(logs);
    }
}
