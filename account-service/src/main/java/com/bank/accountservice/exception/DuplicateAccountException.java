package com.bank.accountservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAccountException extends RuntimeException {

    private final String accountNumber;

    public DuplicateAccountException(String accountNumber) {
        super("Account already exists: " + accountNumber);
        this.accountNumber = accountNumber;
    }
}
