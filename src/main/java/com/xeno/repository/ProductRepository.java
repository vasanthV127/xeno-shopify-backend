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
    
    @Query("SELECT p, COUNT(oi.id) as orderCount FROM Product p " +
           "LEFT JOIN OrderItem oi ON oi.product.id = p.id " +
           "LEFT JOIN Order o ON oi.order.id = o.id " +
           "WHERE p.tenant.tenantId = :tenantId " +
           "GROUP BY p.id " +
           "ORDER BY orderCount DESC")
    List<Object[]> findTopProductsByOrders(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
