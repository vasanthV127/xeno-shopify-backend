package com.xeno.controller;

import com.xeno.model.CartEvent;
import com.xeno.model.CheckoutEvent;
import com.xeno.repository.CartEventRepository;
import com.xeno.repository.CheckoutEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final CartEventRepository cartEventRepository;
    private final CheckoutEventRepository checkoutEventRepository;
    
    @GetMapping("/carts")
    public ResponseEntity<List<CartEvent>> getCartEvents(Authentication authentication) {
        String tenantId = authentication.getName();
        List<CartEvent> cartEvents = cartEventRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(cartEvents);
    }
    
    @GetMapping("/carts/abandoned")
    public ResponseEntity<List<CartEvent>> getAbandonedCarts(Authentication authentication) {
        String tenantId = authentication.getName();
        List<CartEvent> abandonedCarts = cartEventRepository.findByTenantIdAndIsAbandoned(tenantId, true);
        return ResponseEntity.ok(abandonedCarts);
    }
    
    @GetMapping("/checkouts")
    public ResponseEntity<List<CheckoutEvent>> getCheckoutEvents(Authentication authentication) {
        String tenantId = authentication.getName();
        List<CheckoutEvent> checkoutEvents = checkoutEventRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(checkoutEvents);
    }
    
    @GetMapping("/checkouts/abandoned")
    public ResponseEntity<List<CheckoutEvent>> getAbandonedCheckouts(Authentication authentication) {
        String tenantId = authentication.getName();
        List<CheckoutEvent> abandonedCheckouts = checkoutEventRepository.findByTenantIdAndAbandoned(tenantId, true);
        return ResponseEntity.ok(abandonedCheckouts);
    }
    
    @GetMapping("/checkouts/completed")
    public ResponseEntity<List<CheckoutEvent>> getCompletedCheckouts(Authentication authentication) {
        String tenantId = authentication.getName();
        List<CheckoutEvent> completedCheckouts = checkoutEventRepository.findByTenantIdAndCompleted(tenantId, true);
        return ResponseEntity.ok(completedCheckouts);
    }
}
