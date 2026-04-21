package com.bank.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "customer-service", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    CustomerDTO getCustomer(@PathVariable UUID customerId);

    @GetMapping("/{customerId}/exists")
    boolean customerExists(@PathVariable UUID customerId);

    record CustomerDTO(
            UUID customerId,
            String fullName,
            String email,
            String phone,
            String kycStatus
    ) {
    }
}
