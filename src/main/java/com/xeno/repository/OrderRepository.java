package com.xeno.repository;

import com.xeno.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTenantTenantId(String tenantId);
    
    Optional<Order> findByTenantTenantIdAndShopifyOrderId(String tenantId, String shopifyOrderId);
    
    List<Order> findByTenantTenantIdAndOrderDateBetween(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    long countByTenantTenantId(String tenantId);
    
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.tenant.tenantId = :tenantId")
    Double getTotalRevenueByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT DATE(o.orderDate) as date, SUM(o.totalPrice) as revenue, COUNT(o) as orderCount " +
           "FROM Order o WHERE o.tenant.tenantId = :tenantId " +
           "AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.orderDate) ORDER BY DATE(o.orderDate)")
    List<Object[]> getOrderStatsByDateRange(@Param("tenantId") String tenantId, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
}
