package com.bank.customerservice.controller;

import com.bank.customerservice.dto.CustomerRequest;
import com.bank.customerservice.dto.CustomerResponse;
import com.bank.customerservice.dto.KycUpdateRequest;
import com.bank.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for Customer operations.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Create a new customer.
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        log.info("Received request to create customer");
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get customer by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.debug("Received request to get customer by ID: {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get customer by CustomerID (UUID).
     */
    @GetMapping("/uuid/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerByCustomerId(@PathVariable UUID customerId) {
        log.debug("Received request to get customer by CustomerID: {}", customerId);
        CustomerResponse response = customerService.getCustomerByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get customer by email.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        log.debug("Received request to get customer by email: {}", email);
        Optional<CustomerResponse> response = customerService.getCustomerByEmail(email);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all customers.
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.debug("Received request to get all customers");
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Update customer.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        log.info("Received request to update customer with ID: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update KYC status.
     */
    @PatchMapping("/{id}/kyc")
    public ResponseEntity<CustomerResponse> updateKycStatus(
            @PathVariable Long id,
            @Valid @RequestBody KycUpdateRequest request) {
        log.info("Received request to update KYC status for customer ID: {}", id);
        CustomerResponse response = customerService.updateKycStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete customer.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("Received request to delete customer with ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if email exists.
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        log.debug("Checking if email exists: {}", email);
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
