package com.bank.transactionservice.exception;

public class IdempotencyException extends TransactionException {

    public IdempotencyException(String message) {
        super(message);
    }
}
