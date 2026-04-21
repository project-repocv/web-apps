#!/bin/bash

# Service: UserService
cat > /workspace/auth-service/src/main/java/com/bank/authservice/service/UserService.java << 'EOF'
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
EOF

# Service: UserServiceImpl
cat > /workspace/auth-service/src/main/java/com/bank/authservice/service/UserServiceImpl.java << 'EOF'
package com.bank.authservice.service;

import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.User;
import com.bank.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == User.UserStatus.ACTIVE,
                true, true,
                user.getStatus() != User.UserStatus.SUSPENDED,
                authorities.isEmpty() ? Set.of(new SimpleGrantedAuthority("ROLE_USER")) : authorities
        );
    }
    
    @Override
    public User createUser(RegisterRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (existsByCustomerId(request.getCustomerId())) {
            throw new IllegalArgumentException("Customer already has an account");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .customerId(request.getCustomerId())
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.PENDING_ACTIVATION)
                .build();
        User savedUser = userRepository.save(user);
        log.info("Created user with username: {} and customer ID: {}", request.getUsername(), request.getCustomerId());
        return savedUser;
    }
    
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    @Override
    public User findByCustomerId(String customerId) {
        return userRepository.findByCustomerId(customerId).orElse(null);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByCustomerId(String customerId) {
        return userRepository.existsByCustomerId(customerId);
    }
    
    @Override
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }
    
    @Override
    public User suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(User.UserStatus.SUSPENDED);
        return userRepository.save(user);
    }
}
EOF

echo "Auth Service UserService created successfully"
