package com.bank.customerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Customer Service.
 * Implements RFC 7807 Problem Details format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Problem> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("/problems/customer-not-found"))
                .withTitle("Customer Not Found")
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .with("timestamp", LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Problem> handleDuplicateEmailException(DuplicateEmailException ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("/problems/duplicate-email"))
                .withTitle("Duplicate Email")
                .withStatus(Status.CONFLICT)
                .withDetail(ex.getMessage())
                .with("email", ex.getEmail())
                .with("timestamp", LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Problem problem = Problem.builder()
                .withType(URI.create("/problems/validation-error"))
                .withTitle("Validation Error")
                .withStatus(Status.BAD_REQUEST)
                .withDetail("One or more validation errors occurred")
                .with("errors", errors)
                .with("timestamp", LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleGenericException(Exception ex) {
        Problem problem = Problem.builder()
                .withType(URI.create("/problems/internal-error"))
                .withTitle("Internal Server Error")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .withDetail(ex.getMessage())
                .with("timestamp", LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
