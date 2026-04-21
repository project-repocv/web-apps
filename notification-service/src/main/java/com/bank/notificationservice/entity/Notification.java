package com.bank.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String recipient;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    
    @Column(length = 2000, nullable = false)
    private String content;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    private String errorMessage;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime sentAt;
    
    private Integer retryCount;
    
    @Column(name = "transaction_id")
    private String transactionId;
}

enum NotificationType {
    TRANSACTION_ALERT,
    LOW_BALANCE,
    ACCOUNT_FROZEN,
    ACCOUNT_CLOSED,
    SECURITY_ALERT,
    PROMOTIONAL
}

enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH_NOTIFICATION
}

enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    DELIVERED
}
