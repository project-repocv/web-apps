package com.bank.customerservice.service;

import com.bank.customerservice.dto.CustomerRequest;
import com.bank.customerservice.dto.CustomerResponse;
import com.bank.customerservice.dto.KycUpdateRequest;
import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.exception.CustomerNotFoundException;
import com.bank.customerservice.exception.DuplicateEmailException;
import com.bank.customerservice.mapper.CustomerMapper;
import com.bank.customerservice.repository.CustomerRepository;
import com.bank.customerservice.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CustomerService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EncryptionUtil encryptionUtil;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setCustomerId(UUID.randomUUID());

        // Encrypt SSN if provided
        if (request.getSsn() != null && !request.getSsn().isBlank()) {
            customer.setEncryptedSsn(encryptionUtil.encrypt(request.getSsn()));
        }

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created with ID: {}", savedCustomer.getId());

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer by ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByCustomerId(UUID customerId) {
        log.debug("Fetching customer by CustomerID: {}", customerId);
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));
        return customerMapper.toResponse(customer);
    }

    @Override
    public Optional<CustomerResponse> getCustomerByEmail(String email) {
        log.debug("Fetching customer by email: {}", email);
        return customerRepository.findByEmail(email)
                .map(customerMapper::toResponse);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        // Check if email is being changed and if it's already taken
        if (!customer.getEmail().equals(request.getEmail()) 
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        customerMapper.updateCustomerFromRequest(request, customer);

        // Update encrypted SSN if provided
        if (request.getSsn() != null && !request.getSsn().isBlank()) {
            customer.setEncryptedSsn(encryptionUtil.encrypt(request.getSsn()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated with ID: {}", updatedCustomer.getId());

        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public CustomerResponse updateKycStatus(Long id, KycUpdateRequest request) {
        log.info("Updating KYC status for customer ID: {}, new status: {}", id, request.getKycStatus());

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.setKycStatus(request.getKycStatus());
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("KYC status updated for customer ID: {}", updatedCustomer.getId());

        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted with ID: {}", id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}
