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
    
    @Query("SELECT p FROM Product p WHERE p.tenant.tenantId = :tenantId ORDER BY p.id DESC")
    List<Product> findByTenantTenantIdOrderByIdDesc(@Param("tenantId") String tenantId);
    
    @Query("SELECT p.shopifyProductId as productId, p.title as title, COUNT(oi.id) as orderCount " +
           "FROM Product p " +
           "LEFT JOIN OrderItem oi ON oi.shopifyProductId = p.shopifyProductId " +
           "LEFT JOIN Order o ON o.id = oi.order.id " +
           "WHERE p.tenant.id = :tenantId AND (o.tenant.id = :tenantId OR o.tenant.id IS NULL) " +
           "GROUP BY p.shopifyProductId, p.title " +
           "ORDER BY orderCount DESC")
    List<Object[]> findTopProductsByOrderCount(@Param("tenantId") Long tenantId);
}
