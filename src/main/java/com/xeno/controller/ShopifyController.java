package com.xeno.controller;

import com.xeno.model.Tenant;
import com.xeno.service.AuthService;
import com.xeno.service.ShopifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shopify")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ShopifyController {

    private final ShopifyService shopifyService;
    private final AuthService authService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncData() {
        Tenant tenant = authService.getCurrentTenant();
        log.info("Manual sync triggered for tenant: {}", tenant.getTenantId());
        
        try {
            shopifyService.syncShopifyData(tenant);
            return ResponseEntity.ok(Map.of(
                "message", "Data sync completed successfully",
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("Error during manual sync", e);
            return ResponseEntity.status(500).body(Map.of(
                "message", "Data sync failed: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    @PostMapping("/webhooks/orders/create")
    public ResponseEntity<String> handleOrderCreate(@RequestBody Map<String, Object> orderData,
                                                     @RequestHeader("X-Shopify-Shop-Domain") String shopDomain) {
        log.info("Received order create webhook from: {}", shopDomain);
        // Webhook handling logic here
        return ResponseEntity.ok("Webhook received");
    }

    @PostMapping("/webhooks/customers/create")
    public ResponseEntity<String> handleCustomerCreate(@RequestBody Map<String, Object> customerData,
                                                        @RequestHeader("X-Shopify-Shop-Domain") String shopDomain) {
        log.info("Received customer create webhook from: {}", shopDomain);
        return ResponseEntity.ok("Webhook received");
    }

    @PostMapping("/webhooks/products/create")
    public ResponseEntity<String> handleProductCreate(@RequestBody Map<String, Object> productData,
                                                       @RequestHeader("X-Shopify-Shop-Domain") String shopDomain) {
        log.info("Received product create webhook from: {}", shopDomain);
        return ResponseEntity.ok("Webhook received");
    }
}
