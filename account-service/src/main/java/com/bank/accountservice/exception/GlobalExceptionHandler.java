package com.bank.accountservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAccountNotFound(AccountNotFoundException ex) {
        log.error("Account not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Account Not Found");
        problemDetail.setProperty("errorCategory", "ACCOUNT_NOT_FOUND");
        problemDetail.setProperty("timestamp", Instant.now());
        if (ex.getAccountNumber() != null) {
            problemDetail.setProperty("accountNumber", ex.getAccountNumber());
        }
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientFunds(InsufficientFundsException ex) {
        log.error("Insufficient funds: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Insufficient Funds");
        problemDetail.setProperty("errorCategory", "INSUFFICIENT_FUNDS");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("accountNumber", ex.getAccountNumber());
        problemDetail.setProperty("requestedAmount", ex.getRequestedAmount());
        problemDetail.setProperty("availableBalance", ex.getAvailableBalance());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateAccount(DuplicateAccountException ex) {
        log.error("Duplicate account: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate Account");
        problemDetail.setProperty("errorCategory", "DUPLICATE_ACCOUNT");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("accountNumber", ex.getAccountNumber());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(InvalidAccountStatusException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAccountStatus(InvalidAccountStatusException ex) {
        log.error("Invalid account status transition: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid Account Status");
        problemDetail.setProperty("errorCategory", "INVALID_STATUS_TRANSITION");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("currentStatus", ex.getCurrentStatus());
        problemDetail.setProperty("requestedStatus", ex.getRequestedStatus());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyConflict(IdempotencyKeyConflictException ex) {
        log.error("Idempotency key conflict: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Idempotency Key Conflict");
        problemDetail.setProperty("errorCategory", "IDEMPOTENCY_CONFLICT");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("idempotencyKey", ex.getIdempotencyKey());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errorCategory", "VALIDATION_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("errorCategory", "INTERNAL_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.of(problemDetail).build();
    }
}
