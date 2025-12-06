package com.xeno.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(name = "cart_token")
    private String cartToken;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "cart_value")
    private BigDecimal cartValue;
    
    @Column(name = "item_count")
    private Integer itemCount;
    
    @Column(name = "event_type")
    private String eventType; // cart_created, cart_abandoned
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "abandoned_at")
    private LocalDateTime abandonedAt;
    
    @Column(name = "is_abandoned")
    private Boolean isAbandoned = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
