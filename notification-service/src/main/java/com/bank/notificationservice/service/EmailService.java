package com.bank.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {
    
    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject, String body) {
        try {
            Thread.sleep(100);
            log.info("Sending email to: {}, Subject: {}", to, subject);
            log.info("Email body: {}", body);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Email sending interrupted for: {}", to, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    public boolean validateEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
