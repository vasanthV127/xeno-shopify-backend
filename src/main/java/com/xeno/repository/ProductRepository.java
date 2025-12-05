package com.xeno.repository;

import com.xeno.model.Product;
import com.xeno.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenantTenantId(String tenantId);
    
    List<Product> findByTenant(Tenant tenant);
    
    long countByTenant(Tenant tenant);
    
    long countByTenantAndStatus(Tenant tenant, String status);
    
    Optional<Product> findByTenantTenantIdAndShopifyProductId(String tenantId, String shopifyProductId);
    
    long countByTenantTenantId(String tenantId);
    
    @Query(value = "SELECT p.*, COUNT(oi.id) as order_count FROM products p " +
           "LEFT JOIN order_items oi ON oi.shopify_product_id = p.shopify_product_id " +
           "LEFT JOIN orders o ON oi.order_id = o.id " +
           "WHERE p.tenant_id = (SELECT id FROM tenants WHERE tenant_id = :tenantId) " +
           "GROUP BY p.id " +
           "ORDER BY order_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopProductsByOrders(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
