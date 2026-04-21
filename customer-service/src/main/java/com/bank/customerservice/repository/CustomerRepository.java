package com.bank.customerservice.repository;

import com.bank.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Customer entity.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by email address.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by unique customer ID.
     */
    Optional<Customer> findByCustomerId(UUID customerId);

    /**
     * Check if email exists (for uniqueness validation).
     */
    boolean existsByEmail(String email);

    /**
     * Find customer by email with pessimistic locking.
     * Used for concurrent update scenarios.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Optional<Customer> findByEmailWithLock(String email);

    /**
     * Find customer by customer ID with pessimistic locking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.customerId = :customerId")
    Optional<Customer> findByCustomerIdWithLock(UUID customerId);
}
