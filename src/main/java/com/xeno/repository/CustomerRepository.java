package com.xeno.repository;

import com.xeno.model.Customer;
import com.xeno.model.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByTenantTenantId(String tenantId);
    
    Optional<Customer> findByTenantTenantIdAndShopifyCustomerId(String tenantId, String shopifyCustomerId);
    
    Optional<Customer> findByShopifyCustomerIdAndTenant(String shopifyCustomerId, Tenant tenant);
    
    Optional<Customer> findByIdAndTenant(Long id, Tenant tenant);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.tenantId = :tenantId ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomersBySpend(@Param("tenantId") String tenantId);
    
    long countByTenantTenantId(String tenantId);
    
    long countByTenant(Tenant tenant);
    
    long countByTenantAndTotalSpentGreaterThan(Tenant tenant, BigDecimal amount);
    
    long countByTenantAndTotalSpentBetween(Tenant tenant, BigDecimal minAmount, BigDecimal maxAmount);
    
    long countByTenantAndTotalSpentLessThan(Tenant tenant, BigDecimal amount);
    
    @Query("SELECT SUM(c.totalSpent) FROM Customer c WHERE c.tenant.tenantId = :tenantId")
    Double getTotalRevenueByTenant(@Param("tenantId") String tenantId);
    
    // Pagination and filtering
    Page<Customer> findByTenant(Tenant tenant, Pageable pageable);
    
    List<Customer> findByTenant(Tenant tenant);
    
    Page<Customer> findByTenantAndTotalSpentGreaterThan(Tenant tenant, BigDecimal amount, Pageable pageable);
    
    List<Customer> findByTenantAndTotalSpentGreaterThan(Tenant tenant, BigDecimal amount);
    
    Page<Customer> findByTenantAndTotalSpentBetween(Tenant tenant, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    
    List<Customer> findByTenantAndTotalSpentBetween(Tenant tenant, BigDecimal minAmount, BigDecimal maxAmount);
    
    Page<Customer> findByTenantAndTotalSpentLessThan(Tenant tenant, BigDecimal amount, Pageable pageable);
    
    List<Customer> findByTenantAndTotalSpentLessThan(Tenant tenant, BigDecimal amount);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant = :tenant AND " +
           "(LOWER(c.firstName) LIKE :search OR LOWER(c.lastName) LIKE :search OR LOWER(c.email) LIKE :search)")
    Page<Customer> searchCustomers(@Param("search") String search, @Param("tenant") Tenant tenant, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant = :tenant AND " +
           "(LOWER(c.firstName) LIKE :search OR LOWER(c.lastName) LIKE :search OR LOWER(c.email) LIKE :search)")
    List<Customer> searchCustomersAll(@Param("search") String search, @Param("tenant") Tenant tenant);
}
