package com.bank.notificationservice.service;

import com.bank.notificationservice.dto.NotificationRequest;
import com.bank.notificationservice.dto.NotificationResponse;
import com.bank.notificationservice.dto.SendNotificationRequest;
import com.bank.notificationservice.entity.NotificationStatus;
import com.bank.notificationservice.entity.NotificationType;
import com.bank.notificationservice.repository.NotificationRepository;
import com.bank.notificationservice.service.impl.NotificationServiceImpl;
import com.bank.notificationservice.util.EmailService;
import com.bank.notificationservice.util.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository, emailService, smsService);
    }

    @Test
    void createNotification_success() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "customer-123",
                "john@example.com",
                "+1234567890",
                NotificationType.TRANSACTION_DEPOSIT,
                "Deposit Confirmation",
                "Your deposit of $100 has been processed.",
                "TXN123",
                "ACC123",
                new BigDecimal("100.00"),
                "USD"
        );

        // Mock saved notification
        when(notificationRepository.save(any())).thenAnswer(invocation -> {
            var notif = invocation.getArgument(0);
            return notif;
        });

        // When
        NotificationResponse response = notificationService.createNotification(request);

        // Then
        assertNotNull(response);
        assertEquals("customer-123", response.customerId());
        assertEquals(NotificationType.TRANSACTION_DEPOSIT, response.notificationType());
        verify(notificationRepository).save(any());
    }

    @Test
    void sendNotification_success() {
        // Given
        SendNotificationRequest request = new SendNotificationRequest(
                "john@example.com",
                "+1234567890",
                NotificationType.TRANSACTION_WITHDRAWAL,
                "Withdrawal Confirmation",
                "Your withdrawal of $50 has been processed.",
                "TXN456",
                "ACC123",
                new BigDecimal("50.00"),
                "USD"
        );

        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        doNothing().when(smsService).sendSms(anyString(), anyString());
        
        when(notificationRepository.save(any())).thenAnswer(invocation -> {
            var notif = invocation.getArgument(0);
            return notif;
        });

        // When
        NotificationResponse response = notificationService.sendNotification(request);

        // Then
        assertNotNull(response);
        assertEquals(NotificationStatus.SENT, response.status());
        verify(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        verify(smsService).sendSms(anyString(), anyString());
    }

    @Test
    void getNotificationsByCustomer_success() {
        // Given
        String customerId = "customer-123";
        when(notificationRepository.findByCustomerId(customerId)).thenReturn(List.of());

        // When
        List<NotificationResponse> responses = notificationService.getNotificationsByCustomer(customerId);

        // Then
        assertNotNull(responses);
        verify(notificationRepository).findByCustomerId(customerId);
    }

    @Test
    void retryFailedNotifications_success() {
        // Given
        when(notificationRepository.findByStatus(NotificationStatus.FAILED)).thenReturn(List.of());

        // When
        notificationService.retryFailedNotifications();

        // Then
        verify(notificationRepository).findByStatus(NotificationStatus.FAILED);
    }
}
