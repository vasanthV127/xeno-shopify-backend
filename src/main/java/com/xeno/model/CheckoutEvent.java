package com.xeno.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkout_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(name = "checkout_token")
    private String checkoutToken;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "checkout_value")
    private BigDecimal checkoutValue;
    
    @Column(name = "item_count")
    private Integer itemCount;
    
    @Column(name = "event_type")
    private String eventType; // checkout_started, checkout_completed, checkout_abandoned
    
    @Column(name = "completed")
    private Boolean completed = false;
    
    @Column(name = "abandoned")
    private Boolean abandoned = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "abandoned_at")
    private LocalDateTime abandonedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
