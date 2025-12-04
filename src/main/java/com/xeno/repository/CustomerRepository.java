package com.xeno.repository;

import com.xeno.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByTenantTenantId(String tenantId);
    
    Optional<Customer> findByTenantTenantIdAndShopifyCustomerId(String tenantId, String shopifyCustomerId);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.tenantId = :tenantId ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomersBySpend(@Param("tenantId") String tenantId);
    
    long countByTenantTenantId(String tenantId);
    
    @Query("SELECT SUM(c.totalSpent) FROM Customer c WHERE c.tenant.tenantId = :tenantId")
    Double getTotalRevenueByTenant(@Param("tenantId") String tenantId);
}
