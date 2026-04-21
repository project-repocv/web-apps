package com.bank.accountservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundsException extends RuntimeException {

    private final String accountNumber;
    private final Double requestedAmount;
    private final Double availableBalance;

    public InsufficientFundsException(String accountNumber, Double requestedAmount, Double availableBalance) {
        super(String.format("Insufficient funds for account %s. Requested: %.2f, Available: %.2f",
                accountNumber, requestedAmount, availableBalance));
        this.accountNumber = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }
}
