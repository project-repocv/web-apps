package com.bank.customerservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a customer is not found.
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {

    private final String customerId;

    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId);
        this.customerId = customerId;
    }

    public CustomerNotFoundException(Long id) {
        super("Customer not found with ID: " + id);
        this.customerId = id.toString();
    }
}
