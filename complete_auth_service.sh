#!/bin/bash

# Complete Auth Service - Entity: RefreshToken
cat > /workspace/auth-service/src/main/java/com/bank/authservice/entity/RefreshToken.java << 'EOF'
package com.bank.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
EOF

# Repository: UserRepository
cat > /workspace/auth-service/src/main/java/com/bank/authservice/repository/UserRepository.java << 'EOF'
package com.bank.authservice.repository;

import com.bank.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByCustomerId(String customerId);
    boolean existsByUsername(String username);
    boolean existsByCustomerId(String customerId);
}
EOF

# Repository: RefreshTokenRepository
cat > /workspace/auth-service/src/main/java/com/bank/authservice/repository/RefreshTokenRepository.java << 'EOF'
package com.bank.authservice.repository;

import com.bank.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsername(String username);
}
EOF

echo "Auth Service repositories created successfully"
