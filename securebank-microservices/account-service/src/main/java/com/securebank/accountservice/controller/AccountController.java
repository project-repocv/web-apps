package com.securebank.accountservice.controller;

import com.securebank.accountservice.model.Account;
import com.securebank.accountservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Account> createAccount(
            @PathVariable String userId,
            @RequestParam(defaultValue = "CHECKING") Account.AccountType type) {
        return ResponseEntity.ok(accountService.createAccount(userId, type));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable String userId) {
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<Account> deposit(
            @PathVariable String accountId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(accountService.deposit(accountId, amount));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Account> withdraw(
            @PathVariable String accountId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(accountService.withdraw(accountId, amount));
    }
}
