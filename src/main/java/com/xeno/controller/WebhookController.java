package com.xeno.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xeno.model.Customer;
import com.xeno.model.Order;
import com.xeno.model.Product;
import com.xeno.model.Tenant;
import com.xeno.repository.CustomerRepository;
import com.xeno.repository.OrderRepository;
import com.xeno.repository.ProductRepository;
import com.xeno.repository.TenantRepository;
import com.xeno.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/webhooks/shopify")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebhookService webhookService;

    /**
     * Webhook endpoint for Shopify order creation/update
     */
    @PostMapping("/orders/create")
    public ResponseEntity<Map<String, String>> handleOrderCreate(
            @RequestBody String payload,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String hmacHeader) {
        
        logger.info("Received order create webhook from domain: {}", shopDomain);

        try {
            // Find tenant by shop domain
            Tenant tenant = findTenantByShopDomain(shopDomain);
            if (tenant == null) {
                logger.warn("No tenant found for shop domain: {}", shopDomain);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Tenant not found"));
            }

            // Verify webhook authenticity
            if (!verifyWebhook(payload, hmacHeader, tenant.getShopifyAccessToken())) {
                logger.warn("Invalid webhook signature for domain: {}", shopDomain);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }

            // Parse order data
            JsonNode orderJson = objectMapper.readTree(payload);
            Order order = parseOrder(orderJson, tenant);
            
            // Save or update order
            orderRepository.save(order);
            logger.info("Order {} saved successfully for tenant {}", order.getShopifyOrderId(), tenant.getId());

            return ResponseEntity.ok(Map.of("status", "success", "message", "Order processed"));

        } catch (Exception e) {
            logger.error("Error processing order webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Webhook endpoint for Shopify customer creation/update
     */
    @PostMapping("/customers/create")
    public ResponseEntity<Map<String, String>> handleCustomerCreate(
            @RequestBody String payload,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String hmacHeader) {
        
        logger.info("Received customer create webhook from domain: {}", shopDomain);

        try {
            Tenant tenant = findTenantByShopDomain(shopDomain);
            if (tenant == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Tenant not found"));
            }

            if (!verifyWebhook(payload, hmacHeader, tenant.getShopifyAccessToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }

            JsonNode customerJson = objectMapper.readTree(payload);
            Customer customer = parseCustomer(customerJson, tenant);
            
            customerRepository.save(customer);
            logger.info("Customer {} saved successfully", customer.getShopifyCustomerId());

            return ResponseEntity.ok(Map.of("status", "success", "message", "Customer processed"));

        } catch (Exception e) {
            logger.error("Error processing customer webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Webhook endpoint for Shopify product creation/update
     */
    @PostMapping("/products/create")
    public ResponseEntity<Map<String, String>> handleProductCreate(
            @RequestBody String payload,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String hmacHeader) {
        
        logger.info("Received product create webhook from domain: {}", shopDomain);

        try {
            Tenant tenant = findTenantByShopDomain(shopDomain);
            if (tenant == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Tenant not found"));
            }

            if (!verifyWebhook(payload, hmacHeader, tenant.getShopifyAccessToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }

            JsonNode productJson = objectMapper.readTree(payload);
            Product product = parseProduct(productJson, tenant);
            
            productRepository.save(product);
            logger.info("Product {} saved successfully", product.getShopifyProductId());

            return ResponseEntity.ok(Map.of("status", "success", "message", "Product processed"));

        } catch (Exception e) {
            logger.error("Error processing product webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Verify Shopify webhook signature using HMAC-SHA256
     */
    private boolean verifyWebhook(String payload, String hmacHeader, String secret) {
        if (hmacHeader == null || secret == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            
            byte[] digest = mac.doFinal(payload.getBytes());
            String computedHmac = Base64.getEncoder().encodeToString(digest);
            
            return computedHmac.equals(hmacHeader);
        } catch (Exception e) {
            logger.error("Error verifying webhook signature: ", e);
            return false;
        }
    }

    /**
     * Find tenant by Shopify shop domain
     */
    private Tenant findTenantByShopDomain(String shopDomain) {
        if (shopDomain == null) {
            return null;
        }
        return tenantRepository.findByShopifyDomain(shopDomain).orElse(null);
    }

    /**
     * Parse order from Shopify webhook JSON
     */
    private Order parseOrder(JsonNode orderJson, Tenant tenant) {
        Order order = new Order();
        order.setTenant(tenant);
        order.setShopifyOrderId(orderJson.get("id").asText());
        order.setOrderNumber(orderJson.has("order_number") ? orderJson.get("order_number").asText() : null);
        
        // Parse customer
        if (orderJson.has("customer") && !orderJson.get("customer").isNull()) {
            JsonNode customerJson = orderJson.get("customer");
            String customerId = customerJson.get("id").asText();
            Optional<Customer> existingCustomer = customerRepository.findByTenantTenantIdAndShopifyCustomerId(tenant.getTenantId(), customerId);
            existingCustomer.ifPresent(order::setCustomer);
        }

        // Parse financial status
        if (orderJson.has("financial_status")) {
            order.setFinancialStatus(orderJson.get("financial_status").asText());
        }

        // Parse fulfillment status
        if (orderJson.has("fulfillment_status") && !orderJson.get("fulfillment_status").isNull()) {
            order.setFulfillmentStatus(orderJson.get("fulfillment_status").asText());
        }

        // Parse prices
        if (orderJson.has("total_price")) {
            order.setTotalPrice(new BigDecimal(orderJson.get("total_price").asText()));
        }
        if (orderJson.has("subtotal_price")) {
            order.setSubtotalPrice(new BigDecimal(orderJson.get("subtotal_price").asText()));
        }
        if (orderJson.has("total_tax")) {
            order.setTotalTax(new BigDecimal(orderJson.get("total_tax").asText()));
        }
        if (orderJson.has("total_shipping")) {
            order.setTotalShipping(new BigDecimal(orderJson.get("total_shipping").asText()));
        }

        // Parse currency
        if (orderJson.has("currency")) {
            order.setCurrency(orderJson.get("currency").asText());
        }

        // Parse created_at date
        if (orderJson.has("created_at")) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(orderJson.get("created_at").asText());
                order.setCreatedAt(odt.toLocalDateTime());
            } catch (Exception e) {
                order.setCreatedAt(LocalDateTime.now());
            }
        } else {
            order.setCreatedAt(LocalDateTime.now());
        }

        order.setUpdatedAt(LocalDateTime.now());
        
        return order;
    }

    /**
     * Parse customer from Shopify webhook JSON
     */
    private Customer parseCustomer(JsonNode customerJson, Tenant tenant) {
        Customer customer = new Customer();
        customer.setTenant(tenant);
        customer.setShopifyCustomerId(customerJson.get("id").asText());
        
        if (customerJson.has("email") && !customerJson.get("email").isNull()) {
            customer.setEmail(customerJson.get("email").asText());
        }

        // Parse name
        if (customerJson.has("first_name") && !customerJson.get("first_name").isNull()) {
            customer.setFirstName(customerJson.get("first_name").asText());
        }
        if (customerJson.has("last_name") && !customerJson.get("last_name").isNull()) {
            customer.setLastName(customerJson.get("last_name").asText());
        }

        if (customerJson.has("phone") && !customerJson.get("phone").isNull()) {
            customer.setPhone(customerJson.get("phone").asText());
        }

        if (customerJson.has("orders_count")) {
            customer.setOrdersCount(customerJson.get("orders_count").asInt());
        }

        if (customerJson.has("total_spent")) {
            customer.setTotalSpent(new BigDecimal(customerJson.get("total_spent").asText()));
        }

        if (customerJson.has("created_at")) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(customerJson.get("created_at").asText());
                customer.setCreatedAt(odt.toLocalDateTime());
            } catch (Exception e) {
                customer.setCreatedAt(LocalDateTime.now());
            }
        } else {
            customer.setCreatedAt(LocalDateTime.now());
        }

        customer.setUpdatedAt(LocalDateTime.now());
        
        return customer;
    }

    /**
     * Parse product from Shopify webhook JSON
     */
    private Product parseProduct(JsonNode productJson, Tenant tenant) {
        Product product = new Product();
        product.setTenant(tenant);
        product.setShopifyProductId(productJson.get("id").asText());
        
        if (productJson.has("title")) {
            product.setTitle(productJson.get("title").asText());
        }

        if (productJson.has("vendor") && !productJson.get("vendor").isNull()) {
            product.setVendor(productJson.get("vendor").asText());
        }

        if (productJson.has("product_type") && !productJson.get("product_type").isNull()) {
            product.setProductType(productJson.get("product_type").asText());
        }

        // Parse price from first variant if available
        if (productJson.has("variants") && productJson.get("variants").isArray() && 
            productJson.get("variants").size() > 0) {
            JsonNode firstVariant = productJson.get("variants").get(0);
            if (firstVariant.has("price")) {
                product.setPrice(new BigDecimal(firstVariant.get("price").asText()));
            }
        }

        if (productJson.has("status")) {
            product.setStatus(productJson.get("status").asText());
        }

        if (productJson.has("created_at")) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(productJson.get("created_at").asText());
                product.setCreatedAt(odt.toLocalDateTime());
            } catch (Exception e) {
                product.setCreatedAt(LocalDateTime.now());
            }
        } else {
            product.setCreatedAt(LocalDateTime.now());
        }

        product.setUpdatedAt(LocalDateTime.now());
        
        return product;
    }
    
    /**
     * Webhook endpoint for Shopify cart creation
     */
    @PostMapping("/cart/create")
    public ResponseEntity<String> handleCartCreate(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmacHeader) {
        
        try {
            logger.info("Received cart_create webhook from domain: {}", shopDomain);
            
            // Process the webhook
            webhookService.processCartCreate(payload, shopDomain);
            
            return ResponseEntity.ok("Cart webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing cart_create webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
    
    /**
     * Webhook endpoint for Shopify checkout creation
     */
    @PostMapping("/checkout/create")
    public ResponseEntity<String> handleCheckoutCreate(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmacHeader) {
        
        try {
            logger.info("Received checkout_create webhook from domain: {}", shopDomain);
            
            // Process the webhook
            webhookService.processCheckoutCreate(payload, shopDomain);
            
            return ResponseEntity.ok("Checkout webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing checkout_create webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
    
    /**
     * Webhook endpoint for Shopify checkout update
     */
    @PostMapping("/checkout/update")
    public ResponseEntity<String> handleCheckoutUpdate(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmacHeader) {
        
        try {
            logger.info("Received checkout_update webhook from domain: {}", shopDomain);
            
            // Process the webhook
            webhookService.processCheckoutUpdate(payload, shopDomain);
            
            return ResponseEntity.ok("Checkout update webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing checkout_update webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
}

