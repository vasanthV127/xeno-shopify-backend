package com.xeno.service;

import com.xeno.model.*;
import com.xeno.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final CartEventRepository cartEventRepository;
    private final CheckoutEventRepository checkoutEventRepository;
    private final TenantRepository tenantRepository;
    
    @Transactional
    public void processCartCreate(Map<String, Object> payload, String shopDomain) {
        try {
            log.info("Processing cart_create webhook for domain: {}", shopDomain);
            
            // Find tenant by shop domain
            Tenant tenant = tenantRepository.findByShopifyDomain(shopDomain)
                    .orElseThrow(() -> new RuntimeException("Tenant not found for domain: " + shopDomain));
            
            CartEvent cartEvent = new CartEvent();
            cartEvent.setTenantId(tenant.getTenantId());
            cartEvent.setCartToken((String) payload.get("token"));
            cartEvent.setEventType("cart_created");
            
            // Extract customer info if available
            if (payload.containsKey("customer") && payload.get("customer") != null) {
                Map<String, Object> customer = (Map<String, Object>) payload.get("customer");
                cartEvent.setCustomerEmail((String) customer.get("email"));
                cartEvent.setCustomerId(String.valueOf(customer.get("id")));
            }
            
            // Calculate cart value and item count
            if (payload.containsKey("line_items")) {
                List<Map<String, Object>> lineItems = (List<Map<String, Object>>) payload.get("line_items");
                cartEvent.setItemCount(lineItems.size());
                
                // Calculate total
                BigDecimal total = BigDecimal.ZERO;
                for (Map<String, Object> item : lineItems) {
                    if (item.containsKey("price") && item.containsKey("quantity")) {
                        BigDecimal price = new BigDecimal(item.get("price").toString());
                        Integer quantity = (Integer) item.get("quantity");
                        total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
                    }
                }
                cartEvent.setCartValue(total);
            }
            
            cartEvent.setIsAbandoned(false);
            cartEventRepository.save(cartEvent);
            
            log.info("Cart event saved successfully for tenant: {}", tenant.getTenantId());
        } catch (Exception e) {
            log.error("Error processing cart_create webhook", e);
        }
    }
    
    @Transactional
    public void processCheckoutCreate(Map<String, Object> payload, String shopDomain) {
        try {
            log.info("Processing checkout_create webhook for domain: {}", shopDomain);
            
            // Find tenant by shop domain
            Tenant tenant = tenantRepository.findByShopifyDomain(shopDomain)
                    .orElseThrow(() -> new RuntimeException("Tenant not found for domain: " + shopDomain));
            
            CheckoutEvent checkoutEvent = new CheckoutEvent();
            checkoutEvent.setTenantId(tenant.getTenantId());
            checkoutEvent.setCheckoutToken((String) payload.get("token"));
            checkoutEvent.setEventType("checkout_started");
            
            // Extract customer info
            if (payload.containsKey("customer") && payload.get("customer") != null) {
                Map<String, Object> customer = (Map<String, Object>) payload.get("customer");
                checkoutEvent.setCustomerEmail((String) customer.get("email"));
                checkoutEvent.setCustomerId(String.valueOf(customer.get("id")));
            } else if (payload.containsKey("email")) {
                checkoutEvent.setCustomerEmail((String) payload.get("email"));
            }
            
            // Extract checkout value
            if (payload.containsKey("total_price")) {
                checkoutEvent.setCheckoutValue(new BigDecimal(payload.get("total_price").toString()));
            }
            
            // Extract item count
            if (payload.containsKey("line_items")) {
                List<Map<String, Object>> lineItems = (List<Map<String, Object>>) payload.get("line_items");
                checkoutEvent.setItemCount(lineItems.size());
            }
            
            checkoutEvent.setCompleted(false);
            checkoutEvent.setAbandoned(false);
            checkoutEventRepository.save(checkoutEvent);
            
            log.info("Checkout event saved successfully for tenant: {}", tenant.getTenantId());
        } catch (Exception e) {
            log.error("Error processing checkout_create webhook", e);
        }
    }
    
    @Transactional
    public void processCheckoutUpdate(Map<String, Object> payload, String shopDomain) {
        try {
            log.info("Processing checkout_update webhook for domain: {}", shopDomain);
            
            Tenant tenant = tenantRepository.findByShopifyDomain(shopDomain)
                    .orElseThrow(() -> new RuntimeException("Tenant not found for domain: " + shopDomain));
            
            String checkoutToken = (String) payload.get("token");
            
            // Check if order was completed
            if (payload.containsKey("order_id") && payload.get("order_id") != null) {
                // Checkout was completed - mark as completed
                CheckoutEvent checkoutEvent = new CheckoutEvent();
                checkoutEvent.setTenantId(tenant.getTenantId());
                checkoutEvent.setCheckoutToken(checkoutToken);
                checkoutEvent.setEventType("checkout_completed");
                checkoutEvent.setCompleted(true);
                checkoutEvent.setCompletedAt(LocalDateTime.now());
                checkoutEventRepository.save(checkoutEvent);
                
                log.info("Checkout completed event saved for tenant: {}", tenant.getTenantId());
            }
        } catch (Exception e) {
            log.error("Error processing checkout_update webhook", e);
        }
    }
}
