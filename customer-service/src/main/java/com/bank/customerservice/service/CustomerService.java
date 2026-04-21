package com.bank.customerservice.service;

import com.bank.customerservice.dto.CustomerRequest;
import com.bank.customerservice.dto.CustomerResponse;
import com.bank.customerservice.dto.KycUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Customer operations.
 */
public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(Long id);

    CustomerResponse getCustomerByCustomerId(UUID customerId);

    Optional<CustomerResponse> getCustomerByEmail(String email);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    CustomerResponse updateKycStatus(Long id, KycUpdateRequest request);

    void deleteCustomer(Long id);

    boolean existsByEmail(String email);
}
