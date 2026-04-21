package com.bank.notificationservice.repository;

import com.bank.notificationservice.entity.Notification;
import com.bank.notificationservice.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt < :beforeDate AND n.status = 'FAILED'")
    List<Notification> findFailedNotificationsBefore(LocalDateTime beforeDate);
    
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
}
