package com.xeno.repository;

import com.xeno.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByTenantId(String tenantId);
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByShopifyDomain(String shopifyDomain);
    boolean existsByEmail(String email);
    boolean existsByShopifyDomain(String shopifyDomain);
}
