package com.bank.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String customerId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "preference_channels", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "channel")
    private Set<NotificationChannel> channels;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "preference_types", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "type")
    private Set<NotificationType> types;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
