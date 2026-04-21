package com.bank.transactionservice.exception;

public class TransactionNotFoundException extends TransactionException {

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
