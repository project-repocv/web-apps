package com.bank.notificationservice.repository;

import com.bank.notificationservice.entity.Notification;
import com.bank.notificationservice.entity.NotificationStatus;
import com.bank.notificationservice.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
class NotificationRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void saveNotification_success() {
        // Given
        Notification notification = Notification.builder()
                .customerId("customer-123")
                .recipientEmail("john@example.com")
                .recipientPhone("+1234567890")
                .notificationType(NotificationType.TRANSACTION_DEPOSIT)
                .subject("Deposit Confirmation")
                .content("Your deposit has been processed.")
                .status(NotificationStatus.PENDING)
                .transactionId("TXN123")
                .accountNumber("ACC123")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .deliveryAttempts(0)
                .build();

        // When
        Notification savedNotification = notificationRepository.save(notification);

        // Then
        assertNotNull(savedNotification.getId());
        assertNotNull(savedNotification.getNotificationId());
        assertEquals("customer-123", savedNotification.getCustomerId());
        assertEquals(NotificationType.TRANSACTION_DEPOSIT, savedNotification.getNotificationType());
        assertEquals(NotificationStatus.PENDING, savedNotification.getStatus());
    }

    @Test
    void findByNotificationId_found() {
        // Given
        Notification notification = Notification.builder()
                .customerId("customer-123")
                .recipientEmail("john@example.com")
                .notificationType(NotificationType.TRANSACTION_WITHDRAWAL)
                .subject("Withdrawal Confirmation")
                .content("Your withdrawal has been processed.")
                .status(NotificationStatus.SENT)
                .build();
        notificationRepository.save(notification);

        // When
        var foundNotification = notificationRepository.findByNotificationId(notification.getNotificationId());

        // Then
        assertTrue(foundNotification.isPresent());
        assertEquals("customer-123", foundNotification.get().getCustomerId());
        assertEquals(NotificationStatus.SENT, foundNotification.get().getStatus());
    }

    @Test
    void findByNotificationId_notFound() {
        // When
        var foundNotification = notificationRepository.findByNotificationId("NONEXISTENT");

        // Then
        assertTrue(foundNotification.isEmpty());
    }

    @Test
    void findByCustomerId_success() {
        // Given
        Notification notification1 = Notification.builder()
                .customerId("customer-123")
                .recipientEmail("john@example.com")
                .notificationType(NotificationType.TRANSACTION_DEPOSIT)
                .subject("Deposit")
                .content("Content 1")
                .status(NotificationStatus.SENT)
                .build();
                
        Notification notification2 = Notification.builder()
                .customerId("customer-123")
                .recipientEmail("john@example.com")
                .notificationType(NotificationType.TRANSACTION_WITHDRAWAL)
                .subject("Withdrawal")
                .content("Content 2")
                .status(NotificationStatus.SENT)
                .build();
                
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        // When
        var notifications = notificationRepository.findByCustomerId("customer-123");

        // Then
        assertEquals(2, notifications.size());
    }

    @Test
    void findByStatus_success() {
        // Given
        Notification notification1 = Notification.builder()
                .customerId("customer-123")
                .recipientEmail("john@example.com")
                .notificationType(NotificationType.TRANSACTION_DEPOSIT)
                .subject("Deposit")
                .content("Content 1")
                .status(NotificationStatus.PENDING)
                .build();
                
        Notification notification2 = Notification.builder()
                .customerId("customer-456")
                .recipientEmail("jane@example.com")
                .notificationType(NotificationType.TRANSACTION_WITHDRAWAL)
                .subject("Withdrawal")
                .content("Content 2")
                .status(NotificationStatus.PENDING)
                .build();
                
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        // When
        var notifications = notificationRepository.findByStatus(NotificationStatus.PENDING);

        // Then
        assertEquals(2, notifications.size());
    }
}
