package com.securebank.accountservice.service;

import com.securebank.accountservice.model.Account;
import com.securebank.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String userId, Account.AccountType type) {
        String accountNumber = generateAccountNumber();
        
        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .type(type)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccountById(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public Account deposit(String accountId, BigDecimal amount) {
        Account account = getAccountById(accountId);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    public Account withdraw(String accountId, BigDecimal amount) {
        Account account = getAccountById(accountId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("1000000");
        for (int i = 0; i < 3; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
