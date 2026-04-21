package com.bank.authservice.service;

import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User createUser(RegisterRequest request);
    User findByUsername(String username);
    User findByCustomerId(String customerId);
    boolean existsByUsername(String username);
    boolean existsByCustomerId(String customerId);
    User activateUser(Long userId);
    User suspendUser(Long userId);
}
