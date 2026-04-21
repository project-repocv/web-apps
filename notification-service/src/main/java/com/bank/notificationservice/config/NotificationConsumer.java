package com.bank.notificationservice.config;

import com.bank.notificationservice.dto.TransactionEvent;
import com.bank.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {}", event.getTransactionId());
        
        try {
            notificationService.processTransactionEvent(event);
            log.info("Processed transaction event successfully: {}", event.getTransactionId());
        } catch (Exception e) {
            log.error("Error processing transaction event: {}", event.getTransactionId(), e);
            throw e;
        }
    }
}
