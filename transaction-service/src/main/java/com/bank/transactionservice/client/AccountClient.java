package com.bank.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", path = "/api/accounts")
public interface AccountClient {

    @GetMapping("/number/{accountNumber}")
    AccountDTO getAccountByNumber(@PathVariable String accountNumber);

    @GetMapping("/exists/{accountNumber}")
    boolean accountExists(@PathVariable String accountNumber);

    @PutMapping("/{id}/balance")
    AccountDTO updateBalance(@PathVariable Long id,
                             @RequestParam BigDecimal amount,
                             @RequestParam String operationType,
                             @RequestParam(required = false) String description);

    record AccountDTO(Long id, String accountNumber, String customerId, String accountType,
                      BigDecimal balance, String currency, String status) {
    }
}
