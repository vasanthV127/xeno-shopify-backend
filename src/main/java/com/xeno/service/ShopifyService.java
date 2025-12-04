package com.xeno.service;

import com.xeno.model.*;
import com.xeno.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopifyService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public void syncShopifyData(Tenant tenant) {
        log.info("Starting Shopify sync for tenant: {}", tenant.getTenantId());
        
        try {
            syncCustomers(tenant);
            syncProducts(tenant);
            syncOrders(tenant);
            log.info("Shopify sync completed successfully for tenant: {}", tenant.getTenantId());
        } catch (Exception e) {
            log.error("Error syncing Shopify data for tenant: {}", tenant.getTenantId(), e);
            throw new RuntimeException("Failed to sync Shopify data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void syncCustomers(Tenant tenant) {
        String url = String.format("https://%s/admin/api/2024-01/customers.json", tenant.getShopifyDomain());
        
        WebClient webClient = webClientBuilder.baseUrl(url).build();
        
        Mono<Map> response = webClient.get()
                .header("X-Shopify-Access-Token", tenant.getShopifyAccessToken())
                .retrieve()
                .bodyToMono(Map.class);

        response.subscribe(data -> {
            List<Map<String, Object>> customers = (List<Map<String, Object>>) data.get("customers");
            if (customers != null) {
                customers.forEach(customerData -> {
                    try {
                        saveCustomer(tenant, customerData);
                    } catch (Exception e) {
                        log.error("Error saving customer: {}", customerData, e);
                    }
                });
            }
        }, error -> log.error("Error fetching customers from Shopify", error));
    }

    @SuppressWarnings("unchecked")
    private void syncProducts(Tenant tenant) {
        String url = String.format("https://%s/admin/api/2024-01/products.json", tenant.getShopifyDomain());
        
        WebClient webClient = webClientBuilder.baseUrl(url).build();
        
        Mono<Map> response = webClient.get()
                .header("X-Shopify-Access-Token", tenant.getShopifyAccessToken())
                .retrieve()
                .bodyToMono(Map.class);

        response.subscribe(data -> {
            List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("products");
            if (products != null) {
                products.forEach(productData -> {
                    try {
                        saveProduct(tenant, productData);
                    } catch (Exception e) {
                        log.error("Error saving product: {}", productData, e);
                    }
                });
            }
        }, error -> log.error("Error fetching products from Shopify", error));
    }

    @SuppressWarnings("unchecked")
    private void syncOrders(Tenant tenant) {
        String url = String.format("https://%s/admin/api/2024-01/orders.json", tenant.getShopifyDomain());
        
        WebClient webClient = webClientBuilder.baseUrl(url).build();
        
        Mono<Map> response = webClient.get()
                .header("X-Shopify-Access-Token", tenant.getShopifyAccessToken())
                .retrieve()
                .bodyToMono(Map.class);

        response.subscribe(data -> {
            List<Map<String, Object>> orders = (List<Map<String, Object>>) data.get("orders");
            if (orders != null) {
                orders.forEach(orderData -> {
                    try {
                        saveOrder(tenant, orderData);
                    } catch (Exception e) {
                        log.error("Error saving order: {}", orderData, e);
                    }
                });
            }
        }, error -> log.error("Error fetching orders from Shopify", error));
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void saveCustomer(Tenant tenant, Map<String, Object> customerData) {
        String shopifyCustomerId = String.valueOf(customerData.get("id"));
        
        Customer customer = customerRepository
                .findByTenantTenantIdAndShopifyCustomerId(tenant.getTenantId(), shopifyCustomerId)
                .orElse(Customer.builder()
                        .tenant(tenant)
                        .shopifyCustomerId(shopifyCustomerId)
                        .build());

        customer.setEmail((String) customerData.get("email"));
        customer.setFirstName((String) customerData.get("first_name"));
        customer.setLastName((String) customerData.get("last_name"));
        customer.setPhone((String) customerData.get("phone"));
        customer.setOrdersCount(((Number) customerData.getOrDefault("orders_count", 0)).intValue());
        customer.setTotalSpent(Double.parseDouble(String.valueOf(customerData.getOrDefault("total_spent", "0.0"))));
        customer.setState((String) customerData.get("state"));
        customer.setTags((String) customerData.get("tags"));

        customerRepository.save(customer);
        log.debug("Customer saved: {}", shopifyCustomerId);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void saveProduct(Tenant tenant, Map<String, Object> productData) {
        String shopifyProductId = String.valueOf(productData.get("id"));
        
        Product product = productRepository
                .findByTenantTenantIdAndShopifyProductId(tenant.getTenantId(), shopifyProductId)
                .orElse(Product.builder()
                        .tenant(tenant)
                        .shopifyProductId(shopifyProductId)
                        .build());

        product.setTitle((String) productData.get("title"));
        product.setDescription((String) productData.get("body_html"));
        product.setVendor((String) productData.get("vendor"));
        product.setProductType((String) productData.get("product_type"));
        product.setStatus((String) productData.get("status"));

        List<Map<String, Object>> variants = (List<Map<String, Object>>) productData.get("variants");
        if (variants != null && !variants.isEmpty()) {
            Map<String, Object> firstVariant = variants.get(0);
            product.setPrice(Double.parseDouble(String.valueOf(firstVariant.getOrDefault("price", "0.0"))));
            product.setInventoryQuantity(((Number) firstVariant.getOrDefault("inventory_quantity", 0)).intValue());
        }

        List<Map<String, Object>> images = (List<Map<String, Object>>) productData.get("images");
        if (images != null && !images.isEmpty()) {
            product.setImageUrl((String) images.get(0).get("src"));
        }

        productRepository.save(product);
        log.debug("Product saved: {}", shopifyProductId);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void saveOrder(Tenant tenant, Map<String, Object> orderData) {
        String shopifyOrderId = String.valueOf(orderData.get("id"));
        
        Order order = orderRepository
                .findByTenantTenantIdAndShopifyOrderId(tenant.getTenantId(), shopifyOrderId)
                .orElse(Order.builder()
                        .tenant(tenant)
                        .shopifyOrderId(shopifyOrderId)
                        .build());

        String shopifyCustomerId = String.valueOf(((Map<String, Object>) orderData.get("customer")).get("id"));
        Customer customer = customerRepository
                .findByTenantTenantIdAndShopifyCustomerId(tenant.getTenantId(), shopifyCustomerId)
                .orElse(null);

        order.setCustomer(customer);
        order.setOrderNumber(String.valueOf(orderData.get("order_number")));
        order.setOrderDate(parseDateTime((String) orderData.get("created_at")));
        order.setTotalPrice(Double.parseDouble(String.valueOf(orderData.get("total_price"))));
        order.setSubtotalPrice(Double.parseDouble(String.valueOf(orderData.getOrDefault("subtotal_price", "0.0"))));
        order.setTotalTax(Double.parseDouble(String.valueOf(orderData.getOrDefault("total_tax", "0.0"))));
        order.setFinancialStatus((String) orderData.get("financial_status"));
        order.setFulfillmentStatus((String) orderData.get("fulfillment_status"));
        order.setCurrency((String) orderData.getOrDefault("currency", "USD"));

        Order savedOrder = orderRepository.save(order);

        // Save order items
        List<Map<String, Object>> lineItems = (List<Map<String, Object>>) orderData.get("line_items");
        if (lineItems != null) {
            lineItems.forEach(lineItem -> saveOrderItem(savedOrder, lineItem));
        }

        log.debug("Order saved: {}", shopifyOrderId);
    }

    @Transactional
    public void saveOrderItem(Order order, Map<String, Object> lineItemData) {
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .shopifyProductId(String.valueOf(lineItemData.get("product_id")))
                .productTitle((String) lineItemData.get("title"))
                .variantTitle((String) lineItemData.get("variant_title"))
                .quantity(((Number) lineItemData.get("quantity")).intValue())
                .price(Double.parseDouble(String.valueOf(lineItemData.get("price"))))
                .totalDiscount(Double.parseDouble(String.valueOf(lineItemData.getOrDefault("total_discount", "0.0"))))
                .build();

        orderItemRepository.save(orderItem);
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Could not parse datetime: {}", dateTimeString);
            return LocalDateTime.now();
        }
    }
}
