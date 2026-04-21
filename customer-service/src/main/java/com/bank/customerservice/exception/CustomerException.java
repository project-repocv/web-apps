package com.bank.customerservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for customer service with RFC 7807 problem details support.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomerException extends RuntimeException {

    private final String errorCode;
    private final String message;
    private final HttpStatus status;

    public CustomerException(String message) {
        this("CUSTOMER_ERROR", message, HttpStatus.BAD_REQUEST);
    }
}
