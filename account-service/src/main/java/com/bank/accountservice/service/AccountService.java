package com.bank.accountservice.service;

import com.bank.accountservice.client.CustomerClient;
import com.bank.accountservice.dto.AccountRequest;
import com.bank.accountservice.dto.AccountResponse;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.entity.AuditLog;
import com.bank.accountservice.exception.AccountNotFoundException;
import com.bank.accountservice.exception.DuplicateAccountException;
import com.bank.accountservice.exception.InsufficientFundsException;
import com.bank.accountservice.exception.InvalidAccountStatusException;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.repository.AuditLogRepository;
import com.bank.accountservice.util.EncryptionUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuditLogRepository auditLogRepository;
    private final CustomerClient customerClient;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        // Verify customer exists with circuit breaker
        if (!customerExists(request.customerId())) {
            throw new IllegalArgumentException("Customer not found: " + request.customerId());
        }

        // Check for duplicate account for same customer and type
        List<Account> existingAccounts = accountRepository.findByCustomerId(request.customerId());
        for (Account existing : existingAccounts) {
            if (existing.getAccountType().name().equals(request.accountType())) {
                throw new DuplicateAccountException(
                        String.format("Customer already has a %s account", request.accountType()));
            }
        }

        // Generate unique account number
        String accountNumber = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateAccountNumber();
        }

        // Encrypt account number for storage
        String encryptedAccountNumber = encryptionUtil.encrypt(accountNumber);

        Account account = Account.builder()
                .accountNumber(encryptedAccountNumber)
                .customerId(request.customerId())
                .accountType(Account.AccountType.valueOf(request.accountType()))
                .balance(request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO)
                .currency(request.currency())
                .status(Account.AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Created account {} for customer {}", accountNumber, request.customerId());

        // Create audit log
        createAuditLog(savedAccount.getId(), "ACCOUNT_CREATED", BigDecimal.ZERO,
                savedAccount.getBalance(), savedAccount.getBalance(),
                "Initial account creation", "SYSTEM");

        return AccountResponse.fromEntity(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountResponse.fromEntity(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        String encryptedAccountNumber = encryptionUtil.encrypt(accountNumber);
        Account account = accountRepository.findByAccountNumber(encryptedAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return AccountResponse.fromEntity(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream()
                .map(AccountResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AccountResponse updateAccountStatus(Long id, String newStatus) {
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        Account.AccountStatus currentStatus = account.getStatus();
        Account.AccountStatus requestedStatus = Account.AccountStatus.valueOf(newStatus);

        // Validate status transition
        validateStatusTransition(currentStatus, requestedStatus);

        account.setStatus(requestedStatus);
        Account updatedAccount = accountRepository.save(account);

        log.info("Updated account {} status from {} to {}",
                account.getAccountNumber(), currentStatus, requestedStatus);

        createAuditLog(updatedAccount.getId(), "STATUS_UPDATED", account.getBalance(),
                updatedAccount.getBalance(), BigDecimal.ZERO,
                String.format("Status changed from %s to %s", currentStatus, requestedStatus),
                "SYSTEM");

        return AccountResponse.fromEntity(updatedAccount);
    }

    @Transactional
    public AccountResponse creditAccount(String accountNumber, BigDecimal amount, String description, String performedBy) {
        String encryptedAccountNumber = encryptionUtil.encrypt(accountNumber);
        Account account = accountRepository.findByAccountNumberForUpdate(encryptedAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException(account.getStatus().name(), "ACTIVE");
        }

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);

        log.info("Credited {} to account {}. New balance: {}", amount, accountNumber, newBalance);

        createAuditLog(updatedAccount.getId(), "CREDIT", oldBalance, newBalance, amount, description, performedBy);

        return AccountResponse.fromEntity(updatedAccount);
    }

    @Transactional
    public AccountResponse debitAccount(String accountNumber, BigDecimal amount, String description, String performedBy) {
        String encryptedAccountNumber = encryptionUtil.encrypt(accountNumber);
        Account account = accountRepository.findByAccountNumberForUpdate(encryptedAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException(account.getStatus().name(), "ACTIVE");
        }

        BigDecimal oldBalance = account.getBalance();
        if (oldBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountNumber, amount.doubleValue(), oldBalance.doubleValue());
        }

        BigDecimal newBalance = oldBalance.subtract(amount);
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);

        log.info("Debited {} from account {}. New balance: {}", amount, accountNumber, newBalance);

        createAuditLog(updatedAccount.getId(), "DEBIT", oldBalance, newBalance, amount, description, performedBy);

        return AccountResponse.fromEntity(updatedAccount);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(Long accountId) {
        return auditLogRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @CircuitBreaker(name = "customerService", fallbackMethod = "customerExistsFallback")
    protected boolean customerExists(UUID customerId) {
        return customerClient.customerExists(customerId);
    }

    protected boolean customerExistsFallback(UUID customerId, Throwable t) {
        log.warn("Circuit breaker triggered for customer service, assuming customer exists: {}", customerId);
        return true; // Fallback to allow operation during service outage
    }

    private void validateStatusTransition(Account.AccountStatus current, Account.AccountStatus requested) {
        if (current == requested) {
            return; // No change needed
        }

        switch (current) {
            case ACTIVE:
                if (requested != Account.AccountStatus.FROZEN && requested != Account.AccountStatus.CLOSED) {
                    throw new InvalidAccountStatusException(current.name(), requested.name());
                }
                break;
            case FROZEN:
                if (requested != Account.AccountStatus.ACTIVE && requested != Account.AccountStatus.CLOSED) {
                    throw new InvalidAccountStatusException(current.name(), requested.name());
                }
                break;
            case CLOSED:
                throw new InvalidAccountStatusException(current.name(), requested.name());
        }
    }

    private String generateAccountNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(3);
        String randomPart = String.format("%06d", new SecureRandom().nextInt(1000000));
        return "ACC" + timestamp + randomPart;
    }

    private void createAuditLog(Long accountId, String operationType, BigDecimal oldBalance,
                                BigDecimal newBalance, BigDecimal amount, String description, String performedBy) {
        AuditLog auditLog = AuditLog.builder()
                .accountId(accountId)
                .operationType(operationType)
                .oldBalance(oldBalance)
                .newBalance(newBalance)
                .amount(amount)
                .description(description)
                .performedBy(performedBy)
                .build();
        auditLogRepository.save(auditLog);
    }
}
