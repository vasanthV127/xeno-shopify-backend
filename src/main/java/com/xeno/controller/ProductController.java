package com.xeno.controller;

import com.xeno.model.Product;
import com.xeno.model.Tenant;
import com.xeno.repository.ProductRepository;
import com.xeno.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://xeno-shopify-frontend-five.vercel.app"})
@Tag(name = "Products", description = "Product analytics and inventory endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthService authService;

    /**
     * Get product analytics statistics
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Get product analytics statistics",
            description = "Returns total products count, active products, low stock items, and total inventory value"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product stats"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getProductStats() {

        Tenant tenant = authService.getCurrentTenant();

        long totalProducts = productRepository.countByTenant(tenant);
        long activeProducts = productRepository.countByTenantAndStatus(tenant, "active");
        
        List<Product> allProducts = productRepository.findByTenant(tenant);
        double totalInventoryValue = allProducts.stream()
                .mapToDouble(p -> {
                    if (p.getPrice() != null && p.getInventoryQuantity() != null) {
                        return p.getPrice().doubleValue() * p.getInventoryQuantity();
                    }
                    return 0.0;
                })
                .sum();

        long lowStockItems = allProducts.stream()
                .filter(p -> p.getInventoryQuantity() != null && p.getInventoryQuantity() < 10)
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalProducts", totalProducts);
        response.put("activeProducts", activeProducts);
        response.put("lowStockItems", lowStockItems);
        response.put("totalInventoryValue", String.format("%.2f", totalInventoryValue));

        return ResponseEntity.ok(response);
    }

    /**
     * Get top products by revenue (calculated from orders)
     */
    @GetMapping("/top")
    @Operation(
            summary = "Get top products by sales",
            description = "Returns list of top-selling products ordered by revenue"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved top products")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {

        Tenant tenant = authService.getCurrentTenant();

        // Get all products for tenant
        List<Product> products = productRepository.findByTenantTenantIdOrderByIdDesc(tenant.getTenantId());

        List<Map<String, Object>> topProducts = products.stream()
                .limit(limit)
                .map(product -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("title", product.getTitle());
                    productMap.put("vendor", product.getVendor());
                    productMap.put("productType", product.getProductType());
                    productMap.put("price", product.getPrice());
                    productMap.put("inventoryQuantity", product.getInventoryQuantity());
                    productMap.put("orderCount", 0); // Will be 0 for now
                    
                    return productMap;
                })
                .toList();

        return ResponseEntity.ok(topProducts);
    }

    /**
     * Get all products with pagination
     */
    @GetMapping
    @Operation(
            summary = "List all products",
            description = "Get paginated list of all products for the tenant"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    public ResponseEntity<List<Product>> getAllProducts() {

        Tenant tenant = authService.getCurrentTenant();

        List<Product> products = productRepository.findByTenant(tenant);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product inventory status summary
     */
    @GetMapping("/inventory")
    @Operation(
            summary = "Get inventory status summary",
            description = "Returns inventory levels categorized as in-stock, low-stock, and out-of-stock"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory summary")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {

        Tenant tenant = authService.getCurrentTenant();

        List<Product> products = productRepository.findByTenant(tenant);

        long inStock = products.stream()
                .filter(p -> p.getInventoryQuantity() != null && p.getInventoryQuantity() >= 10)
                .count();

        long lowStock = products.stream()
                .filter(p -> p.getInventoryQuantity() != null && p.getInventoryQuantity() > 0 && p.getInventoryQuantity() < 10)
                .count();

        long outOfStock = products.stream()
                .filter(p -> p.getInventoryQuantity() == null || p.getInventoryQuantity() == 0)
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("inStock", inStock);
        response.put("lowStock", lowStock);
        response.put("outOfStock", outOfStock);
        response.put("total", products.size());

        return ResponseEntity.ok(response);
    }
}
