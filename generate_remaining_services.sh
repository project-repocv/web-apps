#!/bin/bash

echo "Generating remaining banking microservices..."

# Create Account Service remaining files
echo "Creating Account Service configuration files..."

cat > /workspace/account-service/src/main/java/com/bank/accountservice/util/EncryptionUtil.java << 'EOF'
package com.bank.accountservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtil {

    @Value("${app.encryption.key:DefaultEncryptionKey1234567890123456}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public String encrypt(String data) {
        try {
            SecretKeySpec keySpec = generateKey(encryptionKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = generateKey(encryptionKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        keyBytes = Arrays.copyOf(keyBytes, 32);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
EOF

cat > /workspace/account-service/src/main/java/com/bank/accountservice/controller/AccountController.java << 'EOF'
package com.bank.accountservice.controller;

import com.bank.accountservice.dto.AccountRequest;
import com.bank.accountservice.dto.AccountResponse;
import com.bank.accountservice.dto.BalanceUpdateRequest;
import com.bank.accountservice.dto.UpdateAccountStatusRequest;
import com.bank.accountservice.entity.AuditLog;
import com.bank.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable UUID customerId) {
        List<AccountResponse> responses = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountResponse response = accountService.updateAccountStatus(id, request.status());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> creditAccount(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestHeader(value = "X-Performed-By", defaultValue = "SYSTEM") String performedBy) {
        AccountResponse response = accountService.creditAccount(accountNumber, amount, 
                description != null ? description : "Credit", performedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestHeader(value = "X-Performed-By", defaultValue = "SYSTEM") String performedBy) {
        AccountResponse response = accountService.debitAccount(accountNumber, amount, 
                description != null ? description : "Debit", performedBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@PathVariable Long id) {
        List<AuditLog> logs = accountService.getAuditLogs(id);
        return ResponseEntity.ok(logs);
    }
}
EOF

cat > /workspace/account-service/src/main/resources/application.yml << 'EOF'
server:
  port: 8082

spring:
  application:
    name: account-service
  config:
    import: optional:configserver:http://config-server:8888
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:account_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: false

resilience4j:
  circuitbreaker:
    instances:
      customerService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 5
  retry:
    instances:
      customerService:
        maxAttempts: 3
        waitDuration: 1000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
  metrics:
    export:
      prometheus:
        enabled: true

app:
  encryption:
    key: ${ENCRYPTION_KEY:DefaultEncryptionKey1234567890123456}

logging:
  level:
    com.bank.accountservice: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
EOF

cat > /workspace/account-service/src/main/resources/db/migration/V1__create_account_tables.sql << 'EOF'
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    old_balance NUMERIC(19, 4),
    new_balance NUMERIC(19, 4),
    amount NUMERIC(19, 4),
    description TEXT,
    performed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    key_value VARCHAR(255) PRIMARY KEY,
    request_hash VARCHAR(255) NOT NULL,
    response_body TEXT,
    status_code INTEGER,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_audit_logs_account_id ON audit_logs(account_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_idempotency_keys_expires_at ON idempotency_keys(expires_at);
EOF

cat > /workspace/account-service/Dockerfile << 'EOF'
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/account-service-*.jar account-service.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "account-service.jar"]
EOF

echo "Account Service completed!"
echo "=== END OF SERVICE: account-service ==="

